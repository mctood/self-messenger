package com.rogatka.introgram.modals

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmChatDeleteModal(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Вы уверены?") },
            text = { Text("Это действие необратимо. Вообще никак. Вы навсегда потеряете доступ к тому, что здесь написано.") },
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