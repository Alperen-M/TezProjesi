package com.example.gezginasistan.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Context extension ile DataStore tanımı
private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthDataStore(private val context: Context) {

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
    }

    // Token'ı Flow olarak oku
    val accessTokenFlow: Flow<String?> = context.authDataStore.data.map { prefs ->
        prefs[KEY_ACCESS_TOKEN]
    }

    // Token'ı yaz
    suspend fun saveAccessToken(token: String) {
        context.authDataStore.edit { prefs ->
            prefs[KEY_ACCESS_TOKEN] = token
        }
    }

    // Token'ı temizle (logout vs.)
    suspend fun clearAccessToken() {
        context.authDataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS_TOKEN)
        }
    }
}
