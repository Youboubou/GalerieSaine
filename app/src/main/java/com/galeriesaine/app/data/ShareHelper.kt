package com.galeriesaine.app.data

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Gère le partage de photos vers d'autres applications.
 *
 * ╔════════════════════════════════════════════════════════════════╗
 * ║  IMPORTANT : Cette fonction utilise ACTION_SEND, le mécanisme  ║
 * ║  NATIF Android. L'app ne communique avec AUCUN service tiers.  ║
 * ║  C'est Android qui affiche la liste des apps disponibles       ║
 * ║  (WhatsApp, Telegram, Gmail, etc.) et qui leur transmet        ║
 * ║  directement la photo.                                         ║
 * ╚════════════════════════════════════════════════════════════════╝
 */
object ShareHelper {

    /**
     * Partage une seule photo.
     */
    fun sharePhoto(context: Context, photoUri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, photoUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Ouvre le sélecteur Android "Partager via..."
        val chooser = Intent.createChooser(intent, "Partager la photo")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Partage plusieurs photos à la fois.
     */
    fun sharePhotos(context: Context, photoUris: List<Uri>) {
        if (photoUris.isEmpty()) return
        if (photoUris.size == 1) {
            sharePhoto(context, photoUris.first())
            return
        }

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(photoUris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Partager les photos")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
