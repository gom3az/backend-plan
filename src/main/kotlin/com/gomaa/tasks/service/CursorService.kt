package com.gomaa.tasks.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.gomaa.tasks.dto.TaskCursor
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter
import java.util.Base64

@Service
class CursorService {
    private val objectMapper =
        ObjectMapper().apply {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }

    fun encode(cursor: TaskCursor): String {
        val json = objectMapper.writeValueAsString(cursor)
        return Base64.getEncoder().encodeToString(json.toByteArray())
    }

    fun decode(base64: String): TaskCursor =
        try {
            val json = String(Base64.getDecoder().decode(base64))
            objectMapper.readValue(json, TaskCursor::class.java)
        } catch (e: Exception) {
            throw InvalidCursorException("Invalid cursor format: ${e.message}")
        }
}

class InvalidCursorException(
    message: String,
) : RuntimeException(message)
