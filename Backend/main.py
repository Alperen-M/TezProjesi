from fastapi import FastAPI, HTTPException
import requests
import os
from dotenv import load_dotenv

# .env dosyasındaki değişkenleri yükle
load_dotenv()

# API anahtarımızı .env dosyasından oku
GOOGLE_API_KEY = os.getenv("GOOGLE_PLACES_API_KEY")

# API anahtarının yüklenip yüklenmediğini kontrol et
if not GOOGLE_API_KEY:
    raise RuntimeError("GOOGLE_PLACES_API_KEY bulunamadı. Lütfen .env dosyasını kontrol edin.")

app = FastAPI()

# --- Ana URL ---
@app.get("/")
def read_root():
    return {"message": "GezginAsistan API'sine hoş geldiniz!"}

# --- YAKINDAKİ MEKANLARI GETİREN API ENDPOINT'İ (2. HAFTA HEDEFİ) ---
# Bu URL'yi Frontend (Android) uygulaması çağıracak
@app.get("/api/v1/places/nearby")
def get_nearby_places(lat: float, lon: float, radius: int = 1500, type: str = "restaurant"):
    """
    Verilen enlem (lat) ve boylama (lon) göre yakındaki mekanları getirir.
    Örnek Kullanım: /api/v1/places/nearby?lat=48.8584&lon=2.2945
    """
    
    url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
    
    # Parametreleri dinamik olarak alıyoruz
    params = {
        "location": f"{lat},{lon}",  # Frontend'den gelen koordinatlar
        "radius": str(radius),
        "type": type,
        "key": GOOGLE_API_KEY
    }

    try:
        response = requests.get(url, params=params)
        response.raise_for_status()  # HTTP 4xx veya 5xx hatası varsa exception fırlatır
        
        data = response.json()
        
        if data.get("status") not in ["OK", "ZERO_RESULTS"]:
            # Google API'den gelen spesifik hataları yönet
            raise HTTPException(status_code=400, detail=f"Google API Error: {data.get('status')} - {data.get('error_message', '')}")
            
        return data
        
    except requests.exceptions.RequestException as e:
        # Ağ hatası veya HTTP hatası olursa
        raise HTTPException(status_code=503, detail=f"Servise ulaşılamıyor: {e}")
    except Exception as e:
        # Diğer beklenmedik hatalar için
        raise HTTPException(status_code=500, detail=f"Beklenmedik bir hata oluştu: {str(e)}")