from pydantic import BaseModel

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