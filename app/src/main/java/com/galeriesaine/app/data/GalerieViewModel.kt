package com.galeriesaine.app.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.galeriesaine.app.model.Album
import com.galeriesaine.app.model.Photo
import com.galeriesaine.app.model.UserAlbum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel : contient l'état de l'application et la logique.
 */
class GalerieViewModel(application: Application) : AndroidViewModel(application) {

    private val photoRepository = PhotoRepository(application)
    private val favoritesRepository = FavoritesRepository(application)
    private val userAlbumRepository = UserAlbumRepository(application)

    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos.asStateFlow()

    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val favoriteIds = favoritesRepository.favoriteIds

    // Albums utilisateur, toujours à jour via le flux DataStore
    val userAlbums: StateFlow<List<UserAlbum>> = userAlbumRepository.albums
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Charge toutes les photos et albums depuis le téléphone. */
    fun chargerPhotos() {
        viewModelScope.launch {
            _isLoading.value = true
            _photos.value = photoRepository.getAllPhotos()
            _albums.value = photoRepository.getAllAlbums()
            _isLoading.value = false
        }
    }

    suspend fun getPhotosInAlbum(albumName: String): List<Photo> {
        return photoRepository.getPhotosInAlbum(albumName)
    }

    fun toggleFavorite(photoId: Long) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(photoId) }
    }

    // ──────── Albums utilisateur ────────

    fun createUserAlbum(name: String) {
        viewModelScope.launch { userAlbumRepository.createAlbum(name) }
    }

    fun renameUserAlbum(albumId: String, newName: String) {
        viewModelScope.launch { userAlbumRepository.renameAlbum(albumId, newName) }
    }

    fun deleteUserAlbum(albumId: String) {
        viewModelScope.launch { userAlbumRepository.deleteAlbum(albumId) }
    }

    fun togglePhotoInUserAlbum(albumId: String, photoId: Long) {
        viewModelScope.launch { userAlbumRepository.togglePhotoInAlbum(albumId, photoId) }
    }

    /**
     * Retourne les objets Photo d'un album utilisateur, dans l'ordre où les
     * photos apparaissent dans la galerie (plus récent en premier).
     */
    fun getPhotosForUserAlbum(albumId: String): List<Photo> {
        val album = userAlbums.value.firstOrNull { it.id == albumId } ?: return emptyList()
        return _photos.value.filter { it.id in album.photoIds }
    }
}
