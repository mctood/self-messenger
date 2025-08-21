package com.rogatka.introgram.modals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
@Composable
fun AboutModal(
    show: Boolean,
    onConfirm: () -> Unit
) {
    val context = LocalContext.current
    val versionName = remember {
        try {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
        } catch (e: Exception) {
            "N/A"
        }
    }

    if (show) {

        AlertDialog(
            icon = {
                Text("\uD83D\uDC38")
            },
            onDismissRequest = onConfirm,
            title = { Text("О программке") },
            text = {
                Column {
                    Text("Версия $versionName")
                    Spacer(Modifier.height(16.dp))
                    Text("Сделано на коленке для записи заметок перед сном. Если у вас есть предложения по улучшению, пишите в Telegram: @rogatk")
                }

            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Ква")
                }
            }
        )
    }
}