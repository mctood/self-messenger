package com.rogatka.introgram.modals

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import com.rogatka.introgram.Chat

@Composable
fun AboutModal(
    show: Boolean,
    onConfirm: () -> Unit
) {
    if (show) {
        AlertDialog(
            icon = {
                Text("\uD83D\uDC38")
            },
            onDismissRequest = onConfirm,
            title = { Text("О программке") },
            text = {
                Text("Сделано на коленке для записи заметок перед сном. Если у вас есть предложения по улучшению, пишите в Telegram: @rogatk")
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Ква")
                }
            }
        )
    }
}