package com.gomaa.tasks.repository

import com.gomaa.tasks.model.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository :
    JpaRepository<Task, Long>,
    JpaSpecificationExecutor<Task> {
    fun findByIdAndUser_Username(
        id: Long,
        username: String,
    ): Task?

    fun findByCompleted(completed: Boolean): List<Task>

    fun countByCompleted(completed: Boolean): List<Task>

    fun existsByTitle(title: String): Boolean
}
