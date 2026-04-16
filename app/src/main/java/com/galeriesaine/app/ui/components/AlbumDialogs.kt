package com.galeriesaine.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.galeriesaine.app.model.UserAlbum

/**
 * Dialogue de création (ou renommage) d'un album personnel.
 *
 * @param initialName Nom par défaut dans le champ (vide pour la création)
 * @param title Titre du dialogue
 * @param confirmLabel Libellé du bouton principal
 */
@Composable
fun AlbumNameDialog(
    initialName: String = "",
    title: String,
    confirmLabel: String = "Créer",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom de l'album") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = name.trim()
                    if (trimmed.isNotEmpty()) {
                        onConfirm(trimmed)
                        onDismiss()
                    }
                },
                enabled = name.trim().isNotEmpty()
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

/**
 * Dialogue qui liste tous les albums de l'utilisateur avec des cases à cocher
 * pour ajouter/retirer rapidement une photo de plusieurs albums à la fois.
 *
 * Un bouton "Nouvel album" permet d'en créer un à la volée.
 */
@Composable
fun AddToAlbumDialog(
    photoId: Long,
    albums: List<UserAlbum>,
    onToggle: (albumId: String) -> Unit,
    onCreateNew: (name: String) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter à un album") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (albums.isEmpty()) {
                    Text(
                        text = "Tu n'as pas encore d'album personnel. Clique sur « Nouvel album » pour en créer un.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    albums.forEach { album ->
                        val isIn = photoId in album.photoIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isIn,
                                onCheckedChange = { onToggle(album.id) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(album.name, fontWeight = FontWeight.Medium)
                                Text(
                                    text = "${album.photoIds.size} photo(s)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("+ Nouvel album")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Terminé") }
        }
    )

    if (showCreateDialog) {
        AlbumNameDialog(
            title = "Nouvel album",
            confirmLabel = "Créer",
            onConfirm = { newName ->
                onCreateNew(newName)
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

/**
 * Dialogue de confirmation de suppression d'un album.
 */
@Composable
fun DeleteAlbumConfirmDialog(
    albumName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Supprimer l'album ?") },
        text = {
            Text(
                "L'album « $albumName » sera supprimé. Les photos qu'il contient " +
                    "ne seront PAS supprimées et resteront dans ta galerie."
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                }
            ) {
                Text("Supprimer", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}
