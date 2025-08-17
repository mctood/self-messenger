package com.rogatka.introgram

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime

val gson = Gson()
val emptyChats: String = gson.toJson(Chats(chats = mutableListOf()))
fun randomUID(): Int {
    return (100000000..999999999).random()
}

suspend fun saveBitmapToFile(
    context: Context,
    bitmap: Bitmap,
    filename: String
): String = withContext(Dispatchers.IO) {
    val file = File(context.filesDir, filename)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    file.absolutePath
}



fun loadBitmapFromFile(context: Context, filename: String?): Bitmap? {
    if (filename == null) return null
    val filepath = File(context.filesDir, filename).path
    return try {
        BitmapFactory.decodeFile(filepath)
    } catch (e: Exception) {
        null
    }
}

fun deleteImageFile(context: Context, filename: String?): Boolean {
    if (filename == null) return false
    val filepath = File(context.filesDir, filename).path
    return try {
        val file = File(filepath)
        if (file.exists()) {
            file.delete()
        } else {
            false
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
        false
    }
}


data class Message(
    val id: Int,
    val content: String,
    val time: LocalDateTime,
)


data class Chat(
    var imagePath: String,
    val messages: MutableList<Message>,
    var name: String,
    val id: Int,
    var backgroundPath: String?
)

data class Chats(
    val chats: MutableList<Chat>
)

data class User(
    val username: String,
    val call: String
)

data class Folder(
    val name: String,
    val chats: Chats
)


fun writeJsonToFile(context: Context, filename: String, jsonData: String) {
    try {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use { stream ->
            stream.write(jsonData.toByteArray())
        }
    } catch (e: IOException) {
        throw IOException("Не удалось записать файл: ${e.message}")
    }
}

fun readJsonFromFile(context: Context, filename: String): String? {
    return try {
        context.openFileInput(filename).bufferedReader().use { it.readText() }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun getAllChatMessages(context: Context, chatId: Int): MutableList<Message>? {
    val chat = getChatByID(context, chatId)
    return chat?.messages
}

fun newMessage(context: Context, message: Message, chat: Chat?) {
    if (chat == null) return
    val chats: Chats = gson.fromJson(
        readJsonFromFile(context, "chats.json") ?: emptyChats,
        Chats::class.java
    )
    for (i in chats.chats.indices) {
        if (chats.chats[i].id == chat.id) {
            chats.chats[i].messages.add(0, message)
            break
        }
    }
    writeJsonToFile(context, "chats.json", gson.toJson(chats))
}

fun renameChat(context: Context, name: String, chat: Chat?) {
    if (chat == null) return
    val chats: Chats = gson.fromJson(
        readJsonFromFile(context, "chats.json") ?: emptyChats,
        Chats::class.java
    )
    for (i in chats.chats.indices) {
        if (chats.chats[i].id == chat.id) {
            chats.chats[i].name = name
            break
        }
    }
    writeJsonToFile(context, "chats.json", gson.toJson(chats))
}

fun changeChatPhoto(context: Context, chat: Chat?, imagePath: String) {
    if (chat == null) return
    val chats: Chats = gson.fromJson(
        readJsonFromFile(context, "chats.json") ?: emptyChats,
        Chats::class.java
    )
    for (i in chats.chats.indices) {
        if (chats.chats[i].id == chat.id) {
            chats.chats[i].imagePath = imagePath
            break
        }
    }
    writeJsonToFile(context, "chats.json", gson.toJson(chats))
}

fun changeChatBackground(context: Context, chat: Chat?, backgroundPath: String) {
    if (chat == null) return
    val chats: Chats = gson.fromJson(
        readJsonFromFile(context, "chats.json") ?: emptyChats,
        Chats::class.java
    )
    for (i in chats.chats.indices) {
        if (chats.chats[i].id == chat.id) {
            chats.chats[i].backgroundPath = backgroundPath
            break
        }
    }
    writeJsonToFile(context, "chats.json", gson.toJson(chats))
}

fun deleteChat(context: Context, chat: Chat) {
    val chats: Chats = gson.fromJson(
        readJsonFromFile(context, "chats.json") ?: emptyChats,
        Chats::class.java
    )
    for (i in chats.chats.indices) {
        if (chats.chats[i].id == chat.id) {
            chats.chats.removeAt(i)
            break
        }
    }
    writeJsonToFile(context, "chats.json", gson.toJson(chats))
}

fun getAllChats(context: Context): List<Chat> {
    val chats: Chats = gson.fromJson(readJsonFromFile(context, "chats.json") ?: emptyChats, Chats::class.java)
    return chats.chats
}

fun getChatByID(context: Context, id: Int): Chat? {
    val chats: Chats = gson.fromJson(readJsonFromFile(context, "chats.json") ?: emptyChats, Chats::class.java)
    for (chat in chats.chats) {
        if (chat.id == id) return chat
    }
    return null
}


fun addChat(context: Context, chat: Chat) {
    val chats: Chats = gson.fromJson(
        readJsonFromFile(context, "chats.json") ?: emptyChats,
        Chats::class.java
    )
    chats.chats.add(chat)
    writeJsonToFile(context, "chats.json", gson.toJson(chats))
}