package com.gomaa.tasks.controller

import com.gomaa.tasks.dto.*
import com.gomaa.tasks.exceptions.TaskNotFoundException
import com.gomaa.tasks.repository.TaskRepository
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDateTime

@RestController
@RequestMapping("/tasks")
class TaskController(val taskRepository: TaskRepository) {

    @GetMapping("")
    fun getAllTasks(): ResponseEntity<List<TaskResponse>> {
        return ResponseEntity.ok(taskRepository.findAll().map { it.toResponse() })
    }

    @GetMapping("/{id}")
    fun getTask(@PathVariable @Valid id: Long): ResponseEntity<TaskResponse> {
        val task = taskRepository.findById(id).orElseThrow { TaskNotFoundException() }
        return ResponseEntity.ok(task.toResponse())
    }

    @PostMapping("")
    fun save(
        @RequestBody @Valid request: CreateTaskRequest, uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Unit> {
        val created = taskRepository.save(request.toEntity())

        val uri = uriComponentsBuilder.buildAndExpand(created.id).toUri()
        return ResponseEntity.created(uri).build()
    }

    @PutMapping("/{id}")
    fun updateTask(
        @PathVariable @Valid id: Long,
        @RequestBody @Valid request: UpdateTaskRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Unit> {
        val task = taskRepository.findById(id).orElseThrow { TaskNotFoundException() }
        taskRepository.save(
            task.copy(
                title = request.title ?: task.title,
                description = request.description ?: task.description,
                completed = request.completed ?: task.completed,
                dueDate = request.dueDate ?: task.dueDate,
                updatedAt = LocalDateTime.now()
            )
        )
        val uri = uriComponentsBuilder.buildAndExpand(id).toUri()
        return ResponseEntity.created(uri).build()
    }
}