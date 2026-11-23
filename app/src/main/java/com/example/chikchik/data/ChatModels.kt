package com.example.chikchik.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class ChatRequest(
    @SerialName("model") val model: String,
    @SerialName("messages") val messages: List<Message>,
    @SerialName("temperature") val temperature: Double = 0.3,
    @SerialName("max_tokens") val max_tokens: Int = 300
)

@Serializable
data class Message(
    @SerialName("role") val role: String,
    @SerialName("content") val content: String
)

@Serializable
data class ChatResponse(
    @SerialName("choices") val choices: List<Choice>
)

@Serializable
data class Choice(
    @SerialName("message") val message: Message
)
