package com.rogatka.introgram.modals

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rogatka.introgram.FolderIcon

@Composable
fun NewFolderModal(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, icon: FolderIcon) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(FolderIcon.FOLDER) }

    val iconsMatrix = FolderIcon.entries.toList().chunked(4)

    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Как назовем папку?") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Введите название...") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                    )
                    Spacer(Modifier.height(16.dp))
                    iconsMatrix.forEach { row ->
                        Row(
                            Modifier.padding(bottom = 16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            row.forEach { icon ->
                                val color by animateColorAsState(
                                    targetValue = if (selectedIcon == icon)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    animationSpec = tween(durationMillis = 300),
                                    label = "BgColorAnimation"
                                )

                                IconButton(
                                    onClick = { selectedIcon = icon },
                                ) {
                                    Icon(icon.icon, null, tint = color)
                                }
                            }
                        }
                    }
                }

            },
            confirmButton = {
                TextButton(onClick = {onConfirm(name, selectedIcon)}) {
                    Text("Создать папку")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Назад")
                }
            }
        )
    }
}