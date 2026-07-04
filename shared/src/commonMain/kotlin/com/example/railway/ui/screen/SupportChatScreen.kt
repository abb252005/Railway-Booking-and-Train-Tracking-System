package com.example.railway.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.railway.db.*
import com.example.railway.domain.model.*
import com.example.railway.presentation.SupportChatViewModel
import com.example.railway.ui.component.*
import kotlinx.coroutines.launch

@Composable
fun SupportChatScreen(
    viewModel: SupportChatViewModel,
    onBack: () -> Unit,
) {
    val strings = com.example.railway.ui.theme.LocalRailwayStrings.current
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.messages.size, state.sending) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                ChatSidebar(
                    sessions = state.sessions,
                    currentId = state.currentSessionId,
                    strings = strings,
                    onSelect = { id -> 
                        viewModel.selectSession(id)
                        scope.launch { drawerState.close() }
                    },
                    onNewChat = {
                        viewModel.createNewChat()
                        scope.launch { drawerState.close() }
                    },
                    onDelete = viewModel::deleteSession
                )
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NeuralBackground()

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                ChatHeader(
                        currentTitle = state.sessions.find { it.id == state.currentSessionId }?.title ?: strings.support,
                        strings = strings,
                        onBack = onBack,
                        onToggleMenu = { scope.launch { drawerState.open() } }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(state.messages) { msg ->
                                MessageBubble(msg)
                            }
                            state.streamingMessage?.let { stream ->
                                if (stream.isNotBlank()) {
                                    item {
                                        MessageBubble(SupportChatMessage(ChatRole.ASSISTANT, stream, isNew = false))
                                    }
                                }
                            }
                            if (state.sending) {
                                item { TypingIndicator() }
                            }
                            state.error?.let { err ->
                                item {
                                    Text(
                                        err,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    ChatInput(
                        draft = state.draft,
                        sending = state.sending,
                        apiKey = state.apiKey,
                        strings = strings,
                        onApiKeyChange = viewModel::updateApiKey,
                        onDraftChange = viewModel::updateDraft,
                        onSend = viewModel::send
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatSidebar(
    sessions: List<ChatSessionEntity>,
    currentId: Long?,
    strings: com.example.railway.ui.theme.RailwayStrings,
    onSelect: (Long) -> Unit,
    onNewChat: () -> Unit,
    onDelete: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            strings.conversations,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 20.dp, start = 8.dp)
        )

        Button(
            onClick = onNewChat,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(strings.newChat, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sessions) { session ->
                val isSelected = session.id == currentId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                        .clickable { onSelect(session.id) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = session.title,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    if (!isSelected) {
                        IconButton(onClick = { onDelete(session.id) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = strings.deleteDesc, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatHeader(
    currentTitle: String,
    strings: com.example.railway.ui.theme.RailwayStrings,
    onBack: () -> Unit,
    onToggleMenu: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = strings.back,
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                currentTitle,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                strings.supportAssistant,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp
            )
        }

        IconButton(onClick = onToggleMenu) {
            Icon(Icons.Default.Menu, contentDescription = strings.history, tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun MessageBubble(msg: SupportChatMessage) {
    val isUser = msg.role == ChatRole.USER
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = RoundedCornerShape(
        topStart = 20.dp, topEnd = 20.dp,
        bottomStart = if (isUser) 20.dp else 4.dp,
        bottomEnd = if (isUser) 4.dp else 20.dp
    )
    
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(
            modifier = Modifier
                .widthIn(max = 800.dp)
                .shadow(
                    elevation = if (isUser) 4.dp else 2.dp,
                    shape = shape
                )
                .clip(shape)
                .background(
                    brush = if (isUser) {
                        Brush.linearGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.85f))
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                        )
                    }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            MarkdownRenderer(msg.text, isUser)
        }
    }
}

@Composable
private fun ChatInput(
    draft: String,
    sending: Boolean,
    apiKey: String,
    strings: com.example.railway.ui.theme.RailwayStrings,
    onApiKeyChange: (String) -> Unit,
    onDraftChange: (String) -> Unit,
    onSend: () -> Unit
) {
    var showApiDialog by remember { mutableStateOf(value = false) }

    if (showApiDialog) {
        AlertDialog(
            onDismissRequest = { showApiDialog = false },
            title = { Text(strings.openRouterApiKey) },
            text = {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = onApiKeyChange,
                    label = { Text(strings.apiKey) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(strings.apiKeyPlaceholder) }
                )
            },
            confirmButton = {
                TextButton(onClick = { showApiDialog = false }) { Text(strings.save) }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showApiDialog = true }) {
                    Icon(
                        Icons.Default.Key, 
                        contentDescription = strings.apiKey,
                        tint = if (apiKey.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                OutlinedTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(strings.askAnything) },
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(Modifier.width(12.dp))
                IconButton(
                    onClick = onSend,
                    enabled = !sending && draft.isNotBlank() && apiKey.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (sending || draft.isBlank() || apiKey.isBlank())
                                MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.primary
                        )
                ) {
                    if (sending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = strings.send,
                            tint = if (draft.isBlank() || apiKey.isBlank())
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TypingIndicator() {
    val strings = com.example.railway.ui.theme.LocalRailwayStrings.current
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp)) {
            AiSupportOrb(
                isThinking = true,
                modifier = Modifier.scale(0.5f).offset(y = (-15).dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            strings.thinking,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
    }
}
