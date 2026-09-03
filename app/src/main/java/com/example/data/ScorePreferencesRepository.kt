package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_preferences")

class ScorePreferencesRepository(private val context: Context) {

    private val highScoreKey = intPreferencesKey("all_time_high_score")

    val highScoreFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[highScoreKey] ?: 0
    }

    suspend fun saveHighScore(newHighScore: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[highScoreKey] ?: 0
            if (newHighScore > current) {
                preferences[highScoreKey] = newHighScore
            }
        }
    }
}
