package com.rogatka.introgram.modals

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmFolderDeleteModal(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Вы уверены?") },
            text = { Text("Чаты останутся нетронутыми, но будут перемещены в общую папку") },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Да будет так")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Отмена")
                }
            }
        )
    }
}