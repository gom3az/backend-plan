package com.gomaa.tasks.controller

import com.gomaa.tasks.dto.*
import com.gomaa.tasks.exceptions.TaskNotFoundException
import com.gomaa.tasks.repository.TaskRepository
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder

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
    fun createTask(@Valid @RequestBody request: CreateTaskRequest): ResponseEntity<TaskResponse> {
        val task = taskRepository.save(request.toEntity())
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(task.toResponse())
    }

    @PutMapping("/{id}")
    fun updateTask(
        @PathVariable @Valid id: Long,
        @RequestBody @Valid request: UpdateTaskRequest,
        uriComponentsBuilder: UriComponentsBuilder
    ): ResponseEntity<Unit> {
        val task = taskRepository.findById(id).orElseThrow { TaskNotFoundException() }
        taskRepository.save(request.toEntity(task))
        val uri = uriComponentsBuilder.buildAndExpand(id).toUri()
        return ResponseEntity.created(uri).build()
    }
}