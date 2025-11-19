from fastapi import FastAPI, HTTPException, Depends, status
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from sqlalchemy.orm import Session
from passlib.context import CryptContext
from jose import JWTError, jwt
from datetime import datetime, timedelta
import models, schemas, database, ai
import requests
import os
from dotenv import load_dotenv

load_dotenv()

# --- AYARLAR ---
models.Base.metadata.create_all(bind=database.engine)
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")

GOOGLE_API_KEY = os.getenv("GOOGLE_PLACES_API_KEY")
SECRET_KEY = os.getenv("SECRET_KEY")
ALGORITHM = os.getenv("ALGORITHM")
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", 30))

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="users/login")

if not GOOGLE_API_KEY or not SECRET_KEY:
    raise RuntimeError("Eksik API anahtarları! .env dosyasını kontrol edin.")

# --- UYGULAMA TANIMI VE DOKÜMANTASYON AYARLARI ---
app = FastAPI(
    title="GezginAsistan API",
    description="Konum tabanlı akıllı öneri sistemi için geliştirilmiş, Yapay Zeka destekli Backend API.",
    version="1.0.0",
    contact={
        "name": "GezginAsistan Geliştirici Ekibi",
        "email": "iletisim@gezginasistan.com",
    },
)

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

# GÜVENLİK GÖREVLİSİ
def get_current_user(token: str = Depends(oauth2_scheme), db: Session = Depends(get_db)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Kimlik doğrulanamadı",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        email: str = payload.get("sub")
        if email is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
        
    user = db.query(models.User).filter(models.User.email == email).first()
    if user is None:
        raise credentials_exception
        
    return user

# --- ENDPOINTLER ---

@app.get("/", summary="Sunucu Durumu", description="API'nin çalışıp çalışmadığını kontrol eder.")
def read_root():
    return {"message": "GezginAsistan API'sine hoş geldiniz! Sistem aktif."}

@app.get("/stats", summary="Sistem İstatistikleri", description="Sistemdeki toplam kullanıcı ve ziyaret sayılarını gösterir (Admin paneli için).")
def get_system_stats(db: Session = Depends(get_db)):
    user_count = db.query(models.User).count()
    visit_count = db.query(models.Visit).count()
    return {
        "total_users": user_count,
        "total_visits": visit_count,
        "system_status": "Active",
        "version": "1.0.0"
    }

# 1. KAYIT OL
@app.post("/users/register", response_model=schemas.UserOut, summary="Kullanıcı Kaydı", description="Yeni bir kullanıcı hesabı oluşturur ve veritabanına kaydeder.")
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

# 2. GİRİŞ YAP
@app.post("/users/login", response_model=schemas.Token, summary="Giriş Yap ve Token Al", description="E-posta ve şifre ile giriş yaparak kimlik doğrulama token'ı (JWT) alır.")
def login_for_access_token(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    user = db.query(models.User).filter(models.User.email == form_data.username).first()
    if not user or not verify_password(form_data.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="E-posta veya şifre hatalı",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    access_token = create_access_token(data={"sub": user.email})
    return {"access_token": access_token, "token_type": "bearer"}

# 3. PROFİLİMİ GÖR
@app.get("/users/me", response_model=schemas.UserOut, summary="Kullanıcı Profilini Getir", description="Giriş yapmış olan kullanıcının kendi bilgilerini döndürür.")
def read_users_me(current_user: models.User = Depends(get_current_user)):
    return current_user

# 4. İLGİ ALANLARINI KAYDET
@app.post("/users/preferences", response_model=schemas.PreferenceOut, summary="İlgi Alanlarını Kaydet", description="Kullanıcının sevdiği kategorileri (Soğuk Başlangıç için) kaydeder.")
def set_user_preferences(
    pref: schemas.PreferenceCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    categories_str = ",".join(pref.categories)
    existing_pref = db.query(models.UserPreference).filter(models.UserPreference.user_id == current_user.id).first()
    
    if existing_pref:
        existing_pref.liked_categories = categories_str
        db.commit()
        db.refresh(existing_pref)
        return existing_pref
    else:
        new_pref = models.UserPreference(user_id=current_user.id, liked_categories=categories_str)
        db.add(new_pref)
        db.commit()
        db.refresh(new_pref)
        return new_pref

# 5. YAKINDAKİ YERLER (GOOGLE)
@app.get("/api/v1/places/nearby", summary="Yakındaki Mekanları Getir (Google)", description="Google Places API kullanarak verilen koordinatlar çevresindeki mekanları ham olarak listeler.")
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

# 6. ZİYARET ET (CHECK-IN)
@app.post("/places/visit", response_model=schemas.VisitOut, summary="Mekan Ziyareti Ekle", description="Kullanıcının gittiği bir mekanı veritabanına kaydeder (AI Eğitimi için).")
def visit_place(
    visit: schemas.VisitCreate, 
    db: Session = Depends(get_db), 
    current_user: models.User = Depends(get_current_user)
):
    new_visit = models.Visit(
        user_id=current_user.id,
        place_id=visit.place_id,
        place_name=visit.place_name,
        place_category=visit.place_category
    )
    db.add(new_visit)
    db.commit()
    db.refresh(new_visit)
    return new_visit

# 7. ZİYARET GEÇMİŞİ
@app.get("/places/history", response_model=list[schemas.VisitOut], summary="Ziyaret Geçmişini Getir", description="Kullanıcının daha önce gittiği mekanların listesini döndürür.")
def read_visit_history(
    db: Session = Depends(get_db), 
    current_user: models.User = Depends(get_current_user)
):
    visits = db.query(models.Visit).filter(models.Visit.user_id == current_user.id).all()
    return visits

# 8. AI ÖNERİLERİ
@app.get("/recommendations/nearby", summary="AI Destekli Öneriler", description="Kullanıcının geçmişine ve tercihlerine göre kişiselleştirilmiş mekan önerileri sunar.")
def get_ai_recommendations(
    lat: float, 
    lon: float, 
    radius: int = 1500, 
    db: Session = Depends(get_db),
    current_user: models.User = Depends(get_current_user)
):
    # 1. Geçmişi Çek
    user_history = db.query(models.Visit).filter(models.Visit.user_id == current_user.id).all()
    
    # 2. Tercihleri Çek
    user_preferences = db.query(models.UserPreference).filter(models.UserPreference.user_id == current_user.id).first()
    
    # 3. Google Verisi Çek
    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
    params = {
        "location": f"{lat},{lon}",
        "radius": str(radius),
        "type": "point_of_interest", 
        "key": GOOGLE_API_KEY
    }
    response = requests.get(url, params=params)
    nearby_places = response.json().get("results", [])
    
    # 4. AI Motoruna Gönder
    sorted_places = ai.generate_recommendations(user_history, user_preferences, nearby_places)
    
    return sorted_places