package com.galeriesaine.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.galeriesaine.app.model.UserAlbum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

// DataStore dédié aux albums utilisateur (séparé de celui des favoris)
private val Context.userAlbumsDataStore by preferencesDataStore(name = "user_albums")

/**
 * Gère les albums créés par l'utilisateur.
 *
 * Les albums sont stockés UNIQUEMENT en local sur le téléphone, sous forme
 * de JSON dans DataStore. Aucune communication réseau, aucun fichier n'est
 * déplacé — les albums sont "virtuels" (une liste d'IDs de photos).
 */
class UserAlbumRepository(private val context: Context) {

    private val albumsKey = stringPreferencesKey("albums_json")

    /**
     * Flux qui émet la liste des albums utilisateur.
     * Se met à jour automatiquement à chaque modification.
     */
    val albums: Flow<List<UserAlbum>> = context.userAlbumsDataStore.data
        .map { preferences ->
            val json = preferences[albumsKey] ?: return@map emptyList()
            parseAlbums(json)
        }

    /**
     * Crée un nouvel album vide avec le nom donné.
     * Retourne l'ID du nouvel album.
     */
    suspend fun createAlbum(name: String): String {
        val newId = "album_${System.currentTimeMillis()}"
        updateAlbums { current ->
            current + UserAlbum(id = newId, name = name.trim(), photoIds = emptySet())
        }
        return newId
    }

    /**
     * Renomme un album existant.
     */
    suspend fun renameAlbum(albumId: String, newName: String) {
        updateAlbums { current ->
            current.map { if (it.id == albumId) it.copy(name = newName.trim()) else it }
        }
    }

    /**
     * Supprime un album (ne supprime aucune photo, évidemment).
     */
    suspend fun deleteAlbum(albumId: String) {
        updateAlbums { current -> current.filterNot { it.id == albumId } }
    }

    /**
     * Ajoute une photo à un album (ne fait rien si elle y est déjà).
     */
    suspend fun addPhotoToAlbum(albumId: String, photoId: Long) {
        updateAlbums { current ->
            current.map {
                if (it.id == albumId) it.copy(photoIds = it.photoIds + photoId) else it
            }
        }
    }

    /**
     * Retire une photo d'un album.
     */
    suspend fun removePhotoFromAlbum(albumId: String, photoId: Long) {
        updateAlbums { current ->
            current.map {
                if (it.id == albumId) it.copy(photoIds = it.photoIds - photoId) else it
            }
        }
    }

    /**
     * Ajoute ou retire une photo d'un album (selon son état actuel).
     */
    suspend fun togglePhotoInAlbum(albumId: String, photoId: Long) {
        updateAlbums { current ->
            current.map {
                if (it.id == albumId) {
                    val newIds = if (photoId in it.photoIds) it.photoIds - photoId
                    else it.photoIds + photoId
                    it.copy(photoIds = newIds)
                } else it
            }
        }
    }

    // ────────── Helpers internes pour la (dé)sérialisation JSON ──────────

    private suspend fun updateAlbums(transform: (List<UserAlbum>) -> List<UserAlbum>) {
        context.userAlbumsDataStore.edit { preferences ->
            val current = preferences[albumsKey]?.let { parseAlbums(it) } ?: emptyList()
            preferences[albumsKey] = serializeAlbums(transform(current))
        }
    }

    private fun parseAlbums(json: String): List<UserAlbum> = try {
        val array = JSONArray(json)
        (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            val idsArray = obj.getJSONArray("photoIds")
            val ids = (0 until idsArray.length()).map { idsArray.getLong(it) }.toSet()
            UserAlbum(
                id = obj.getString("id"),
                name = obj.getString("name"),
                photoIds = ids
            )
        }
    } catch (_: Exception) {
        emptyList()
    }

    private fun serializeAlbums(albums: List<UserAlbum>): String {
        val array = JSONArray()
        albums.forEach { album ->
            val obj = JSONObject()
            obj.put("id", album.id)
            obj.put("name", album.name)
            obj.put("photoIds", JSONArray(album.photoIds.toList()))
            array.put(obj)
        }
        return array.toString()
    }
}
