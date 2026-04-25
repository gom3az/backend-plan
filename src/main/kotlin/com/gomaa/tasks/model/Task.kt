package com.gomaa.tasks.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tasks")
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val title: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "completed", nullable = false)
    val completed: Boolean = false,

    @Column(name = "due_date")
    val dueDate: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // JPA requires no-arg constructor — Kotlin generates this as a default
    constructor() : this(0, "", null, false, null, LocalDateTime.now(), LocalDateTime.now())
}