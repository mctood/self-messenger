package com.rogatka.introgram.modals

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.rogatka.introgram.Folder
import com.rogatka.introgram.getAllFolders

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveToFolderModal(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (selectedFolder: Folder?) -> Unit
) {
    val context = LocalContext.current
    val folders: List<Folder> = getAllFolders(context)

    var expandedFolderSelect by remember { mutableStateOf(false) }
    var selectedFolder by remember {
        mutableStateOf<Folder?>(null)
    }

    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Куда переместить?") },
            text = {
                ExposedDropdownMenuBox(
                    expanded = expandedFolderSelect,
                    onExpandedChange = { expandedFolderSelect = !expandedFolderSelect },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField( // Используем OutlinedTextField для консистентности
                        value = selectedFolder?.name ?: "Без папки", // Отображаем имя выбранной папки или "Без папки"
                        onValueChange = {}, // onValueChange здесь не нужен, т.к. выбор через меню
                        readOnly = true,
                        label = { Text("Выберите папку") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFolderSelect)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedFolderSelect,
                        onDismissRequest = { expandedFolderSelect = false }
                    ) {
                        // Опция "Без папки"
                        DropdownMenuItem(
                            text = { Text("Без папки") },
                            onClick = {
                                selectedFolder = null
                                expandedFolderSelect = false
                            }
                        )
                        // Опции для каждой папки
                        folders.forEach { folder ->
                            DropdownMenuItem(
                                text = { Text(folder.name) },
                                onClick = {
                                    selectedFolder = folder
                                    expandedFolderSelect = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {onConfirm(selectedFolder)}) {
                    Text("Переместить")
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