package com.gomaa.tasks.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    val username: String,
    @field:NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 6 characters")
    val password: String,
)
