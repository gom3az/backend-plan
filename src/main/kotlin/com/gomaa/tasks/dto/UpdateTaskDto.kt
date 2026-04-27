package com.gomaa.tasks.dto

import com.gomaa.tasks.model.Task
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class UpdateTaskRequest(
    @field:Size(min = 1, max = 255) val title: String? = null,
    val description: String? = null,
    val completed: Boolean? = null,
    val dueDate: LocalDateTime? = null
)

fun UpdateTaskRequest.toEntity(task: Task) = Task(
    id = task.id,
    title = title ?: task.title,
    description = description ?: task.description,
    completed = completed ?: task.completed,
    dueDate = dueDate ?: task.dueDate,
    createdAt = task.createdAt,
    updatedAt = LocalDateTime.now()
)