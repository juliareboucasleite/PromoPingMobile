package com.example.promopingmobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "promoping_prefs")

class SessionManager(private val context: Context) {

    private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    private val REMEMBER_ME_KEY = booleanPreferencesKey("remember_me")

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[TOKEN_KEY]
    }

    val rememberMeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[REMEMBER_ME_KEY] ?: false
    }

    suspend fun saveToken(token: String, rememberMe: Boolean = false) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            prefs[REMEMBER_ME_KEY] = rememberMe
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(REMEMBER_ME_KEY)
        }
    }
}
