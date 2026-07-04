package com.example.railway.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed class AiResult<out T> {
    data class Success<T>(val data: T, val modelId: String) : AiResult<T>()
    data class RateLimited(val retryAfterMs: Long) : AiResult<Nothing>()
    data class NetworkError(val message: String) : AiResult<Nothing>()
    data class ParseError(val raw: String) : AiResult<Nothing>()
    data object NoApiKey : AiResult<Nothing>()
}

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<AiMessage>,
    @SerialName("response_format") val responseFormat: AiResponseFormat? = null,
    val temperature: Float = 0.7f,
    @SerialName("max_tokens") val maxTokens: Int = 1024,
    val stream: Boolean = false
)

@Serializable
data class AiMessage(val role: String, val content: String)

@Serializable
data class AiResponseFormat(val type: String)

@Serializable
data class ChatResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<AiChoice> = emptyList(),
    val error: AiError? = null
)

@Serializable
data class AiError(
    val message: String? = null,
    val code: Int? = null
)

@Serializable
data class AiChoice(
    val index: Int = 0,
    val message: AiMessage,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class ChatStreamResponse(
    val choices: List<AiStreamChoice> = emptyList()
)

@Serializable
data class AiStreamChoice(
    val delta: AiDelta,
    @SerialName("finish_reason") val finishReason: String? = null
)

@Serializable
data class AiDelta(
    val content: String? = null
)

enum class ChatRole { USER, ASSISTANT }

data class SupportChatMessage(
    val role: ChatRole,
    val text: String,
    val isNew: Boolean = false
)
