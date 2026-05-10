package com.gomaa.tasks.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class TaskCursor
    @JsonCreator
    constructor(
        @JsonProperty("createdAt") val createdAt: LocalDateTime,
        @JsonProperty("dueDate") val dueDate: LocalDateTime?,
        @JsonProperty("id") val id: String,
    )

data class TaskPageResponse(
    val tasks: List<TaskResponse>,
    val nextCursor: String?,
)
