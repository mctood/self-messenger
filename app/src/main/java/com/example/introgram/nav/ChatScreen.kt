package com.rogatka.introgram.nav

import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rogatka.introgram.ChatAvatar
import com.rogatka.introgram.Message
import com.rogatka.introgram.changeChatBackground
import com.rogatka.introgram.changeChatPhoto
import com.rogatka.introgram.deleteChat
import com.rogatka.introgram.deleteImageFile
import com.rogatka.introgram.getAllChatMessages
import com.rogatka.introgram.getChatByID
import com.rogatka.introgram.loadBitmapFromFile
import com.rogatka.introgram.modals.ConfirmChatDeleteModal
import com.rogatka.introgram.modals.EditChatNameModal
import com.rogatka.introgram.newMessage
import com.rogatka.introgram.randomUID
import com.rogatka.introgram.renameChat
import com.rogatka.introgram.resizeToCover
import com.rogatka.introgram.saveBitmapToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime


@Composable
fun MessageBox(text: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp, 8.dp, 3.dp, 8.dp), // Радиус скругления
        color = MaterialTheme.colorScheme.surfaceContainer, // Цвет фона
        modifier = modifier.padding(bottom = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp, 12.dp, 12.dp, 4.dp),
            horizontalAlignment = Alignment.End
        ) {
            Box {
                Text(text = text)
            }
            Box {
                Text("00:00", fontWeight = FontWeight.Thin, fontSize = 12.sp)
            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatScreen(chatId: Int, navController: NavController) {
    val context = LocalContext.current
    val chat = getChatByID(context = context, id = chatId)
    if (chat == null) {
        navController.navigate("main")
        return
    }
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
            renameChat(context, newChatName, chat)
            showEditNameDialog = false
            navController.navigate("chat/${chatId}")
        })

    /** END MODALS **/

    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }


    fun sendMessage() {
        val messageModel = Message(
            id = randomUID(), content = messageText, time = LocalDateTime.now()
        )
        newMessage(
            context = context, chat = chat, message = messageModel
        )
        messageText = ""
        focusRequester.requestFocus()
        keyboardController?.show()
        messages.add(0, messageModel)
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
                        ChatAvatar(imagePath.value, 36.dp, loading = avatarLoading)
                        Text(
                            chat.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("main") }) {
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
                    .heightIn(min = 48.dp)
                    .imePadding()
                    .clip(RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp))
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
                    placeholder = { Text("Введите сообщение...") },
                    singleLine = false,
                    maxLines = 10,
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
//                        .drawWithCache {
//                            // Оптимизация рендеринга
//                            onDrawWithContent {
//                                drawRect(Color(0x33000000)) // Затемнение
//                                drawImage(imageBitmap )
//                            }
//                        }
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End,
                    reverseLayout = true,
                ) {
                    items(messages) { message: Message ->
                        MessageBox(
                            text = message.content,
                            modifier = Modifier.animateItemPlacement()
                        )
                    }
                }
            }

        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
}


