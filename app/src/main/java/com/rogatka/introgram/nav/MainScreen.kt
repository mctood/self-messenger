package com.rogatka.introgram.nav

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rogatka.introgram.modals.ConfirmFolderDeleteModal
import com.rogatka.introgram.modals.NewFolderModal
import com.rogatka.introgram.Chat
import com.rogatka.introgram.ChatAvatar
import com.rogatka.introgram.ChatTypes
import com.rogatka.introgram.Folder
import com.rogatka.introgram.Message
import com.rogatka.introgram.SharedContentHolder
import com.rogatka.introgram.TaskStats
import com.rogatka.introgram.addFolder
import com.rogatka.introgram.countStats
import com.rogatka.introgram.currentDateToString
import com.rogatka.introgram.deleteFolder
import com.rogatka.introgram.getAllChatMessages
import com.rogatka.introgram.getAllChats
import com.rogatka.introgram.getAllFolders
import com.rogatka.introgram.modals.AboutModal
import com.rogatka.introgram.moveChatToFolder
import com.rogatka.introgram.newMessage
import com.rogatka.introgram.randomUID
import com.rogatka.introgram.topBarColors


@Composable
fun TopBarFolder(
    color: Color = Color(0x11FFFFFF),
    selected: Boolean = false,
    onClick: (() -> Unit),
    content: @Composable (RowScope.() -> Unit)
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color.Transparent, // Прозрачный, когда не выбрано
        animationSpec = tween(durationMillis = 300), // Длительность анимации в мс
        label = "BorderColorAnimation"
    )

    Surface(
        modifier = Modifier
            .padding(8.dp, 8.dp, 0.dp, 8.dp)
            .clip(RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(percent = 50)
            ),
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
fun MainScreen(navController: NavController, folder: Int = 0) {
    val sharedText = SharedContentHolder.sharedText
    var shareMode by remember { mutableStateOf(sharedText != null) }
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val folders: List<Folder> = remember { getAllFolders(context) }
    val allFolderIds = remember {
        listOf(-1, 0) + folders.map { it.id }
    }
    var folderIndex by rememberSaveable {
        mutableIntStateOf(allFolderIds.indexOf(folder))
    }
    var folderId by rememberSaveable { mutableIntStateOf(folder) }
    val chats: List<Chat> = remember(folderId) {
        if (folderId == -1) {
            getAllChats(context).filter { it.type == ChatTypes.TODO }
        }
        else getAllChats(context, folderId)
    }

    var expanded by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showDeleteFolderDialog by remember { mutableStateOf(false) }
    var showAboutModal by remember { mutableStateOf(false) }

    /** MODALS **/

    ConfirmFolderDeleteModal(
        show = showDeleteFolderDialog,
        onDismiss = { showDeleteFolderDialog = false },
        onConfirm = {
            deleteFolder(context, folderId)
            for (chat in getAllChats(context, folderId)) {
                moveChatToFolder(context, chat, null)
            }
            showDeleteFolderDialog = false
            navController.navigate("main/0")
        })

    NewFolderModal (
        show = showNewFolderDialog,
        onDismiss = { showNewFolderDialog = false },
        onConfirm = { name, icon ->
            val newFolder = Folder(
                id = randomUID(),
                name = name,
                icon = icon
            )
            addFolder(context, newFolder)
            showNewFolderDialog = false
            navController.navigate("main/${newFolder.id}")
        })

    AboutModal(
        show = showAboutModal,
        onConfirm = { showAboutModal = false }
    )

    /** END MODALS **/

    var totalDragX = 0f
    Scaffold(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (totalDragX > 100) {
                            if (folderIndex > 0) {
                                folderIndex--
                                folderId = allFolderIds[folderIndex]
                            }
                        } else if (totalDragX < -100) {
                            if (folderIndex < allFolderIds.lastIndex) {
                                folderIndex++
                                folderId = allFolderIds[folderIndex]
                            }
                        }
                        totalDragX = 0f
                    },
                    onDrag = { change, dragAmount ->
                        totalDragX += dragAmount.x
                        change.consume()
                    }
                )
            },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("details/${folderId}")
                },
                content = {Icon(Icons.Default.Edit, contentDescription = "Edit")},
                modifier = Modifier.padding(8.dp)
            )
        },
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                CenterAlignedTopAppBar(
                    colors = topBarColors(),
                    title = {
                        Text(
                            if (shareMode) "Куда переслать?" else "Чаты",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                        DropdownMenu(
                            expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("Создать папку") }, leadingIcon = {
                                Icon(
                                    Icons.Default.Folder, contentDescription = "Создать папку"
                                )
                            }, onClick = {
                                expanded = false
                                showNewFolderDialog = true
                            })
                            if (folderId > 0)
                                DropdownMenuItem(text = { Text("Удалить папку") }, leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete, contentDescription = "Удалить папку"
                                    )
                                }, onClick = {
                                    expanded = false
                                    showDeleteFolderDialog = true
                                })

                            HorizontalDivider()
                            DropdownMenuItem(text = { Text("О программе") }, leadingIcon = {
                                Icon(
                                    Icons.Default.Info, contentDescription = "О программе"
                                )
                            }, onClick = {
                                expanded = false
                                showAboutModal = true
                            })
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
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .background(topBarColors().containerColor)
                ) {


                    TopBarFolder(selected = folderId ==  -1, color = Color(0x22FFFFFF), onClick = {
                        folderId = -1
                        folderIndex = allFolderIds.indexOf(folderId)
                    }) {
                        Icon(Icons.Filled.Checklist, contentDescription = "1", modifier = Modifier.size(24.dp))
                    }

                    TopBarFolder(selected = folderId ==  0, onClick = {
                        folderId = 0
                        folderIndex = allFolderIds.indexOf(folderId)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "1", modifier = Modifier.size(16.dp))
                        Text("Все чаты", modifier = Modifier.padding(start = 12.dp))
                    }

                    folders.forEach { folder ->
                        TopBarFolder(selected = folderId == folder.id, onClick = {
                            folderId = folder.id
                            folderIndex = allFolderIds.indexOf(folder.id)
                        }) {
                            Icon(folder.icon.icon, contentDescription = "Folder Icon", modifier = Modifier.size(16.dp))
                            Text(folder.name, modifier = Modifier.padding(start = 12.dp))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp) // Минимальная высота, но можно настроить под ваш дизайн
                    )
                }
            }
        }
    ) { padding ->

        if (chats.isEmpty()) {
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
                    "Добавьте чаты, используя кнопку ниже.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    "Для добавления папки кликните на меню сверху",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        else Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp),
        ) {
            if (folderId == -1) {
                val stats = countStats(context)
                TaskStats(stats.done, stats.total)
            }


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
                            if (shareMode) {
                                newMessage(
                                    context = context,
                                    chat = chat,
                                    message = Message(
                                        id = randomUID(),
                                        content = sharedText!!,
                                        dateTime = currentDateToString()
                                    )
                                )
                                shareMode = false
                                SharedContentHolder.sharedText = null
                            }
                            navController.navigate("chat/${chat.id}/${folderId}")
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ChatAvatar(filename = chat.imagePath, size = 60.dp, todo = chat.type == ChatTypes.TODO)
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