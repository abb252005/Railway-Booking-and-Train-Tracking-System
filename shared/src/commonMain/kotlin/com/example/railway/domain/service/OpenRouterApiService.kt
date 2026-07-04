package com.example.railway.domain.service

import com.example.railway.domain.model.ChatRequest
import com.example.railway.domain.model.ChatResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class OpenRouterApiService(private val client: HttpClient) {
    private val baseUrl = "https://openrouter.ai/api/v1/chat/completions"

    suspend fun chatCompletion(request: ChatRequest, apiKey: String): ChatResponse {
        val response: HttpResponse = client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${apiKey.trim()}")
            setBody(request)
        }
        
        return if (response.status == HttpStatusCode.OK) {
            response.body()
        } else {
            val errorBody = response.bodyAsText()
            throw Exception("HTTP ${response.status.value}: $errorBody")
        }
    }

    fun streamChatCompletion(request: ChatRequest, apiKey: String): Flow<String> = flow {
        client.preparePost(baseUrl) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${apiKey.trim()}")
            setBody(request.copy(stream = true))
        }.execute { response ->
            if (response.status != HttpStatusCode.OK) {
                val errorBody = response.bodyAsText()
                throw Exception("HTTP ${response.status.value}: $errorBody")
            }
            
            val channel: ByteReadChannel = response.body()
            while (!channel.isClosedForRead) {
                val line = channel.readLine() ?: break
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue
                
                if (trimmed.startsWith("data: ")) {
                    val data = trimmed.substring(6).trim()
                    if (data == "[DONE]") break
                    emit(data)
                } else if (!trimmed.startsWith(":")) { 
                    emit(trimmed)
                }
            }
        }
    }
}
