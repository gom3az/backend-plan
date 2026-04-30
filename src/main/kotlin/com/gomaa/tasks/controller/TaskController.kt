package com.gomaa.tasks.controller

import com.gomaa.tasks.dto.*
import com.gomaa.tasks.exceptions.TaskNotFoundException
import com.gomaa.tasks.repository.TaskRepository
import com.gomaa.tasks.repository.UserRepository
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tasks")
class TaskController(
    val taskRepository: TaskRepository,
    val userRepository: UserRepository,
) {

    @GetMapping("")
    fun getUserTasks(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<List<TaskResponse>> {
        return ResponseEntity.ok(
            taskRepository.findAllByUser_UsernameOrderByCreatedAtDesc(userDetails.username).map { it.toResponse() })
    }

    @GetMapping("/{id}")
    fun getTask(
        @PathVariable @Valid id: Long, @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<TaskResponse> {
        val task = taskRepository.findByIdAndUser_Username(id, userDetails.username) ?: throw TaskNotFoundException()
        return ResponseEntity.ok(task.toResponse())
    }

    @PostMapping("")
    fun createTask(
        @Valid @RequestBody request: CreateTaskRequest, @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<TaskResponse> {
        val user = userRepository.findByUsername(userDetails.username) ?: throw UsernameNotFoundException("User not found")
        val task = taskRepository.save(request.toEntity(user))
        return ResponseEntity.status(HttpStatus.CREATED).body(task.toResponse())
    }

    @PutMapping("/{id}")
    fun updateTask(
        @PathVariable @Valid id: Long,
        @RequestBody @Valid request: UpdateTaskRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<Unit> {
        val task = taskRepository.findByIdAndUser_Username(id, userDetails.username) ?: throw TaskNotFoundException()
        taskRepository.save(request.toEntity(task))
        return ResponseEntity.noContent().build()
    }
}