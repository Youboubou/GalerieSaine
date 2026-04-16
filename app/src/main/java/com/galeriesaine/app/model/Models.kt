package com.galeriesaine.app.model

import android.net.Uri

/**
 * Représente une photo du téléphone.
 *
 * @param id Identifiant unique de la photo dans le système Android
 * @param uri URI de la photo (permet de la charger/afficher)
 * @param displayName Nom du fichier (ex: "IMG_20240315.jpg")
 * @param dateAdded Date d'ajout (en secondes depuis 1970)
 * @param bucketName Nom du dossier/album qui contient la photo
 */
data class Photo(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateAdded: Long,
    val bucketName: String
)

/**
 * Représente un album (= un dossier contenant des photos).
 *
 * @param name Nom de l'album (ex: "DCIM", "Screenshots", "WhatsApp Images")
 * @param coverPhoto Première photo de l'album, utilisée comme miniature
 * @param photoCount Nombre de photos dans cet album
 */
data class Album(
    val name: String,
    val coverPhoto: Photo,
    val photoCount: Int
)

/**
 * Représente un album CRÉÉ PAR L'UTILISATEUR (album virtuel).
 *
 * Les photos ne sont pas déplacées : on mémorise juste leurs IDs.
 * Une même photo peut appartenir à plusieurs albums.
 *
 * @param id Identifiant unique de l'album (ex: "album_1712345678")
 * @param name Nom donné par l'utilisateur
 * @param photoIds IDs des photos du téléphone qui appartiennent à cet album
 */
data class UserAlbum(
    val id: String,
    val name: String,
    val photoIds: Set<Long>
)
