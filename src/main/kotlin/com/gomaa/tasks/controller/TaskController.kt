package com.gomaa.tasks.controller

import com.gomaa.tasks.dto.*
import com.gomaa.tasks.exceptions.TaskNotFoundException
import com.gomaa.tasks.exceptions.UserNotFoundException
import com.gomaa.tasks.model.Task
import com.gomaa.tasks.repository.TaskRepository
import com.gomaa.tasks.repository.UserRepository
import com.gomaa.tasks.service.CursorService
import com.gomaa.tasks.specs.TaskSpecs
import jakarta.validation.Valid
import org.springframework.data.domain.ScrollPosition
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Window
import org.springframework.data.jpa.domain.Specification
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/tasks")
class TaskController(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val cursorService: CursorService,
) {
    @GetMapping("")
    fun getUserTasks(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "20") pageSize: Int,
        @RequestParam(defaultValue = "dueDate") sort: String,
        @RequestParam(defaultValue = "asc") direction: String,
        @RequestParam(required = false) afterCursor: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dueDateFrom: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dueDateTo: LocalDate?,
    ): ResponseEntity<TaskPageResponse> {
        val sortDirection = if (direction.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val sorted = Sort.by(sortDirection, sort).and(Sort.by(Sort.Direction.ASC, "id"))
        val spec: Specification<Task> =
            TaskSpecs.withFilters(
                assignee = userDetails.username,
                dueDateFrom = dueDateFrom,
                dueDateTo = dueDateTo,
            )

        val position: ScrollPosition =
            if (afterCursor == null) {
                ScrollPosition.keyset()
            } else {
                val cursor = cursorService.decode(afterCursor)
                ScrollPosition.of(
                    mapOf("createdAt" to cursor.createdAt, "dueDate" to cursor.dueDate, "id" to cursor.id),
                    ScrollPosition.Direction.FORWARD,
                )
            }

        val tasks: Window<Task> =
            taskRepository.findBy(spec) { query ->
                query.limit(pageSize).sortBy(sorted).scroll(position)
            }

        val lastTask = tasks.toList().lastOrNull()
        val nextCursor =
            lastTask?.let {
                cursorService.encode(
                    TaskCursor(
                        createdAt = it.createdAt,
                        dueDate = it.dueDate,
                        id = it.id.toString(),
                    ),
                )
            }

        return ResponseEntity.ok(
            TaskPageResponse(
                tasks = tasks.toList().map { it.toResponse() },
                nextCursor = nextCursor,
            ),
        )
    }

    @GetMapping("/{id}")
    fun getTask(
        @PathVariable @Valid id: Long,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<TaskResponse> {
        val task = taskRepository.findByIdAndUser_Username(id, userDetails.username) ?: throw TaskNotFoundException()
        return ResponseEntity.ok(task.toResponse())
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("")
    fun createTask(
        @Valid @RequestBody request: CreateTaskRequest,
        @AuthenticationPrincipal userDetails: UserDetails,
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
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<Unit> {
        val task = taskRepository.findByIdAndUser_Username(id, userDetails.username) ?: throw TaskNotFoundException()
        taskRepository.save(request.toEntity(task))
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/{id}")
    fun deleteTask(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<Unit> {
        val exist = taskRepository.findByIdAndUser_Username(id, userDetails.username) ?: throw TaskNotFoundException()
        taskRepository.delete(exist)

        return ResponseEntity.noContent().build()
    }
}
