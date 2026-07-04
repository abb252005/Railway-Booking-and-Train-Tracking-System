package com.example.railway.domain.repository

import com.example.railway.domain.model.*
import com.example.railway.domain.service.OpenRouterApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.coroutines.delay

class AiSupportRepository(
    private val api: OpenRouterApiService,
    private val repository: RailwayRepository
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // Use stable models and 'auto' for best reliability
    private val models = listOf(
        "openrouter/auto",
        "google/gemma-7b-it:free",
        "mistralai/mistral-7b-instruct:free",
        "meta-llama/llama-3-8b-instruct:free"
    )

    fun observeSessions(userId: String) = repository.observeSessions(userId)
    fun observeMessages(sessionId: Long) = repository.observeMessages(sessionId)
    suspend fun createSession(userId: String, title: String) = repository.createSession(userId, title)
    suspend fun updateSessionTitle(sessionId: Long, title: String) = repository.updateSessionTitle(sessionId, title)
    suspend fun deleteSession(sessionId: Long) = repository.deleteSession(sessionId)
    suspend fun saveChatMessage(sessionId: Long, userId: String, role: ChatRole, text: String) = 
        repository.saveChatMessage(sessionId, userId, if (role == ChatRole.USER) "user" else "assistant", text)

    private val systemPromptContent = """
        You are RAIL-E, the expert technical assistant for the RailTrack Pro system. 
        Your knowledge is grounded in the actual project files and architecture.

        CORE PROJECT INFO:
        - System: A Kotlin Multiplatform (KMP) railway booking and real-time train tracking system.
        - Structure:
            * `shared`: Common domain logic, models, and UI components.
            * `server`: C-based high-concurrency backend.
            * `androidApp` / `iosApp`: Platform-specific entry points.

        BACKEND ARCHITECTURE (C):
        - High-Performance: Uses `kqueue` (macOS) and `epoll` (Linux) for I/O multiplexing.
        - Scalability: Handles 50,000+ concurrent clients using a multi-threaded worker pool and zero-allocation memory pooling.
        - Spatial Partitioning: A 1000x1000 grid is used to efficiently broadcast train updates only to nearby clients.
        - Custom Binary Protocol: 
            * `MSG_TYPE_SUBSCRIBE` (1)
            * `MSG_TYPE_TRAIN_POSITION` (2): Full position update.
            * `MSG_TYPE_DELTA_UPDATE` (3): Efficient updates using a bitmask (LAT, LNG, PROGRESS, SPEED, BEARING).

        FRONTEND DETAILS (KMP):
        - UI: Compose Multiplatform with "Neural UI" styling and custom shader-like backgrounds.
        - Tracking: `TrainSimulationManager` handles real-time interpolation between server updates.
        - Connectivity: `SocketClient` (Ktor-based) connects to the C server via raw TCP.
        - Local Data: SQLDelight for persistence (sessions, messages, offline data).

        DATA MODELS:
        - `Train`: id, name, status (RUNNING, DELAYED, etc.), carriages, schedule.
        - `Station`: id (e.g., "NY", "CA"), name, latitude, longitude.
        - `Booking`: id, trainId, userId, seatNumber, departureDate, price.
        - `Wallet`: Manages user balance for ticket purchases.

        SETUP & DEPLOYMENT:
        - Server IP: 192.168.1.39 (Mac), Port: 8080.
        - iPad Connection: Must be on the same Wi-Fi. Requires `NSLocalNetworkUsageDescription` and `NSAllowsLocalNetworking` in `Info.plist`.
        - Troubleshooting: Check macOS Firewall and ensure the C server binary is allowed to receive connections.

        YOUR ROLE:
        - Be a technical expert. If a user asks "How does tracking work?", explain the C backend, spatial grid, and binary protocol.
        - If a user asks "How do I connect my iPad?", guide them through the IP and firewall settings from the `CROSS_DEVICE_COMMUNICATION.md` info.
        - Keep answers concise but deeply technical when appropriate.
    """.trimIndent()

    fun generateChatReplyStream(history: List<AiMessage>, apiKey: String): Flow<AiResult<String>> = flow {
        if (apiKey.isBlank()) {
            emit(AiResult.NoApiKey)
            return@flow
        }

        val systemPrompt = AiMessage(
            role = "system",
            content = systemPromptContent
        )
        val messages = listOf(systemPrompt) + history

        var streamStarted = false
        var fullText = ""

        for (model in models) {
            try {
                api.streamChatCompletion(
                    ChatRequest(
                        model = model,
                        messages = messages,
                        temperature = 0.7f,
                        maxTokens = 400
                    ),
                    apiKey
                ).collect { chunk ->
                    chunk.split("\ndata: ").forEach { line ->
                        val data = if (line.startsWith("data: ")) line.substring(6).trim() else line.trim()
                        if (data == "[DONE]") return@forEach
                        
                        try {
                            val resp = json.decodeFromString<ChatStreamResponse>(data)
                            val delta = resp.choices.firstOrNull()?.delta?.content
                            if (delta != null && delta != "") {
                                fullText += delta
                                streamStarted = true
                                emit(AiResult.Success(fullText, model))
                            }
                        } catch (e: Exception) { }
                    }
                }
                if (streamStarted) return@flow
            } catch (e: Exception) {
                if (streamStarted) return@flow
            }
            // Small delay to avoid hitting rate limits too fast during retries
            delay(500)
        }

        // Try non-streaming fallback
        val result = generateChatReply(history, apiKey)
        emit(result)
    }

    private suspend fun generateChatReply(history: List<AiMessage>, apiKey: String): AiResult<String> {
        val systemPrompt = AiMessage(
            role = "system",
            content = systemPromptContent
        )
        val messages = listOf(systemPrompt) + history

        val errors = mutableListOf<String>()
        for (model in models) {
            try {
                val response = api.chatCompletion(
                    ChatRequest(
                        model = model,
                        messages = messages,
                        temperature = 0.7f,
                        maxTokens = 1000
                    ),
                    apiKey
                )
                val text = response.choices.firstOrNull()?.message?.content?.trim()
                if (!text.isNullOrBlank()) return AiResult.Success(text, model)
                
                if (response.error != null) {
                    errors.add("$model: ${response.error.message}")
                } else {
                    errors.add("$model: Empty response (No choices)")
                }
            } catch (e: Exception) {
                errors.add("$model: ${e.message ?: e.toString()}")
            }
            delay(500)
        }
        return AiResult.NetworkError("All models failed. Details: ${errors.joinToString("; ")}")
    }
}
