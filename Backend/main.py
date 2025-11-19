from fastapi import FastAPI, HTTPException, Depends, status
from fastapi.security import OAuth2PasswordBearer 
from sqlalchemy.orm import Session
from passlib.context import CryptContext
from jose import JWTError, jwt
from datetime import datetime, timedelta
import models, schemas, database
import requests
import os
from dotenv import load_dotenv
import ai 

load_dotenv()

# --- AYARLAR ---
models.Base.metadata.create_all(bind=database.engine)
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

GOOGLE_API_KEY = os.getenv("GOOGLE_PLACES_API_KEY")
SECRET_KEY = os.getenv("SECRET_KEY")
ALGORITHM = os.getenv("ALGORITHM")
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", 30))

# Token'ın nereden alınacağını belirtiyoruz (Swagger UI için)
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="users/login")

if not GOOGLE_API_KEY or not SECRET_KEY:
    raise RuntimeError("Eksik API anahtarları! .env dosyasını kontrol edin.")

app = FastAPI()

# --- YARDIMCI FONKSİYONLAR ---

def get_db():
    db = database.SessionLocal()
    try:
        yield db
    finally:
        db.close()

def get_password_hash(password):
    return pwd_context.hash(password)

def verify_password(plain_password, hashed_password):
    return pwd_context.verify(plain_password, hashed_password)

def create_access_token(data: dict):
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

# --- GÜVENLİK GÖREVLİSİ (YENİ!) ---
# Bu fonksiyon, token gerektiren her sayfada çalışacak.
def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Kimlik doğrulanamadı",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        # 1. Token'ı çöz
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        email: str = payload.get("sub")
        if email is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
        
    # 2. Token içindeki e-postaya sahip kullanıcıyı bul
    user = db.query(models.User).filter(models.User.email == email).first()
    if user is None:
        raise credentials_exception
        
    return user

# --- ENDPOINTLER ---

@app.get("/")
def read_root():
    return {"message": "GezginAsistan API'sine hoş geldiniz!"}

# 1. KAYIT OL
@app.post("/users/register", response_model=schemas.UserOut)
def register_user(user: schemas.UserCreate, db: Session = Depends(get_db)):
    db_user = db.query(models.User).filter(models.User.email == user.email).first()
    if db_user:
        raise HTTPException(status_code=400, detail="Bu e-posta zaten kayıtlı.")
    
    hashed_password = get_password_hash(user.password)
    new_user = models.User(email=user.email, hashed_password=hashed_password, full_name=user.full_name)
    
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    return new_user

# 2. GİRİŞ YAP (Burada form-data desteği ekledik ki Swagger'daki kilit butonu çalışsın)
from fastapi.security import OAuth2PasswordRequestForm
@app.post("/users/login", response_model=schemas.Token)
def login_for_access_token(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    # Not: Swagger UI 'username' gönderir, biz onu 'email' olarak kullanacağız
    user = db.query(models.User).filter(models.User.email == form_data.username).first()
    if not user or not verify_password(form_data.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="E-posta veya şifre hatalı",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    access_token = create_access_token(data={"sub": user.email})
    return {"access_token": access_token, "token_type": "bearer"}

# 3. PROFİLİMİ GÖR (SADECE GİRİŞ YAPANLAR GÖREBİLİR) - YENİ!
@app.get("/users/me", response_model=schemas.UserOut)
def read_users_me(current_user: models.User = Depends(get_current_user)):
    return current_user

# 4. YAKINDAKİ YERLER
@app.get("/api/v1/places/nearby")
def get_nearby_places(lat: float, lon: float, radius: int = 1500, type: str = "restaurant"):
    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
    params = {
        "location": f"{lat},{lon}",
        "radius": str(radius),
        "type": type,
        "key": GOOGLE_API_KEY
    }
    try:
        response = requests.get(url, params=params)
        response.raise_for_status()
        return response.json()
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 5. ZİYARET ET (CHECK-IN) - SADECE GİRİŞ YAPANLAR!
@app.post("/places/visit", response_model=schemas.VisitOut)
def visit_place(
    visit: schemas.VisitCreate, 
    db: Session = Depends(get_db), 
    current_user: models.User = Depends(get_current_user)
):
    # Yeni ziyaret kaydı oluştur
    new_visit = models.Visit(
        user_id=current_user.id, # Giriş yapmış kullanıcının ID'si
        place_id=visit.place_id,
        place_name=visit.place_name,
        place_category=visit.place_category
    )
    
    db.add(new_visit)
    db.commit()
    db.refresh(new_visit)
    return new_visit
# ... üsttekiler kalsın ...

# 7. YAPAY ZEKA DESTEKLİ ÖNERİLER (FİNAL)
@app.get("/recommendations/nearby")
def get_ai_recommendations(
    lat: float, 
    lon: float, 
    radius: int = 1500, 
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):

    # 1. Kullanıcının geçmişini veritabanından çek
    user_history = db.query(models.Visit).filter(models.Visit.user_id == current_user.id).all()
    
    # 2. Google'dan etraftaki mekanları çek (Mevcut fonksiyonumuzu kullanalım)
    # (Burada kod tekrarı yapmamak için yukarıdaki fonksiyonu çağırabiliriz ama
    # şimdilik temiz olsun diye request atıyoruz, normalde fonksiyonu ayırmak daha iyidir)
    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
    params = {
        "location": f"{lat},{lon}",
        "radius": str(radius),
        "type": "point_of_interest", # Her şeyi getirsin, sadece restoran değil
        "key": GOOGLE_API_KEY
    }
    response = requests.get(url, params=params)
    nearby_places = response.json().get("results", [])
    
    # 3. Yapay Zeka Motorunu Çalıştır
    sorted_places = ai.generate_recommendations(user_history, nearby_places)
    
    return sorted_places