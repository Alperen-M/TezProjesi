import pandas as pd
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.metrics.pairwise import cosine_similarity

def generate_recommendations(user_history, nearby_places):
    """
    user_history: Kullanıcının gittiği yerlerin listesi (Veritabanından)
    nearby_places: Google'dan gelen etraftaki mekanlar
    """
    
    # 1. Eğer kullanıcının hiç geçmişi yoksa, AI çalışamaz.
    # O zaman Google'dan gelen listeyi olduğu gibi geri döndürürüz.
    if not user_history:
        return nearby_places

    # 2. Kullanıcı Profilini Oluştur
    # Kullanıcının gittiği mekanların kategorilerini birleştiriyoruz.
    # Örn: "museum cafe restaurant museum"
    user_categories = " ".join([visit.place_category for visit in user_history])
    
    # 3. Aday Mekanları Hazırla
    # Google'dan gelen her mekan için bir "Metin" oluşturuyoruz.
    # Örn: Mekan A -> "restaurant food point_of_interest"
    candidate_docs = []
    for place in nearby_places:
        # Google'dan gelen 'types' listesini alıp birleştiriyoruz
        types = " ".join(place.get("types", []))
        candidate_docs.append(types)
        
    # 4. Vektörleştirme (Metni Sayılara Çevirme)
    # Scikit-learn burada devreye giriyor. Kelimeleri sayıp matematiksel vektör yapıyor.
    # Listenin başına kullanıcının profilini ekliyoruz.
    corpus = [user_categories] + candidate_docs
    
    vectorizer = CountVectorizer()
    count_matrix = vectorizer.fit_transform(corpus)
    
    # 5. Benzerlik Hesaplama (Cosine Similarity)
    # Kullanıcı profili (0. indeks) ile diğer tüm mekanlar arasındaki açıyı ölçüyoruz.
    user_vector = count_matrix[0]
    candidate_vectors = count_matrix[1:]
    
    similarity_scores = cosine_similarity(user_vector, candidate_vectors)[0]
    
    # 6. Sonuçları Birleştir ve Sırala
    # Her mekana bir "AI Skoru" ekliyoruz.
    recommendations = []
    for i, place in enumerate(nearby_places):
        place_with_score = place.copy()
        place_with_score["ai_score"] = float(similarity_scores[i]) # Skoru ekle (0.0 ile 1.0 arası)
        recommendations.append(place_with_score)
        
    # Skoru en yüksek olanı en başa al (Sıralama)
    recommendations.sort(key=lambda x: x["ai_score"], reverse=True)
    
    return recommendations