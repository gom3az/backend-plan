package com.gomaa.tasks.repository

import com.gomaa.tasks.model.Task
import com.gomaa.tasks.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskRepository : JpaRepository<Task, Long> {

    fun findAllByUser_UsernameOrderByCreatedAtDesc(username: String): List<Task>

    fun findByIdAndUser_Username(id: Long, username: String): Task?

    fun findByCompleted(completed: Boolean): List<Task>

    fun countByCompleted(completed: Boolean): List<Task>

    fun existsByTitle(title: String): Boolean

}