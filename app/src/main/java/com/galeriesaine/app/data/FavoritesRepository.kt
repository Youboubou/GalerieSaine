package com.galeriesaine.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Crée un DataStore (stockage local clé-valeur) nommé "favoris"
private val Context.favoritesDataStore by preferencesDataStore(name = "favoris")

/**
 * Gère la liste des photos mises en favori.
 *
 * Les favoris sont stockés UNIQUEMENT en local sur le téléphone,
 * via DataStore (API Android officielle). Aucune synchronisation en ligne.
 */
class FavoritesRepository(private val context: Context) {

    private val favoritesKey = stringSetPreferencesKey("photo_favorites")

    /**
     * Flux qui émet l'ensemble des IDs de photos favorites.
     * Se met à jour automatiquement quand on ajoute/retire un favori.
     */
    val favoriteIds: Flow<Set<Long>> = context.favoritesDataStore.data
        .map { preferences ->
            preferences[favoritesKey]?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()
        }

    /**
     * Ajoute ou retire une photo des favoris.
     */
    suspend fun toggleFavorite(photoId: Long) {
        context.favoritesDataStore.edit { preferences ->
            val current = preferences[favoritesKey] ?: emptySet()
            val idString = photoId.toString()
            preferences[favoritesKey] = if (idString in current) {
                current - idString
            } else {
                current + idString
            }
        }
    }
}
