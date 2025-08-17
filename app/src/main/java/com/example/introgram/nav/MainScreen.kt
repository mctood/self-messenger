package com.rogatka.introgram.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rogatka.introgram.Chat
import com.rogatka.introgram.ChatAvatar
import com.rogatka.introgram.getAllChatMessages
import com.rogatka.introgram.getAllChats


@Composable
fun TopBarFolder(color: Color = Color(0x11FFFFFF), content: @Composable (RowScope.() -> Unit)) {
    Surface(
        modifier = Modifier
            .padding(8.dp, 8.dp, 0.dp, 8.dp)
            .clip(RoundedCornerShape(percent = 50))
            .clickable{},
        shape = RoundedCornerShape(percent = 50),
        color = color,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val chats: List<Chat> = getAllChats(context)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("details")
                },
                content = {Icon(Icons.Default.Edit, contentDescription = "Edit")},
                modifier = Modifier.padding(16.dp)
            )
        },
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(
                            "Чаты",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* do something */ }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* do something */ }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    TopBarFolder(color = Color(0x22FFFFFF)) {
                        Icon(Icons.Filled.Checklist, contentDescription = "1", modifier = Modifier.size(24.dp))
                    }
                    repeat (10) {
                        TopBarFolder {
                            Icon(Icons.Filled.Star, contentDescription = "1", modifier = Modifier.size(16.dp))
                            Text("Квакушка", modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
            }


        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp),
        ) {
            chats.forEach { chat ->
                val allMessages = getAllChatMessages(context, chat.id)

                val lastMessage = if (allMessages.isNullOrEmpty()) {
                    "Тут ничего нет!"
                } else {
                    allMessages[0].content
                }
                Box(
                    modifier = Modifier
                        .clickable {
                            navController.navigate("chat/${chat.id}")
                        }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ChatAvatar(filename = chat.imagePath, size = 60.dp)
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(
                                chat.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                lastMessage,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}