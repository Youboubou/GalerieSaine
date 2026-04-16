package com.galeriesaine.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.galeriesaine.app.data.GalerieViewModel
import com.galeriesaine.app.data.ShareHelper
import com.galeriesaine.app.model.Photo
import com.galeriesaine.app.ui.components.PhotoGridItem

/**
 * Écran qui affiche toutes les photos d'un album (dossier) spécifique.
 * Supporte aussi la sélection multiple (appui long) pour le partage groupé.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumName: String,
    viewModel: GalerieViewModel = viewModel(),
    onBack: () -> Unit,
    onPhotoClick: (Long) -> Unit
) {
    var photos by remember { mutableStateOf<List<Photo>>(emptyList()) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val selectionMode = selectedIds.isNotEmpty()
    val context = LocalContext.current

    // Quitte le mode sélection avec le bouton retour Android
    BackHandler(enabled = selectionMode) {
        selectedIds = emptySet()
    }

    // Charger les photos de l'album
    LaunchedEffect(albumName) {
        photos = viewModel.getPhotosInAlbum(albumName)
    }

    val toggleSelection: (Long) -> Unit = { id ->
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    Scaffold(
        topBar = {
            if (selectionMode) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (selectedIds.size == 1) "1 photo sélectionnée"
                            else "${selectedIds.size} photos sélectionnées",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectedIds = emptySet() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Annuler la sélection")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            val urisToShare = photos
                                .filter { it.id in selectedIds }
                                .map { it.uri }
                            ShareHelper.sharePhotos(context, urisToShare)
                            selectedIds = emptySet()
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = "Partager")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            } else {
                TopAppBar(
                    title = { Text(albumName) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour"
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(2.dp)
        ) {
            items(photos, key = { it.id }) { photo ->
                PhotoGridItem(
                    photo = photo,
                    isSelected = photo.id in selectedIds,
                    modifier = Modifier.padding(1.dp),
                    onClick = {
                        if (selectionMode) toggleSelection(photo.id)
                        else onPhotoClick(photo.id)
                    },
                    onLongClick = { toggleSelection(photo.id) }
                )
            }
        }
    }
}
