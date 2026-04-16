package com.galeriesaine.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.galeriesaine.app.model.Album
import com.galeriesaine.app.model.Photo

/**
 * Vignette d'une photo dans la grille.
 *
 * Supporte :
 * - clic simple (onClick)
 * - appui long (onLongClick) → active la sélection multiple
 * - état sélectionné (affiche une coche et assombrit la photo)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoGridItem(
    photo: Photo,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Applique un léger rétrécissement si sélectionné (effet visuel)
        val imageModifier = if (isSelected) {
            Modifier
                .fillMaxSize()
                .padding(10.dp)
                .clip(RoundedCornerShape(4.dp))
        } else {
            Modifier.fillMaxSize()
        }

        AsyncImage(
            model = photo.uri,
            contentDescription = photo.displayName,
            contentScale = ContentScale.Crop,
            modifier = imageModifier
        )

        // Coche en haut à droite si sélectionné
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Sélectionnée",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

/**
 * Carte d'un album PERSONNEL (créé par l'utilisateur).
 *
 * Affiche la couverture (1re photo trouvée dans la liste globale), le nom
 * et le nombre de photos. Si l'album est vide, affiche une icône de dossier.
 */
@Composable
fun UserAlbumGridItem(
    userAlbum: com.galeriesaine.app.model.UserAlbum,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    // Récupère une photo de couverture depuis l'état partagé du ViewModel
    val viewModel: com.galeriesaine.app.data.GalerieViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()
    val allPhotos by viewModel.photos.collectAsState()
    val coverPhoto = remember(userAlbum.photoIds, allPhotos) {
        allPhotos.firstOrNull { it.id in userAlbum.photoIds }
    }

    Column(modifier = modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (coverPhoto != null) {
                AsyncImage(
                    model = coverPhoto.uri,
                    contentDescription = userAlbum.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
            // Badge du nombre de photos
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${userAlbum.photoIds.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = userAlbum.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
/**
 * Carte d'un album du téléphone : photo de couverture + nom + nombre de photos.
 */
@Composable
fun AlbumGridItem(
    album: Album,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(modifier = modifier.clickable(onClick = onClick)) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = album.coverPhoto.uri,
                contentDescription = album.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Badge avec le nombre de photos
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${album.photoCount}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = album.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
