package com.chatbase.demo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chatbase.sdk.model.UiMessageContent
import com.chatbase.demo.ui.theme.*
import com.chatbase.demo.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val reversedMessages = remember(state.messages) { state.messages.reversed() }

    // Scroll to bottom on send and while streaming
    val isStreaming = state.messages.lastOrNull()?.isStreaming == true
    LaunchedEffect(state.isSending) {
        if (state.isSending) listState.scrollToItem(0)
    }
    LaunchedEffect(isStreaming, state.messages.size) {
        if (isStreaming) listState.scrollToItem(0)
    }

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisible >= totalItems - 3 && state.hasMoreHistory && !state.isLoadingHistory
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMoreHistory()
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Chat",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBackground
                    )
                )
                HorizontalDivider(color = DarkOutline, thickness = 1.dp)
            }
        },
        bottomBar = {
            MessageInput(
                text = inputText,
                onTextChange = viewModel::onInputChanged,
                onSend = viewModel::sendMessage,
                isSending = state.isSending
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            reverseLayout = true,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp)
        ) {
            items(reversedMessages, key = { it.id }) { message ->
                when (val content = message.content) {
                    is UiMessageContent.Text -> {
                        if (content.text.isEmpty() && message.isStreaming) {
                            TypingIndicator()
                        } else if (content.text.isNotEmpty() || message.isError) {
                            MessageBubble(
                                message = message,
                                onRetry = { msgId ->
                                    if (msgId != null) viewModel.retryMessage(msgId)
                                }
                            )
                        }
                    }
                    is UiMessageContent.ToolCall -> ToolCallCard(message = message)
                }
            }
            if (state.isLoadingHistory) {
                item("loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = BluePrimary
                        )
                    }
                }
            }
        }
    }
}
