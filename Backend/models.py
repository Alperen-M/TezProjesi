from sqlalchemy import Column, Integer, String, ForeignKey, DateTime
from sqlalchemy.sql import func
from database import Base

# Kullanıcılar Tablosu
class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True) # E-posta benzersiz olmalı
    hashed_password = Column(String) # Şifreyi açık değil, şifreli saklayacağız
    full_name = Column(String, nullable=True)

# ZİYARET GEÇMİŞİ TABLOSU
class Visit(Base):
    __tablename__ = "visits"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id")) # Hangi kullanıcı?
    
    place_id = Column(String)       # Google'ın verdiği mekan ID'si
    place_name = Column(String)     # Mekanın adı (Örn: Starbucks)
    place_category = Column(String) # Kategori (Örn: restaurant, cafe)
    
    created_at = Column(DateTime(timezone=True), server_default=func.now()) # Ne zaman gitti?