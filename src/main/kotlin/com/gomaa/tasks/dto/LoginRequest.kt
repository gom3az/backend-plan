package com.gomaa.tasks.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank @field:Size(min = 3, message = "username: must be greater than or equal to 3") val username: String,
    @field:NotBlank @field:Size(
        min = 5,
        message = "password: must be greater than or equal to 5",
    ) @field:Pattern(regexp = "^[a-zA-Z0-9_@]+$", message = "username: invalid characters") val password: String,
)
