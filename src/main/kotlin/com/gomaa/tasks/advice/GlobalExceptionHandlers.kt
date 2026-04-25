package com.gomaa.tasks.advice

import com.gomaa.tasks.exceptions.TaskNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ErrorResponse(val message: String, val status: Int)

@RestControllerAdvice
class GlobalExceptionHandlers {
    private val log: Logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(TaskNotFoundException::class)
    fun handleNotFound(ex: TaskNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Not found", HttpStatus.NOT_FOUND.value()))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(errors.joinToString("; "), HttpStatus.BAD_REQUEST.value()))
    }
}