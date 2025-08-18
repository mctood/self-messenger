@file:OptIn(ExperimentalMaterial3Api::class)

package com.rogatka.introgram.nav

import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rogatka.introgram.modals.MoveToFolderModal
import com.rogatka.introgram.ChatAvatar
import com.rogatka.introgram.ChatTypes
import com.rogatka.introgram.Message
import com.rogatka.introgram.MessageBox
import com.rogatka.introgram.SystemMessageBox
import com.rogatka.introgram.TodoItemBox
import com.rogatka.introgram.changeChatBackground
import com.rogatka.introgram.changeChatPhoto
import com.rogatka.introgram.checkMessageInTodoChat
import com.rogatka.introgram.currentDateToString
import com.rogatka.introgram.dateMessage
import com.rogatka.introgram.datesDifferent
import com.rogatka.introgram.deleteChat
import com.rogatka.introgram.deleteImageFile
import com.rogatka.introgram.editMessage
import com.rogatka.introgram.getAllChatMessages
import com.rogatka.introgram.getChatByID
import com.rogatka.introgram.loadBitmapFromFile
import com.rogatka.introgram.modals.ConfirmChatDeleteModal
import com.rogatka.introgram.modals.EditChatNameModal
import com.rogatka.introgram.modals.EditMessageModal
import com.rogatka.introgram.moveChatToFolder
import com.rogatka.introgram.newMessage
import com.rogatka.introgram.randomUID
import com.rogatka.introgram.removeMessage
import com.rogatka.introgram.renameChat
import com.rogatka.introgram.resizeToCover
import com.rogatka.introgram.saveBitmapToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun ChatScreen(chatId: Int, navController: NavController, folderToReturn: Int = 0) {
    val context = LocalContext.current
    val chat = getChatByID(context = context, id = chatId)

    if (chat == null) {
        navController.navigate("main/0")
        return
    }
    var chatName by remember {mutableStateOf(chat.name)}
    val messages = remember {
        getAllChatMessages(context = context, chatId = chatId)?.toMutableStateList()
            ?: mutableStateListOf()
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val listState = rememberLazyListState()
    var expanded by remember { mutableStateOf(false) }
    val imagePath = remember { mutableStateOf(chat.imagePath) }
    val backgroundPath = remember { mutableStateOf(chat.backgroundPath) }

    var avatarLoading by remember { mutableStateOf(false) }
    var bgLoading by remember { mutableStateOf(false) }
    var messageToEdit by remember { mutableStateOf<Message?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val imageBitmap = loadBitmapFromFile(
        context = LocalContext.current,
        filename = backgroundPath.value
    )?.asImageBitmap() ?: ImageBitmap(1, 1)


    val pickPhoto =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                avatarLoading = true
                if (imagePath.value.isNotEmpty()) {
                    coroutineScope.launch(Dispatchers.IO) {
                        deleteImageFile(context = context, filename = imagePath.value)
                    }
                    imagePath.value = ""
                }
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        // 2. Загрузка Bitmap с автоматическим закрытием потока
                        val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)?.resizeToCover(128, 128)
                        } ?: return@launch

                        // 3. Сохранение файла
                        val imageId = randomUID()
                        val newFilename = "${imageId}.png"

                        if (saveBitmapToFile(context, bitmap, newFilename).isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                imagePath.value = newFilename
                                avatarLoading = false
                                changeChatPhoto(context, chat, imagePath.value)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PhotoPicker", "Error processing image", e)
                    }
                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }


    val pickBackground =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                bgLoading = true
                if (backgroundPath.value?.isNotEmpty() ?: false) {
                    coroutineScope.launch(Dispatchers.IO) {
                        deleteImageFile(context = context, filename = backgroundPath.value)
                    }
                    backgroundPath.value = ""
                }
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        // 2. Загрузка Bitmap с автоматическим закрытием потока
                        val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)
                        } ?: return@launch

                        // 3. Сохранение файла
                        val imageId = randomUID()
                        val newFilename = "${imageId}.bg.png"

                        if (saveBitmapToFile(context, bitmap, newFilename).isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                backgroundPath.value = newFilename
                                bgLoading = false
                                changeChatBackground(context, chat, newFilename)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PhotoPicker", "Error processing image", e)
                    }
                }
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }


    /** MODALS **/

    var showConfirmDeleteDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showMoveToFolderDialog by remember { mutableStateOf(false) }
    var showMessageEditDialog by remember { mutableStateOf(false) }

    ConfirmChatDeleteModal(
        show = showConfirmDeleteDialog,
        onDismiss = { showConfirmDeleteDialog = false },
        onConfirm = {
            deleteChat(context = context, chat = chat)
            showConfirmDeleteDialog = false
        })

    EditChatNameModal(
        show = showEditNameDialog,
        onDismiss = { showEditNameDialog = false },
        chat = chat,
        onConfirm = { newChatName ->
            chatName = newChatName
            renameChat(context, newChatName, chat)
            showEditNameDialog = false
        })

    MoveToFolderModal(
        show = showMoveToFolderDialog,
        onDismiss = { showMoveToFolderDialog = false },
        onConfirm = { folder ->
            moveChatToFolder(context, chat, folder)
            showMoveToFolderDialog = false
            navController.navigate("main/${folder?.id ?: 0}")
        })

    EditMessageModal(
        show = showMessageEditDialog,
        onDismiss = {
            showMessageEditDialog = false
            messageToEdit = null
        },
        message = messageToEdit,
        onConfirm = { message ->
            val messageIndex = messages.indexOfFirst { it.id == message.id }
            messages[messageIndex].content = message.content
            editMessage(context, chat, message, message.content)
            showMessageEditDialog = false
            messageToEdit = null
        })

    /** END MODALS **/

    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }


    fun sendMessage() {
        val messageModel = Message(
            id = randomUID(), content = messageText, dateTime = currentDateToString()
        )
        newMessage(
            context = context, chat = chat, message = messageModel
        )
        messageText = ""
        focusRequester.requestFocus()
        keyboardController?.show()
        if (messages.isEmpty() || datesDifferent(messages[0], messageModel)) {
            messages.add(0, dateMessage(messageModel))
        }
        messages.add(0, messageModel)
    }

    fun deleteMessage(id: Int) {
        val messageIndex = messages.indexOfFirst { it.id == id }
        removeMessage(context, chat, messages[messageIndex])
        messages.removeAt(messageIndex)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChatAvatar(imagePath.value, size = 36.dp, loading = avatarLoading, todo = chat.type == ChatTypes.TODO)
                        Text(
                            chatName,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("main/${folderToReturn}") }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (bgLoading) {
                        CircularProgressIndicator()
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.Menu, contentDescription = "Menu"
                        )
                    }
                    DropdownMenu(
                        expanded = expanded, onDismissRequest = {
                            expanded = false
                            focusRequester.freeFocus()
                        }) {
                        DropdownMenuItem(text = { Text("Сменить название") }, leadingIcon = {
                            Icon(
                                Icons.Default.Edit, contentDescription = "Переименовать"
                            )
                        }, onClick = {
                            expanded = false
                            showEditNameDialog = true
                        })
                        DropdownMenuItem(text = { Text("Сменить картинку") }, leadingIcon = {
                            Icon(
                                Icons.Default.Photo, contentDescription = "Сменить картинку"
                            )
                        }, onClick = {
                            expanded = false
                            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        })
                        DropdownMenuItem(
                            text = { Text("Установить фон") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.LocalFlorist,
                                    contentDescription = "Установить фон"
                                )
                            },
                            onClick = {
                                expanded = false
                                pickBackground.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Выбрать папку") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = "Выбрать папку"
                                )
                            },
                            onClick = {
                                expanded = false
                                showMoveToFolderDialog = true
                            },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(text = { Text("Удалить чат") }, leadingIcon = {
                            Icon(
                                Icons.Default.Delete, contentDescription = "Удалить"
                            )
                        }, onClick = {
                            expanded = false
                            showConfirmDeleteDialog = true
                        })
                    }
                },
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding(),
                tonalElevation = 8.dp,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),

                ) {
                // Поле ввода сообщения
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .focusRequester(focusRequester),
                    placeholder = { Text(if (chat.type == ChatTypes.CLASSIC) "Введите сообщение..."  else "Что нужно сделать?") },
                    singleLine = false,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                )

                // Кнопка отправки
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            sendMessage()
                            focusRequester.requestFocus()
                            keyboardController?.show()
                        }
                    }, enabled = messageText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Отправить",
                        tint = if (messageText.isNotBlank()) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Image(
                    painter = BitmapPainter(imageBitmap),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                )

                if (chat.type == ChatTypes.CLASSIC)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.End,
                        reverseLayout = true,
                    ) {
                        items(messages) { message: Message ->
                            if (message.isSystem)
                                SystemMessageBox(message.content)
                            else MessageBox(
                                text = message.content,
                                time = message.time(),
                                onDelete = { deleteMessage(id = message.id) },
                                onEdit = {
                                    messageToEdit = message
                                    showMessageEditDialog = true
                                }
                            )
                        }
                    }

                if (chat.type == ChatTypes.TODO) LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.Start,
                    reverseLayout = true,
                ) {
                    items(messages) { message: Message ->
                        TodoItemBox(
                            checked = message.done,
                            text = message.content,
                            modifier = Modifier
                                .clickable {
                                    val index = messages.indexOfFirst { it.id == message.id }
                                    messages[index] = message.copy(done = !message.done)
                                    checkMessageInTodoChat(
                                        context,
                                        chat,
                                        message,
                                        !message.done
                                    )
                                }
                        )
                    }
                }
            }
        }
    }
}


