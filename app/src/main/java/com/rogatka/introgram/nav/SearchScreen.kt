package com.rogatka.introgram.nav

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.rogatka.introgram.Chat
import com.rogatka.introgram.ChatItem
import com.rogatka.introgram.Message
import com.rogatka.introgram.MessageBox
import com.rogatka.introgram.getAllChats
import com.rogatka.introgram.topBarColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.mutableListOf

data class MessageWithChat(
    val message: Message,
    val chatId: Int
)

class SearchViewModel : ViewModel() {
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageWithChat>>(emptyList())
    val messages: StateFlow<List<MessageWithChat>> = _messages.asStateFlow()

    val loading = MutableStateFlow(false)

    private var searchJob: Job? = null

    fun search(query: String, allChats: List<Chat>) {
        searchJob?.cancel()

        searchJob = viewModelScope.launch(Dispatchers.IO) {
            if (query.isNotEmpty()) {
                loading.value = true
                val filteredChats: MutableList<Chat> = mutableListOf()
                val filteredMessages: MutableList<MessageWithChat> = mutableListOf()

                allChats.forEach { chat ->
                    if (chat.name.lowercase().contains(query.lowercase())) {
                        filteredChats.add(chat)
                    }
                    for (message in chat.messages) {
                        if (message.content.lowercase().contains(query.lowercase())) {
                            filteredMessages.add(MessageWithChat(message, chat.id))
                        }
                    }
                }

                // Обновляем состояние в основном потоке
                withContext(Dispatchers.Main) {
                    _chats.value = filteredChats
                    _messages.value = filteredMessages
                }
                loading.value = false
            } else {
                clearResults()
            }
        }
    }

    fun clearResults() {
        searchJob?.cancel()
        viewModelScope.launch {
            _chats.value = emptyList()
            _messages.value = emptyList()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, backFolder: Int) {
    val context = LocalContext.current
    val viewModel: SearchViewModel = viewModel()
    var query by remember { mutableStateOf("") }
    val allChats = remember { getAllChats(context) }

    val chats by viewModel.chats.collectAsState()
    val messages by viewModel.messages.collectAsState()

    val loading by viewModel.loading.collectAsState()

    LaunchedEffect(query) {
        if (query.isNotEmpty()) {
            viewModel.search(query, allChats)
        } else {
            viewModel.clearResults()
        }
    }

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
                actions = {
                    if (loading) CircularProgressIndicator()
                }
            )
        }
    ) { paddingValues ->
        if (query.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
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
                onValueChange = { query = it },
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
                    Column(
                        Modifier
                            .padding(top = 24.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            "Сообщения (${messages.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(16.dp)
                        )
                        messages.forEach { message ->
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                MessageBox(
                                    text = message.message.content.take(50),
                                    time = message.message.dateTime,
                                    inSearch = true,
                                    onTap = {
                                        navController.navigate("chat/${message.chatId}/${backFolder}/${message.message.id}")
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