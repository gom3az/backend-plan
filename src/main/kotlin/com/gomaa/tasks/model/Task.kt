package com.gomaa.tasks.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "tasks", //
    indexes = [//
        Index(name = "idx_after_id", columnList = "created_at"), //
        Index(name = "idx_due_date", columnList = "due_date"), //
        Index(name = "idx_tasks_user_id", columnList = "user_id"), //
    ],
)
class Task(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) var user: User,
    @Column(nullable = false) val title: String,
    @Column(columnDefinition = "TEXT") val description: String? = null,
    @Column(name = "completed", nullable = false) val completed: Boolean = false,
    @Column(name = "due_date") val dueDate: LocalDateTime? = null,
    @Column(
        name = "created_at",
        nullable = false,
        updatable = false,
    ) val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(name = "updated_at", nullable = false) val updatedAt: LocalDateTime = LocalDateTime.now(),
)
