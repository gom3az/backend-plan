package com.gomaa.tasks.controller

import com.gomaa.tasks.dto.*
import com.gomaa.tasks.exceptions.TaskNotFoundException
import com.gomaa.tasks.exceptions.UserNotFoundException
import com.gomaa.tasks.repository.TaskRepository
import com.gomaa.tasks.repository.UserRepository
import com.gomaa.tasks.specs.TaskSpecs
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalDateTime

@RestController
@RequestMapping("/tasks")
class TaskController(
    val taskRepository: TaskRepository,
    val userRepository: UserRepository,
) {

    @GetMapping("")
    fun getUserTasks(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) afterId: LocalDateTime?,
        @RequestParam(defaultValue = "20") pageSize: Int,
        @RequestParam(defaultValue = "dueDate") sort: String,
        @RequestParam(defaultValue = "asc") direction: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dueDateFrom: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dueDateTo: LocalDate?
    ): ResponseEntity<List<TaskResponse>> {
        val sortDirection = if (direction.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC

        val pageable = PageRequest.of(0, pageSize, Sort.by(sortDirection, sort))
        val spec = TaskSpecs.withFilters(
            afterId = afterId, assignee = userDetails.username, dueDateFrom = dueDateFrom, dueDateTo = dueDateTo
        )
        val slice = taskRepository.findAll(spec, pageable)
        val tasks = slice.content

        return ResponseEntity.ok(tasks.map { it.toResponse() })
    }

    @GetMapping("/{id}")
    fun getTask(
        @PathVariable @Valid id: Long, @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<TaskResponse> {
        val task = taskRepository.findByIdAndUser_Username(id, userDetails.username) ?: throw TaskNotFoundException()
        return ResponseEntity.ok(task.toResponse())
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("")
    fun createTask(
        @Valid @RequestBody request: CreateTaskRequest, @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<TaskResponse> {
        val user = userRepository.findByUsername(userDetails.username) ?: throw UserNotFoundException()
        val task = taskRepository.save(request.toEntity(user))
        return ResponseEntity.status(HttpStatus.CREATED).body(task.toResponse())
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
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