package com.flingeria.domain

data class ChatMessage(val role: String, val text: String)
class ContextManager(private val maxMessages: Int = 12) { fun recent(messages: List<ChatMessage>) = messages.takeLast(maxMessages) }
