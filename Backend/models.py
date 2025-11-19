from sqlalchemy import Column, Integer, String
from database import Base

# Kullanıcılar Tablosu
class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    email = Column(String, unique=True, index=True) # E-posta benzersiz olmalı
    hashed_password = Column(String) # Şifreyi açık değil, şifreli saklayacağız
    full_name = Column(String, nullable=True)