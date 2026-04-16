package com.galeriesaine.app

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.galeriesaine.app.ui.screens.AlbumDetailScreen
import com.galeriesaine.app.ui.screens.MainScreen
import com.galeriesaine.app.ui.screens.PermissionScreen
import com.galeriesaine.app.ui.screens.PhotoDetailScreen
import com.galeriesaine.app.ui.screens.UserAlbumDetailScreen
import com.galeriesaine.app.ui.theme.GalerieSaineTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * MainActivity : la seule Activity de l'application.
 * Avec Jetpack Compose, on n'a besoin que d'une seule Activity,
 * toute la navigation se fait en interne.
 */
class MainActivity : ComponentActivity() {

    // URI d'une photo ouverte depuis une autre app (appareil photo, fichiers, etc.)
    // Exposé en State Compose pour que l'UI puisse réagir si l'intent change à chaud.
    private var incomingPhotoUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)
        setContent {
            GalerieSaineTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    GalerieApp(incomingPhotoUri = incomingPhotoUri)
                }
            }
        }
    }

    /**
     * Si l'utilisateur ouvre GalerieSaine en touchant la vignette après
     * avoir pris une photo (ou depuis l'explorateur de fichiers), Android
     * peut relancer l'Activity existante. On met à jour l'URI ici.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_VIEW) {
            incomingPhotoUri = intent.data
        }
    }
}

/**
 * Convertit une URI entrante (reçue via ACTION_VIEW) en ID MediaStore.
 *
 * Les URIs peuvent venir sous plusieurs formes selon l'app source :
 * - content://media/external/images/media/12345 → on lit directement l'ID
 * - content://... (autres) → on interroge MediaStore via une requête de correspondance
 * Retourne null si on ne peut pas faire le lien avec une photo de MediaStore.
 */
private fun resolvePhotoIdFromUri(context: android.content.Context, uri: Uri): Long? {
    // Cas simple : URI MediaStore directe, l'ID est le dernier segment
    try {
        val id = ContentUris.parseId(uri)
        if (id > 0) return id
    } catch (_: Exception) {
        // L'URI n'est pas du format attendu, on essaie la requête ci-dessous
    }

    // Cas général : on demande à MediaStore l'ID correspondant à cette URI
    return runCatching {
        context.contentResolver.query(
            uri,
            arrayOf(MediaStore.Images.Media._ID),
            null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idCol = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                if (idCol >= 0) cursor.getLong(idCol) else null
            } else null
        }
    }.getOrNull()
}

/**
 * Composant racine de l'application.
 * Gère la permission d'accès aux photos et la navigation.
 *
 * @param incomingPhotoUri URI d'une photo ouverte depuis une autre app (optionnel).
 *   Si non-null, on ouvre directement cette photo en plein écran.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GalerieApp(incomingPhotoUri: Uri? = null) {
    // Liste des permissions à demander selon la version d'Android
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        // Android 13+ : permission granulaire pour les images
        listOf(android.Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        // Android 12 et moins : ancienne permission
        listOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val permissionsState = rememberMultiplePermissionsState(permissions)

    if (permissionsState.permissions.all { it.status.isGranted }) {
        // Permission accordée : on affiche l'app
        GalerieNavigation(incomingPhotoUri = incomingPhotoUri)
    } else {
        // Permission refusée ou pas encore demandée : écran d'explication
        PermissionScreen(
            onRequestPermission = { permissionsState.launchMultiplePermissionRequest() }
        )
    }
}

/**
 * Gère la navigation entre les différents écrans de l'app.
 *
 * ⚠️ IMPORTANT : on crée UN SEUL ViewModel partagé entre tous les écrans
 * (scopé à l'Activity), pour que la liste des photos déjà chargées
 * soit disponible quand on ouvre l'écran détail d'une photo.
 */
@Composable
fun GalerieNavigation(incomingPhotoUri: Uri? = null) {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current

    // ViewModel partagé entre tous les écrans
    val sharedViewModel: com.galeriesaine.app.data.GalerieViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()

    // Si on a été lancés avec une URI entrante, on charge les photos
    // puis on navigue directement vers l'écran plein écran de la photo correspondante.
    LaunchedEffect(incomingPhotoUri) {
        if (incomingPhotoUri != null) {
            sharedViewModel.chargerPhotos()
            val photoId = resolvePhotoIdFromUri(context, incomingPhotoUri)
            if (photoId != null) {
                navController.navigate("photo/$photoId") {
                    // Évite d'empiler plusieurs fois le même écran si on reçoit
                    // plusieurs intents à la suite.
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = "main") {
        // Écran principal avec les 3 onglets
        composable("main") {
            MainScreen(
                viewModel = sharedViewModel,
                onPhotoClick = { photoId ->
                    navController.navigate("photo/$photoId")
                },
                onAlbumClick = { albumName ->
                    val encoded = java.net.URLEncoder.encode(albumName, "UTF-8")
                    navController.navigate("album/$encoded")
                },
                onUserAlbumClick = { albumId ->
                    navController.navigate("user_album/$albumId")
                }
            )
        }

        // Écran détail d'une photo
        composable("photo/{photoId}") { backStackEntry ->
            val photoId = backStackEntry.arguments?.getString("photoId")?.toLongOrNull() ?: return@composable
            PhotoDetailScreen(
                photoId = photoId,
                viewModel = sharedViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // Écran détail d'un dossier du téléphone
        composable("album/{albumName}") { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("albumName") ?: return@composable
            val albumName = java.net.URLDecoder.decode(encoded, "UTF-8")
            AlbumDetailScreen(
                albumName = albumName,
                viewModel = sharedViewModel,
                onBack = { navController.popBackStack() },
                onPhotoClick = { photoId ->
                    navController.navigate("photo/$photoId")
                }
            )
        }

        // Écran détail d'un album personnel
        composable("user_album/{albumId}") { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: return@composable
            UserAlbumDetailScreen(
                albumId = albumId,
                viewModel = sharedViewModel,
                onBack = { navController.popBackStack() },
                onPhotoClick = { photoId ->
                    navController.navigate("photo/$photoId")
                }
            )
        }
    }
}
