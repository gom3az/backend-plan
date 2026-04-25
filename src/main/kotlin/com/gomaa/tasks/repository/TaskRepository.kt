package com.gomaa.tasks.repository

import com.gomaa.tasks.model.Task
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository : JpaRepository<Task, Long> {

    fun findByCompleted(completed: Boolean): List<Task>

    fun countByCompleted(completed: Boolean): List<Task>

    fun existsByTitle(completed: Boolean): Boolean

}