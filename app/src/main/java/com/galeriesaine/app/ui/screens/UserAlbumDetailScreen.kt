package com.galeriesaine.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.galeriesaine.app.data.GalerieViewModel
import com.galeriesaine.app.data.ShareHelper
import com.galeriesaine.app.ui.components.AlbumNameDialog
import com.galeriesaine.app.ui.components.DeleteAlbumConfirmDialog
import com.galeriesaine.app.ui.components.PhotoGridItem

/**
 * Écran qui affiche le contenu d'un album utilisateur (personnel).
 *
 * Permet :
 * - de voir les photos de l'album
 * - de les ouvrir en plein écran
 * - de sélectionner plusieurs photos (appui long) pour les partager ou les retirer
 * - de renommer ou supprimer l'album (menu ⋮)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAlbumDetailScreen(
    albumId: String,
    viewModel: GalerieViewModel = viewModel(),
    onBack: () -> Unit,
    onPhotoClick: (Long) -> Unit
) {
    val userAlbums by viewModel.userAlbums.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val context = LocalContext.current

    val album = userAlbums.firstOrNull { it.id == albumId }
    if (album == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    // Photos de cet album, dans l'ordre de la galerie
    val albumPhotos = remember(album.photoIds, photos) {
        photos.filter { it.id in album.photoIds }
    }

    // Mode sélection
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val selectionMode = selectedIds.isNotEmpty()
    BackHandler(enabled = selectionMode) { selectedIds = emptySet() }

    val toggleSelection: (Long) -> Unit = { id ->
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    // Dialogues
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

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
                        // Retirer de l'album (ne supprime pas les photos)
                        IconButton(onClick = {
                            selectedIds.forEach { id ->
                                viewModel.togglePhotoInUserAlbum(album.id, id)
                            }
                            selectedIds = emptySet()
                        }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Retirer de l'album"
                            )
                        }
                        // Partager
                        IconButton(onClick = {
                            val urisToShare = albumPhotos
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
                    title = { Text(album.name) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour"
                            )
                        }
                    },
                    actions = {
                        // Menu ⋮ pour renommer / supprimer
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Options")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Renommer") },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Edit, contentDescription = null)
                                    },
                                    onClick = {
                                        showMenu = false
                                        showRenameDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Supprimer l'album") },
                                    leadingIcon = {
                                        Icon(Icons.Filled.Delete, contentDescription = null)
                                    },
                                    onClick = {
                                        showMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        if (albumPhotos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cet album est vide.\nAjoute des photos depuis l'onglet Photos en les ouvrant en grand.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(2.dp)
            ) {
                items(albumPhotos, key = { it.id }) { photo ->
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

    // Dialogue de renommage
    if (showRenameDialog) {
        AlbumNameDialog(
            initialName = album.name,
            title = "Renommer l'album",
            confirmLabel = "Renommer",
            onConfirm = { newName -> viewModel.renameUserAlbum(album.id, newName) },
            onDismiss = { showRenameDialog = false }
        )
    }

    // Confirmation de suppression
    if (showDeleteDialog) {
        DeleteAlbumConfirmDialog(
            albumName = album.name,
            onConfirm = {
                viewModel.deleteUserAlbum(album.id)
                onBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
