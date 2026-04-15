package com.chatbase.demo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chatbase.sdk.Chatbase
import com.chatbase.sdk.ChatbaseClient
import com.chatbase.sdk.ConversationListState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(
    val agentId: String = "",
    val isConnected: Boolean = false,
    val deviceId: String = "",
    val isIdentified: Boolean = false,
    val currentUserId: String? = null,
    val error: String? = null
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(AppUiState())
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    var client: ChatbaseClient? = null
        private set

    var conversationList: ConversationListState? = null
        private set

    fun connect(agentId: String) {
        if (agentId.isBlank()) {
            _state.update { it.copy(error = "Agent ID cannot be empty") }
            return
        }

        client?.close()

        val newClient = Chatbase.create(getApplication()) {
            this.agentId = agentId
            this.baseUrl = "https://www.chatbase.co"
        }
        client = newClient
        conversationList = ConversationListState(newClient)

        _state.update {
            it.copy(
                agentId = agentId,
                isConnected = true,
                deviceId = newClient.deviceId,
                isIdentified = newClient.isIdentified,
                currentUserId = newClient.currentUserId,
                error = null
            )
        }
    }

    fun identify(token: String) {
        val c = client ?: return
        viewModelScope.launch {
            try {
                c.identify(token)
                _state.update {
                    it.copy(
                        isIdentified = c.isIdentified,
                        currentUserId = c.currentUserId,
                        error = null
                    )
                }
                loadConversations()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Identify failed: ${e.message}") }
            }
        }
    }

    fun logout() {
        client?.logout()
        _state.update {
            it.copy(isIdentified = false, currentUserId = null)
        }
        loadConversations()
    }

    fun newConversation() {
        client?.newConversation()
    }

    fun loadConversations() {
        viewModelScope.launch { conversationList?.load(limit = 20) }
    }

    fun loadMoreConversations() {
        viewModelScope.launch { conversationList?.loadMore() }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        client?.close()
        super.onCleared()
    }
}
