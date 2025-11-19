from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker
import os
from dotenv import load_dotenv

load_dotenv()

# --- DÜZELTİLEN KISIM BURASI ---
# Parantez içine sadece .env dosyasındaki İSMİ yazıyoruz.
SQLALCHEMY_DATABASE_URL = os.getenv("DATABASE_URL")

# Bağlantı motorunu oluştur
engine = create_engine(SQLALCHEMY_DATABASE_URL)

# Veritabanı oturumu (session) oluşturucu
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Veritabanı modelleri için temel sınıf
Base = declarative_base()

# Her istekte veritabanını açıp iş bitince kapatan yardımcı fonksiyon
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()