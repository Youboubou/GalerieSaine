package com.galeriesaine.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.galeriesaine.app.data.GalerieViewModel
import com.galeriesaine.app.data.ShareHelper
import com.galeriesaine.app.ui.components.AddToAlbumDialog

// Bornes de zoom
private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 5f
private const val DOUBLE_TAP_ZOOM = 2.5f

/**
 * Écran de visualisation d'une photo en PLEIN ÉCRAN avec :
 * - Swipe horizontal pour changer de photo (désactivé quand zoomé)
 * - Pincement à deux doigts pour zoomer de 1x à 5x
 * - Pan à un doigt pour se déplacer dans la photo zoomée
 * - Double tap pour zoomer rapidement / revenir à la taille normale
 * - Tap simple pour masquer/afficher la barre du haut
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(
    photoId: Long,
    viewModel: GalerieViewModel = viewModel(),
    onBack: () -> Unit
) {
    val photos by viewModel.photos.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState(initial = emptySet())
    val userAlbums by viewModel.userAlbums.collectAsState()
    val context = LocalContext.current

    // Affichage du dialogue "Ajouter à un album"
    var showAddToAlbumDialog by remember { mutableStateOf(false) }

    val initialIndex = remember(photoId, photos) {
        photos.indexOfFirst { it.id == photoId }.coerceAtLeast(0)
    }

    if (photos.isEmpty()) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { photos.size }
    )

    val currentPhoto = photos.getOrNull(pagerState.currentPage)
    val isFavorite = currentPhoto?.let { it.id in favoriteIds } ?: false

    var barVisible by rememberSaveable { mutableStateOf(true) }

    // Indique si la photo actuelle est zoomée (désactive le swipe entre photos)
    var currentPhotoZoomed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            // Désactive le swipe horizontal entre photos quand on est zoomé
            userScrollEnabled = !currentPhotoZoomed,
            key = { photos[it].id }
        ) { pageIndex ->
            val photo = photos[pageIndex]
            // Chaque page recycle son propre zoom : si on swipe, la nouvelle
            // photo démarre à 1x (le remember { mutableFloatStateOf(1f) } est
            // réinitialisé à chaque nouvelle page du pager).
            ZoomablePhoto(
                photoUri = photo.uri,
                contentDescription = photo.displayName,
                onTap = { barVisible = !barVisible },
                onZoomChange = { zoomed ->
                    // On ne met à jour currentPhotoZoomed que pour la page actuelle
                    if (pageIndex == pagerState.currentPage) {
                        currentPhotoZoomed = zoomed
                    }
                }
            )
        }

        // BARRE DU HAUT (retour + nom + favori + partager)
        AnimatedVisibility(
            visible = barVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = currentPhoto?.displayName ?: "",
                        color = Color.White,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { currentPhoto?.let { viewModel.toggleFavorite(it.id) } }
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (isFavorite) Color.Red else Color.White
                        )
                    }
                    // Bouton "Ajouter à un album"
                    IconButton(
                        onClick = { if (currentPhoto != null) showAddToAlbumDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CreateNewFolder,
                            contentDescription = "Ajouter à un album",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { currentPhoto?.let { ShareHelper.sharePhoto(context, it.uri) } }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Partager",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f)
                )
            )
        }
    }

    // Dialogue "Ajouter à un album"
    if (showAddToAlbumDialog && currentPhoto != null) {
        AddToAlbumDialog(
            photoId = currentPhoto.id,
            albums = userAlbums,
            onToggle = { albumId ->
                viewModel.togglePhotoInUserAlbum(albumId, currentPhoto.id)
            },
            onCreateNew = { name -> viewModel.createUserAlbum(name) },
            onDismiss = { showAddToAlbumDialog = false }
        )
    }
}

/**
 * Une photo sur laquelle l'utilisateur peut zoomer (pincement) et se déplacer.
 *
 * - Pincement à deux doigts → change l'échelle (scale)
 * - Glissement à un doigt quand zoomé → déplace la photo (pan)
 * - Double tap → zoom rapide / retour à la normale
 * - Tap simple → appelle onTap (masquer/afficher les barres)
 *
 * Les gestes de pan sont "absorbés" par cette photo quand elle est zoomée,
 * ce qui empêche le HorizontalPager parent de changer de photo en même temps.
 */
@Composable
private fun ZoomablePhoto(
    photoUri: android.net.Uri,
    contentDescription: String,
    onTap: () -> Unit,
    onZoomChange: (Boolean) -> Unit
) {
    // État du zoom et de la position (survivent à une rotation d'écran)
    var scale by rememberSaveable { mutableFloatStateOf(1f) }
    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }

    // Animation douce pour le double-tap
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        label = "scale"
    )
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        label = "offsetX"
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        label = "offsetY"
    )

    // Notifie le parent quand on zoome/dézoome
    LaunchedEffect(scale) {
        onZoomChange(scale > 1f)
    }

    // Fonction utilitaire : remet le zoom à la normale
    val resetZoom = {
        scale = 1f
        offsetX = 0f
        offsetY = 0f
    }

    AsyncImage(
        model = photoUri,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = animatedScale,
                scaleY = animatedScale,
                translationX = animatedOffsetX,
                translationY = animatedOffsetY
            )
            // Gestion du tap simple et du double-tap
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = {
                        if (scale > 1f) {
                            resetZoom()
                        } else {
                            scale = DOUBLE_TAP_ZOOM
                        }
                    }
                )
            }
            // Gestion du pincement (zoom) et du pan (déplacement)
            //
            // ⚠️ On utilise une boucle manuelle plutôt que detectTransformGestures
            // pour CHOISIR de consommer ou non l'événement :
            // - pincement (2 doigts) → on consomme toujours (pour zoomer)
            // - glissement 1 doigt quand zoomé → on consomme (pour pan)
            // - glissement 1 doigt quand pas zoomé → on NE consomme PAS,
            //   laissant le HorizontalPager parent recevoir le swipe
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pointerCount = event.changes.count { it.pressed }

                        // Calcule le zoom (1.0 si un seul doigt)
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()

                        if (pointerCount >= 2) {
                            // Deux doigts ou plus : pincement → on zoome
                            val newScale = (scale * zoomChange).coerceIn(MIN_ZOOM, MAX_ZOOM)

                            if (newScale > 1f) {
                                val maxOffsetX = (size.width * (newScale - 1)) / 2f
                                val maxOffsetY = (size.height * (newScale - 1)) / 2f
                                offsetX = (offsetX + panChange.x).coerceIn(-maxOffsetX, maxOffsetX)
                                offsetY = (offsetY + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                            scale = newScale

                            // On consomme les événements pour que le pager ne les reçoive pas
                            event.changes.forEach { it.consume() }
                        } else if (pointerCount == 1 && scale > 1f) {
                            // Un doigt et photo zoomée : on déplace la photo (pan)
                            val maxOffsetX = (size.width * (scale - 1)) / 2f
                            val maxOffsetY = (size.height * (scale - 1)) / 2f
                            offsetX = (offsetX + panChange.x).coerceIn(-maxOffsetX, maxOffsetX)
                            offsetY = (offsetY + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)

                            event.changes.forEach { it.consume() }
                        }
                        // Sinon (un doigt, pas zoomé) : on ne consomme rien
                        // → le swipe est transmis au HorizontalPager
                    }
                }
            }
    )
}
