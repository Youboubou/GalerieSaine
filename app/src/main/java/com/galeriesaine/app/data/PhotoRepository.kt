package com.galeriesaine.app.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.galeriesaine.app.model.Album
import com.galeriesaine.app.model.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Cette classe est responsable de récupérer les photos du téléphone.
 *
 * Elle utilise MediaStore, l'API OFFICIELLE Android pour accéder aux médias.
 * AUCUNE donnée n'est envoyée ailleurs : tout reste sur le téléphone.
 */
class PhotoRepository(private val context: Context) {

    /**
     * Récupère toutes les photos du téléphone, triées de la plus récente à la plus ancienne.
     */
    suspend fun getAllPhotos(): List<Photo> = withContext(Dispatchers.IO) {
        val photos = mutableListOf<Photo>()

        // Les colonnes qu'on veut récupérer pour chaque photo
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        // Trier par date d'ajout, décroissant (plus récent en premier)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        // Requête au système Android pour obtenir les photos
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val bucketColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                photos.add(
                    Photo(
                        id = id,
                        uri = uri,
                        displayName = cursor.getString(nameColumn) ?: "Sans nom",
                        dateAdded = cursor.getLong(dateColumn),
                        bucketName = cursor.getString(bucketColumn) ?: "Autre"
                    )
                )
            }
        }

        photos
    }

    /**
     * Regroupe les photos par album (dossier) et crée un objet Album pour chaque dossier.
     */
    suspend fun getAllAlbums(): List<Album> = withContext(Dispatchers.Default) {
        val photos = getAllPhotos()

        // Grouper les photos par nom de dossier
        photos.groupBy { it.bucketName }
            .map { (bucketName, photosInBucket) ->
                Album(
                    name = bucketName,
                    coverPhoto = photosInBucket.first(), // la photo la plus récente comme couverture
                    photoCount = photosInBucket.size
                )
            }
            .sortedByDescending { it.photoCount } // albums les plus remplis en premier
    }

    /**
     * Récupère uniquement les photos d'un album donné.
     */
    suspend fun getPhotosInAlbum(albumName: String): List<Photo> = withContext(Dispatchers.Default) {
        getAllPhotos().filter { it.bucketName == albumName }
    }
}
