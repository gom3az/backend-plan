package com.gomaa.tasks.advice

import com.gomaa.tasks.dto.ErrorResponse
import com.gomaa.tasks.exceptions.ApiValidationException
import com.gomaa.tasks.exceptions.TaskNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.HandlerMethodValidationException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException::class)
    fun handleNotFound(
        ex: TaskNotFoundException, request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse(
                    status = HttpStatus.NOT_FOUND.value(),
                    message = ex.message ?: "Resource not found",
                    path = request.getDescription(false).replace("uri=", "")
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        ex: MethodArgumentNotValidException, request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = "Validation failed",
                    errors = errors,
                    path = request.getDescription(false).replace("uri=", "")
                )
            )
    }

    @ExceptionHandler(HandlerMethodValidationException::class)
    fun handleMethodValidation(
        ex: HandlerMethodValidationException, request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.allErrors.map { it.defaultMessage ?: "Validation error" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = "Validation failed",
                    errors = errors,
                    path = request.getDescription(false).replace("uri=", "")
                )
            )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMessageNotReadable(
        ex: HttpMessageNotReadableException, request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val rawMessage = ex.mostSpecificCause.message ?: "Invalid request body"
        val fieldName = Regex("""JSON property (\w+)""").find(rawMessage)?.groupValues?.get(1)
            ?: Regex("""parameter (\w+)""").find(rawMessage)?.groupValues?.get(1)
        val message = fieldName?.let { "$it is required" } ?: "Invalid request body"
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = message,
                    path = request.getDescription(false).replace("uri=", "")
                )
            )
    }

    @ExceptionHandler(ApiValidationException::class)
    fun handleApiValidation(
        ex: ApiValidationException, request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = ex.message ?: "Validation failed",
                    path = request.getDescription(false).replace("uri=", "")
                )
            )
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthentication(
        ex: AuthenticationException, request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse(
                    status = HttpStatus.UNAUTHORIZED.value(),
                    message = "Invalid credentials",
                    path = request.getDescription(false).replace("uri=", "")
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        ex: Exception, request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "An unexpected error occurred",
                    path = request.getDescription(false).replace("uri=", "")
                )
            )
    }
}