package com.gomaa.tasks.dto

import java.time.LocalDateTime

data class ErrorResponse(
    val status: Int,
    val message: String,
    val errors: List<String>? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val path: String? = null
)