import pandas as pd
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.metrics.pairwise import cosine_similarity

def generate_recommendations(user_history, user_preferences, nearby_places):
    """
    user_history: Kullanıcının gittiği yerler (Ziyaretler)
    user_preferences: Kullanıcının seçtiği ilgi alanları (Soğuk Başlangıç için)
    nearby_places: Google'dan gelen aday mekanlar
    """
    
    user_profile_text = ""

    # 1. STRATEJİ: Önce Ziyaret Geçmişine Bak (En Güçlü Veri)
    if user_history:
        user_profile_text = " ".join([visit.place_category for visit in user_history])
    
    # 2. STRATEJİ: Geçmiş yoksa, Tercihlere Bak (Soğuk Başlangıç)
    elif user_preferences:
        # user_preferences veritabanında "museum,park" şeklinde string olarak duruyor olabilir
        # veya biz onu string olarak alacağız.
        user_profile_text = user_preferences.liked_categories.replace(",", " ")
    
    # 3. Hâlâ veri yoksa, AI çalışamaz. Listeyi olduğu gibi döndür.
    if not user_profile_text:
        return nearby_places

    # --- BURADAN SONRASI AYNI ---
    
    # Aday Mekanları Hazırla
    candidate_docs = []
    for place in nearby_places:
        types = " ".join(place.get("types", []))
        candidate_docs.append(types)
        
    # Vektörleştirme
    # Listenin başına kullanıcının profilini ekliyoruz.
    corpus = [user_profile_text] + candidate_docs
    
    vectorizer = CountVectorizer()
    count_matrix = vectorizer.fit_transform(corpus)
    
    # Benzerlik Hesaplama
    user_vector = count_matrix[0]
    candidate_vectors = count_matrix[1:]
    
    similarity_scores = cosine_similarity(user_vector, candidate_vectors)[0]
    
    # Sonuçları Birleştir ve Sırala
    recommendations = []
    for i, place in enumerate(nearby_places):
        place_with_score = place.copy()
        place_with_score["ai_score"] = float(similarity_scores[i])
        recommendations.append(place_with_score)
        
    recommendations.sort(key=lambda x: x["ai_score"], reverse=True)
    
    return recommendations