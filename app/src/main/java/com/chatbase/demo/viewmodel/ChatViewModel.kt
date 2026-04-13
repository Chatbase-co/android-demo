package com.chatbase.demo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chatbase.sdk.ChatbaseClient
import com.chatbase.sdk.ConversationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    client: ChatbaseClient,
    initialConversationId: String?
) : ViewModel() {

    private val conversation = ConversationState(client)
    val state: StateFlow<ConversationState.State> = conversation.state

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    init {
        if (initialConversationId != null) {
            viewModelScope.launch { conversation.loadHistory(initialConversationId, limit = 5) }
        }
    }

    fun onInputChanged(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank()) return
        _inputText.value = ""
        viewModelScope.launch { conversation.sendMessage(text) }
    }

    fun retryMessage(messageId: String) {
        viewModelScope.launch { conversation.retry(messageId) }
    }

    fun loadMoreHistory() {
        viewModelScope.launch { conversation.loadMoreHistory() }
    }

    override fun onCleared() {
        conversation.close()
    }
}
