package com.rogatka.introgram.modals

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rogatka.introgram.Message

@Composable
fun EditMessageModal(
    show: Boolean,
    message: Message?,
    onDismiss: () -> Unit,
    onConfirm: (message: Message) -> Unit
) {
    if (message == null) return
    var text by remember { mutableStateOf(message.content) }

    if (show) {
        AlertDialog(
            icon = { Icon(Icons.AutoMirrored.Default.Message, null) },
            onDismissRequest = onDismiss,
            title = { Text("Редактировать сообщение") },
            text = {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    placeholder = { Text("Введите текст...") },
                    maxLines = 20,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    message.content = text
                    onConfirm(message)
                }) {
                    Text("Сохранить")
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