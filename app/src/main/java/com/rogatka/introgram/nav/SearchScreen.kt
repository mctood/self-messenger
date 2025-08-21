package com.rogatka.introgram.nav

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rogatka.introgram.Chat
import com.rogatka.introgram.ChatItem
import com.rogatka.introgram.Message
import com.rogatka.introgram.MessageBox
import com.rogatka.introgram.getAllChats
import com.rogatka.introgram.topBarColors
import kotlin.collections.mutableListOf


data class MessageWithChat(
    val message: Message,
    val chatId: Int
)


fun getSearchResults(
    context: Context,
    query: String,
    messages: MutableList<MessageWithChat>,
    chats: MutableList<Chat>,
) {
    Log.e("Search", "Searching! ----------------------------------------")
    chats.clear()
    if (query.isEmpty()) return

    val allChats = getAllChats(context)
    allChats.forEach { chat ->
        if (chat.name.lowercase().contains(query.lowercase())) {
            chats.add(chat)

            for (message in chat.messages) {
                if (message.content.lowercase().contains(query.lowercase())) {
                    messages.add(MessageWithChat(message, chat.id))
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, backFolder: Int) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val messages = remember { mutableListOf<MessageWithChat>() }
    val chats = remember { mutableListOf<Chat>() }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topBarColors(),
                title = {
                    Text(
                        "Поиск",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("main/${backFolder}") }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Menu"
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        if (query.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Тут ничего нет \uD83D\uDC38",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Text(
                    "Введите свой запрос в поле выше.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
        Column(modifier = Modifier.padding(paddingValues)) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                shape = RectangleShape,
                value = query,
                onValueChange = {
                    query = it
                    getSearchResults(
                        context = context,
                        chats = chats,
                        messages = messages,
                        query = query
                    )
                },
                placeholder = { Text("Введите запрос") },
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = topBarColors().containerColor,
                    unfocusedContainerColor = topBarColors().containerColor,
                )
            )
            Column {
                if (chats.isNotEmpty()) {
                    Column {
                        Text(
                            "Чаты (${chats.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                        chats.forEach { chat ->
                            ChatItem(
                                chat = chat,
                                shareMode = false,
                                sharedText = null,
                                navController = navController,
                                folderId = backFolder
                            )
                        }
                    }
                }
                if (messages.isNotEmpty()) {
                    Column(Modifier.padding(top = 24.dp).fillMaxWidth()) {
                        Text(
                            "Сообщения (${messages.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                        messages.forEach { message ->
                            Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalAlignment = Alignment.End) {
                                MessageBox (
                                    text = message.message.content.take(50),
                                    time = message.message.dateTime,
                                    inSearch = true,
                                    onTap =  {
                                        navController.navigate("chat/${message.chatId}/${backFolder}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}