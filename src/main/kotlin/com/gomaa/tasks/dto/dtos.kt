package com.gomaa.tasks.dto

import com.gomaa.tasks.model.Task
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class CreateTaskRequest(
    @field:NotBlank @field:Size(min = 1, max = 255) val title: String,

    val description: String? = null, val completed: Boolean = false, val dueDate: LocalDateTime? = null
)

data class UpdateTaskRequest(
    @field:Size(min = 1, max = 255) val title: String? = null,

    val description: String? = null, val completed: Boolean? = null, val dueDate: LocalDateTime? = null
)

data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val completed: Boolean,
    val dueDate: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

fun Task.toResponse() = TaskResponse(
    id = id,
    title = title,
    description = description,
    completed = completed,
    dueDate = dueDate,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CreateTaskRequest.toEntity() = Task(
    title = title, description = description, completed = completed, dueDate = dueDate
)