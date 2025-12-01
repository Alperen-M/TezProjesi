from pydantic import BaseModel
from datetime import datetime

# 1. Kayıt Olurken Gelen Veri
class UserCreate(BaseModel):
    email: str
    password: str
    full_name: str | None = None

# 2. Kullanıcıya Döndüğümüz Veri (Şifresiz)
class UserOut(BaseModel):
    id: int
    email: str
    full_name: str | None = None

    class Config:
        from_attributes = True

# 3. Giriş Yaparken Gelen Veri (YENİ EKLENDİ)
class UserLogin(BaseModel):
    email: str
    password: str

# 4. Token (Kimlik Kartı) Modeli (HATANIN SEBEBİ BU EKSİKTİ)
class Token(BaseModel):
    access_token: str
    token_type: str

# 5. Ziyaret Ekleme (Kullanıcıdan Gelen Veri)
class VisitCreate(BaseModel):
    place_id: str
    place_name: str
    place_category: str

# 6. Ziyaret Yanıtı (Kullanıcıya Döndüğümüz Veri)
class VisitOut(VisitCreate):
    id: int
    created_at: datetime
    user_id: int

    class Config:
        from_attributes = True

# 7. Tercih Belirleme (Kullanıcıdan Gelen)
class PreferenceCreate(BaseModel):
    categories: list[str] # Örn: ["museum", "cafe"]

# 8. Tercih Yanıtı
class PreferenceOut(BaseModel):
    liked_categories: str
    
 # 9. FRONTEND İÇİN ÖZEL SADELEŞTİRİLMİŞ MEKAN MODELİ (YENİ!)
class PlaceResponse(BaseModel):
    place_id: str
    place_name: str
    lat: float
    lon: float
    address: str
    ai_score: float = 0.0 # AI Puanını da ekleyelim