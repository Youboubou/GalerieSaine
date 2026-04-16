package com.galeriesaine.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Folder
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
import com.galeriesaine.app.model.Album
import com.galeriesaine.app.model.Photo
import com.galeriesaine.app.model.UserAlbum
import com.galeriesaine.app.ui.components.AlbumGridItem
import com.galeriesaine.app.ui.components.AlbumNameDialog
import com.galeriesaine.app.ui.components.PhotoGridItem
import com.galeriesaine.app.ui.components.UserAlbumGridItem

/**
 * Écran principal de l'app avec 3 onglets : Photos / Albums / Favoris.
 *
 * Supporte également le MODE SÉLECTION MULTIPLE :
 * - Appui long sur une photo → entre en mode sélection
 * - Taps suivants → ajoutent / retirent des photos à la sélection
 * - Bouton partager en haut → partage groupé via intent Android natif
 * - Bouton croix ou "retour arrière" → quitte le mode sélection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: GalerieViewModel = viewModel(),
    onPhotoClick: (Long) -> Unit,
    onAlbumClick: (String) -> Unit,
    onUserAlbumClick: (String) -> Unit
) {
    val photos by viewModel.photos.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val userAlbums by viewModel.userAlbums.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState(initial = emptySet())
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) }

    // État de la sélection multiple : ensemble des IDs de photos sélectionnées
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val selectionMode = selectedIds.isNotEmpty()

    // Quitte le mode sélection avec le bouton retour Android
    BackHandler(enabled = selectionMode) {
        selectedIds = emptySet()
    }

    // Quand on change d'onglet, on sort du mode sélection
    LaunchedEffect(selectedTab) { selectedIds = emptySet() }

    // Charger les photos au démarrage
    LaunchedEffect(Unit) {
        if (photos.isEmpty()) viewModel.chargerPhotos()
    }

    // Fonction qui bascule la sélection d'une photo
    val toggleSelection: (Long) -> Unit = { id ->
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    Scaffold(
        topBar = {
            if (selectionMode) {
                // Barre spéciale du mode sélection
                SelectionTopBar(
                    count = selectedIds.size,
                    onClose = { selectedIds = emptySet() },
                    onShare = {
                        val urisToShare = photos
                            .filter { it.id in selectedIds }
                            .map { it.uri }
                        ShareHelper.sharePhotos(context, urisToShare)
                        selectedIds = emptySet()
                    }
                )
            } else {
                TopAppBar(title = { Text("GalerieSaine", fontWeight = FontWeight.Bold) })
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Les onglets ne s'affichent pas en mode sélection (pour éviter les gestes confus)
            if (!selectionMode) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Photos") },
                        icon = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Albums") },
                        icon = { Icon(Icons.Outlined.Folder, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Favoris") },
                        icon = { Icon(Icons.Filled.Favorite, contentDescription = null) }
                    )
                }
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> PhotoGrid(
                        photos = photos,
                        selectedIds = selectedIds,
                        selectionMode = selectionMode,
                        onPhotoClick = onPhotoClick,
                        onToggleSelection = toggleSelection
                    )
                    1 -> AlbumGrid(
                        systemAlbums = albums,
                        userAlbums = userAlbums,
                        onAlbumClick = onAlbumClick,
                        onUserAlbumClick = onUserAlbumClick,
                        onCreateAlbum = { name -> viewModel.createUserAlbum(name) }
                    )
                    2 -> {
                        val favorites = photos.filter { it.id in favoriteIds }
                        if (favorites.isEmpty()) {
                            EmptyState("Aucun favori pour l'instant.\nAppuie sur ❤️ dans une photo pour l'ajouter.")
                        } else {
                            PhotoGrid(
                                photos = favorites,
                                selectedIds = selectedIds,
                                selectionMode = selectionMode,
                                onPhotoClick = onPhotoClick,
                                onToggleSelection = toggleSelection
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Barre du haut affichée en mode sélection multiple.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionTopBar(
    count: Int,
    onClose: () -> Unit,
    onShare: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = if (count == 1) "1 photo sélectionnée" else "$count photos sélectionnées",
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Annuler la sélection")
            }
        },
        actions = {
            IconButton(onClick = onShare) {
                Icon(Icons.Filled.Share, contentDescription = "Partager")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    )
}

/**
 * Grille de photos avec support du mode sélection.
 * - Tap normal : ouvre la photo en plein écran (sauf si mode sélection actif)
 * - Tap en mode sélection : ajoute/retire la photo de la sélection
 * - Appui long : active le mode sélection
 */
@Composable
private fun PhotoGrid(
    photos: List<Photo>,
    selectedIds: Set<Long>,
    selectionMode: Boolean,
    onPhotoClick: (Long) -> Unit,
    onToggleSelection: (Long) -> Unit
) {
    if (photos.isEmpty()) {
        EmptyState("Aucune photo trouvée.")
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(2.dp)
    ) {
        items(photos, key = { it.id }) { photo ->
            PhotoGridItem(
                photo = photo,
                isSelected = photo.id in selectedIds,
                modifier = Modifier.padding(1.dp),
                onClick = {
                    if (selectionMode) onToggleSelection(photo.id)
                    else onPhotoClick(photo.id)
                },
                onLongClick = { onToggleSelection(photo.id) }
            )
        }
    }
}

@Composable
private fun AlbumGrid(
    systemAlbums: List<Album>,
    userAlbums: List<UserAlbum>,
    onAlbumClick: (String) -> Unit,
    onUserAlbumClick: (String) -> Unit,
    onCreateAlbum: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        // ─── Section : albums personnels ───
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader(
                title = "Mes albums",
                actionLabel = "+ Nouveau",
                onActionClick = { showCreateDialog = true }
            )
        }

        if (userAlbums.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Pas encore d'album perso. Crée-en un avec « + Nouveau » !",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(userAlbums, key = { "user_${it.id}" }) { userAlbum ->
                UserAlbumGridItem(
                    userAlbum = userAlbum,
                    modifier = Modifier.padding(4.dp),
                    onClick = { onUserAlbumClick(userAlbum.id) }
                )
            }
        }

        // ─── Section : albums du téléphone ───
        if (systemAlbums.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(title = "Dossiers du téléphone")
            }
            items(systemAlbums, key = { "sys_${it.name}" }) { album ->
                AlbumGridItem(
                    album = album,
                    modifier = Modifier.padding(4.dp),
                    onClick = { onAlbumClick(album.name) }
                )
            }
        }
    }

    // Dialogue de création
    if (showCreateDialog) {
        AlbumNameDialog(
            title = "Nouvel album",
            confirmLabel = "Créer",
            onConfirm = { newName -> onCreateAlbum(newName) },
            onDismiss = { showCreateDialog = false }
        )
    }
}

/**
 * En-tête d'une section dans la grille d'albums, avec bouton d'action optionnel.
 */
@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null) {
            TextButton(onClick = onActionClick) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
