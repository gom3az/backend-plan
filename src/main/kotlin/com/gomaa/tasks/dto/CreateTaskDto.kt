package com.gomaa.tasks.dto

import com.gomaa.tasks.model.Task
import com.gomaa.tasks.model.User
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class CreateTaskRequest(
    @field:NotBlank(message = "Title is required") @field:Size(
        min = 1,
        max = 255,
        message = "Title must be between 1 and 255 characters",
    )
    val title: String,
    val completed: Boolean = false,
    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    val description: String? = null,
    val dueDate: LocalDateTime? = null,
)

data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val completed: Boolean,
    val dueDate: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

fun Task.toResponse() =
    TaskResponse(
        id = id,
        title = title,
        description = description,
        completed = completed,
        dueDate = dueDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

fun CreateTaskRequest.toEntity(user: User) =
    Task(
        title = title,
        description = description,
        completed = completed,
        dueDate = dueDate,
        user = user,
    )
