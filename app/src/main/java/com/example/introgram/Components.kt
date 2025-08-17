package com.rogatka.introgram

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scale

@Composable
fun ChatAvatar(filename: String, size: Dp = 48.dp, modifier: Modifier = Modifier, loading: Boolean = false) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = Color.Gray,
    ) {
        val bitmap = loadBitmapFromFile(context = LocalContext.current, filename = filename)?.asImageBitmap()

        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loading) {
                CircularProgressIndicator()
            }
            if (bitmap != null) Image(
                bitmap = bitmap,
                modifier = Modifier.size(size),
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
            )
        }
    }
}

fun Bitmap.resizeToCover(targetWidth: Int, targetHeight: Int): Bitmap {
    // 1. Вычисляем соотношения сторон
    val sourceAspect = width.toFloat() / height.toFloat()
    val targetAspect = targetWidth.toFloat() / targetHeight.toFloat()

    // 2. Определяем масштаб (больший из двух вариантов)
    val scale = if (sourceAspect > targetAspect) {
        // Широкое изображение - масштабируем по высоте
        targetHeight.toFloat() / height.toFloat()
    } else {
        // Высокое изображение - масштабируем по ширине
        targetWidth.toFloat() / width.toFloat()
    }

    // 3. Создаем промежуточный Bitmap с масштабированием
    val scaledWidth = (width * scale).toInt()
    val scaledHeight = (height * scale).toInt()
    val scaledBitmap = this.scale(scaledWidth, scaledHeight)

    // 4. Вычисляем координаты обрезки (центрирование)
    val x = (scaledWidth - targetWidth) / 2
    val y = (scaledHeight - targetHeight) / 2

    // 5. Создаем итоговый Bitmap с обрезкой
    return Bitmap.createBitmap(
        scaledBitmap,
        x.coerceIn(0, scaledWidth - targetWidth),
        y.coerceIn(0, scaledHeight - targetHeight),
        targetWidth,
        targetHeight
    )
}