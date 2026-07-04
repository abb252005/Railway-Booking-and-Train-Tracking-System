package com.example.railway.presentation

import com.example.railway.util.Config
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.railway.db.*
import com.example.railway.domain.auth.AuthManager
import com.example.railway.domain.model.*
import com.example.railway.domain.repository.AiSupportRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SupportChatState(
    val sessions: List<ChatSessionEntity> = emptyList(),
    val currentSessionId: Long? = null,
    val messages: List<SupportChatMessage> = emptyList(),
    val streamingMessage: String? = null,
    val draft: String = "",
    val sending: Boolean = false,
    val error: String? = null,
    val isMenuOpen: Boolean = false,
    val apiKey: String = Config.OPENROUTER_API_KEY
)

class SupportChatViewModel(
    private val aiRepository: AiSupportRepository,
    private val authManager: AuthManager
) : ViewModel() {
    private val _state = MutableStateFlow(SupportChatState())
    val state: StateFlow<SupportChatState> = _state.asStateFlow()

    private var messageJob: Job? = null

    init {
        viewModelScope.launch {
            authManager.state.collect { auth ->
                val userId = auth.userId ?: "guest"
                aiRepository.observeSessions(userId).collect { sessions ->
                    _state.update { it.copy(sessions = sessions) }
                    if (_state.value.currentSessionId == null && sessions.isNotEmpty()) {
                        selectSession(sessions.first().id)
                    } else if (sessions.isEmpty()) {
                        createNewChat()
                    }
                }
            }
        }
    }

    fun updateApiKey(key: String) = _state.update { it.copy(apiKey = key) }
    fun toggleMenu() = _state.update { it.copy(isMenuOpen = !it.isMenuOpen) }

    fun selectSession(id: Long) {
        _state.update { it.copy(currentSessionId = id, isMenuOpen = false, error = null) }
        messageJob?.cancel()
        messageJob = viewModelScope.launch {
            aiRepository.observeMessages(id).collect { msgs ->
                val chatMsgs = msgs.map {
                    SupportChatMessage(
                        role = if (it.role == "user") ChatRole.USER else ChatRole.ASSISTANT,
                        text = it.text,
                        isNew = false
                    )
                }
                _state.update { it.copy(messages = chatMsgs) }
                
                if (chatMsgs.isEmpty()) {
                    val username = authManager.state.value.username ?: "there"
                    val greeting = "Hi $username, I'm RAIL-E. How can I help you with your journey today?"
                    saveAssistantMessage(id, greeting)
                }
            }
        }
    }

    private suspend fun saveAssistantMessage(sessionId: Long, text: String) {
        val userId = authManager.state.value.userId ?: "guest"
        aiRepository.saveChatMessage(sessionId, userId, ChatRole.ASSISTANT, text)
    }

    fun createNewChat() {
        viewModelScope.launch {
            val userId = authManager.state.value.userId ?: "guest"
            val id = aiRepository.createSession(userId, "New Conversation")
            selectSession(id)
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            aiRepository.deleteSession(id)
            if (_state.value.currentSessionId == id) {
                _state.update { it.copy(currentSessionId = null) }
            }
        }
    }

    fun updateDraft(value: String) = _state.update { it.copy(draft = value, error = null) }

    fun send() {
        val current = _state.value
        val text = current.draft.trim()
        val sid = current.currentSessionId ?: return
        val userId = authManager.state.value.userId ?: "guest"
        
        if (text.isBlank() || current.sending) return

        _state.update { it.copy(draft = "", sending = true, error = null, streamingMessage = "") }

        viewModelScope.launch {
            aiRepository.saveChatMessage(sid, userId, ChatRole.USER, text)
            
            // Update title if it's the first message
            if (current.messages.count { it.role == ChatRole.USER } == 0) {
                val title = if (text.length > 25) text.take(22) + "..." else text
                aiRepository.updateSessionTitle(sid, title)
            }

            val history = _state.value.messages.map { m ->
                AiMessage(role = if (m.role == ChatRole.USER) "user" else "assistant", content = m.text)
            } + AiMessage(role = "user", content = text)

            var fullResponse = ""
            aiRepository.generateChatReplyStream(history, current.apiKey).collect { result ->
                when (result) {
                    is AiResult.Success -> {
                        fullResponse = result.data
                        _state.update { it.copy(streamingMessage = fullResponse) }
                    }
                    is AiResult.NoApiKey -> {
                        _state.update { it.copy(sending = false, error = "Please provide an OpenRouter API key in settings.", streamingMessage = null) }
                    }
                    is AiResult.NetworkError -> {
                        _state.update { it.copy(sending = false, error = result.message, streamingMessage = null) }
                    }
                    else -> { }
                }
            }

            if (fullResponse.isNotBlank()) {
                aiRepository.saveChatMessage(sid, userId, ChatRole.ASSISTANT, fullResponse)
            }
            _state.update { it.copy(sending = false, streamingMessage = null) }
        }
    }
}
