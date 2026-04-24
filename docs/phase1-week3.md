# Phase 1 — Week 3: DTOs, Validation, Exception Handling

**Goal**: Build clean API contracts. Handle validation errors gracefully with proper HTTP status codes.

**Portfolio deliverable**: All endpoints use DTOs. Invalid input returns structured 400 response. Missing resources return 404. Global exception handler in place.

---

## Tuesday — DTO Pattern and Kotlin Data Classes

### Why This Day

Week 2 introduced you to JPA entities. The problem: you should never expose JPA entities directly as API responses. Entities have lazy-loading traps, circular references, and internal infrastructure details. DTOs (Data Transfer Objects) give you a stable, intentional API contract.

### Study Resources

| Resource | What It Covers |
|----------|----------------|
| [Baeldung — The DTO Pattern](https://www.baeldung.com/java-dto-pattern) | DTO rationale, benefits, alternatives to MapStruct |
| [Kotlinlang — Data Classes](https://kotlinlang.org/docs/data-classes.html) | Kotlin data class features ideal for DTOs (auto-generated toString, copy, destructuring) |
| [Baeldung — MapStruct](https://www.baeldung.com/mapstruct) | When your DTO mapping gets complex enough to need a library |
| [Spring Boot Best Practices](https://docs.spring.io/spring-boot/reference/best-practices.html) | Official guidance on layering and DTO usage |

### What to Read

1. **Baeldung DTO Pattern** — Read fully. Focus on "DTOs vs Domain Objects" and "The Pattern Itself" sections. The key insight: a DTO is not a domain object — it is purely a data carrier between layers.
2. **Kotlinlang Data Classes** — Read fully. This is your DTO implementation tool. Understand default values for optional fields — they enable Jackson deserialization without constructors.
3. **Spring Boot Best Practices** — Read the "Data Access" and "Web" sections. This confirms the official guidance: keep entities out of controllers.

### Practical

Kotlin data classes are so concise that for simple DTOs, no mapping library is needed at this stage. Save MapStruct for Week 6 when the complexity justifies it.

**Request DTO — CreateTaskRequest**
```kotlin
package com.example.taskapi.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateTaskRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    val title: String,

    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    val description: String? = null,

    @field:Size(max = 50, message = "Tags must not exceed 50 characters each")
    val tags: List<String> = emptyList()
)
```

**Response DTO — TaskResponse**
```kotlin
package com.example.taskapi.dto

import java.time.LocalDateTime

data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val completed: Boolean,
    val tags: List<String>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
```

**Mapper — TaskMapper (plain Kotlin, no library needed yet)**
```kotlin
package com.example.taskapi.mapper

import com.example.taskapi.dto.CreateTaskRequest
import com.example.taskapi.dto.TaskResponse
import com.example.taskapi.entity.Task

object TaskMapper {

    fun toResponse(task: Task): TaskResponse = TaskResponse(
        id = task.id,
        title = task.title,
        description = task.description,
        completed = task.completed,
        tags = task.tags,
        createdAt = task.createdAt,
        updatedAt = task.updatedAt
    )

    fun toEntity(request: CreateTaskRequest): Task = Task(
        title = request.title,
        description = request.description,
        tags = request.tags,
        completed = false
    )
}
```

### Audit Checkpoint

Before moving to Wednesday, verify you can answer:
- Why should JPA entities never be used as @RequestBody or @ResponseBody types?
- What Kotlin feature makes data classes ideal for DTOs?
- Where does the mapper sit in a layered architecture (controller -> mapper -> service -> repository)?

---

## Wednesday — Bean Validation (JSR-380)

### Why This Day

Input validation is not optional in production APIs. Every endpoint that accepts user input must validate it before processing. Spring integrates with Jakarta Bean Validation (JSR-380), formerly javax.validation.

### Study Resources

| Resource | What It Covers |
|----------|----------------|
| [Baeldung — Bean Validation](https://www.baeldung.com/spring-boot-bean-validation) | @Valid, @Validated, validation annotations |
| [Spring Framework — @ExceptionHandler](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-exceptionhandler.html) | Full @ExceptionHandler reference, supported arguments and return types |
| [Jakarta Bean Validation 3.0 Spec](https://jakarta.ee/specifications/bean-validation/3.0/jakarta-bean-validation-spec-3.0.html) | Official constraint annotations reference (@NotNull, @Size, @Email, @Min, @Max) |
| [Spring Boot Validation How-To](https://docs.spring.io/spring-boot/how-to/validation.html) | Official Spring Boot validation guide |

### What to Read

1. **Baeldung Bean Validation** — Read the "Simple Validation" and "Validation Errors" sections. Focus on how @Valid triggers validation and how MethodArgumentNotValidException is thrown when validation fails.
2. **Spring Framework @ExceptionHandler** — This is the definitive reference. Read it end-to-end. It covers all supported method arguments for @ExceptionHandler methods.
3. **Jakarta Bean Validation 3.0 Spec** — Read the built-in constraint table of contents. You only need @NotNull, @Size, @Email, @Min, @Max for this project. Skim the rest.
4. **Spring Boot Validation How-To** — Read fully. Confirms the official approach for enabling validation in Spring Boot 3.x.

### Practical

**Validation Annotations Reference**
```kotlin
package com.example.taskapi.dto

import jakarta.validation.constraints.*

data class CreateTaskRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    val title: String,

    @field:Size(max = 1000, message = "Description must not exceed 1000 characters")
    val description: String? = null,

    @field:Min(value = 0, message = "Priority must be at least 0")
    @field:Max(value = 10, message = "Priority must not exceed 10")
    val priority: Int = 0,

    @field:Email(message = "Invalid email format")
    val assigneeEmail: String? = null
)
```

**Controller with @Valid**
```kotlin
package com.example.taskapi.controller

import com.example.taskapi.dto.CreateTaskRequest
import com.example.taskapi.dto.TaskResponse
import com.example.taskapi.mapper.TaskMapper
import com.example.taskapi.service.TaskService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tasks")
class TaskController(
    private val taskService: TaskService
) {

    @PostMapping
    fun createTask(@Valid @RequestBody request: CreateTaskRequest): ResponseEntity<TaskResponse> {
        val task = taskService.create(TaskMapper.toEntity(request))
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(TaskMapper.toResponse(task))
    }

    @GetMapping("/{id")
    fun getTask(@PathVariable id: Long): ResponseEntity<TaskResponse> {
        val task = taskService.findById(id)
        return ResponseEntity.ok(TaskMapper.toResponse(task))
    }
}
```

### Audit Checkpoint

Before moving to Thursday, verify you can answer:
- What is the difference between @Valid and @Validated?
- Which exception is thrown when @Valid fails? Where does it come from?
- Which validation annotation would you use to ensure a field is both present and between 1-255 characters?
- Can a nested DTO also have validation annotations? How is it triggered?

---

## Thursday — @ControllerAdvice and Global Exception Handling

### Why This Day

Every endpoint repeating its own error handling logic is a maintenance nightmare. @ControllerAdvice lets you centralize exception handling once and have it apply to all controllers. Combined with custom exceptions, this is the foundation of clean error responses.

### Study Resources

| Resource | What It Covers |
|----------|----------------|
| [Spring Framework — Exception Handling](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-exceptionhandler.html) | @ControllerAdvice, @ExceptionHandler, priority ordering, media type mapping |
| [Spring Boot Best Practices — Web](https://docs.spring.io/spring-boot/reference/web/webmvc.html) | Global exception handler patterns |

### What to Read

1. **Spring Framework Exception Handling** — Read fully. The most important section is "Exception Matching" (root vs cause), "Priority ordering" in multi-@ControllerAdvice arrangements, and the table of supported method arguments. This is the canonical reference.
2. **Spring Boot Best Practices Web** — Read the exception handling section. It confirms what Spring Boot officially supports.

### Practical

**Custom Exception — TaskNotFoundException**
```kotlin
package com.example.taskapi.exception

class TaskNotFoundException(id: Long) : RuntimeException("Task with id $id not found")
```

**Validation Exception — ApiValidationException**
```kotlin
package com.example.taskapi.exception

class ApiValidationException(errors: List<String>) : RuntimeException(errors.joinToString("; "))
```

**ErrorResponse DTO**
```kotlin
package com.example.taskapi.dto

import java.time.LocalDateTime

data class ErrorResponse(
    val status: Int,
    val message: String,
    val errors: List<String>? = null,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val path: String? = null
)
```

**Global Exception Handler**
```kotlin
package com.example.taskapi.exception

import com.example.taskapi.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException::class)
    fun handleNotFound(
        ex: TaskNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    status = HttpStatus.NOT_FOUND.value(),
                    message = ex.message ?: "Resource not found",
                    path = request.getDescription(false).replace("uri=", "")
                )
            )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = "Validation failed",
                    errors = errors,
                    path = request.getDescription(false).replace("uri=", "")
                )
            )
    }

    @ExceptionHandler(ApiValidationException::class)
    fun handleApiValidation(
        ex: ApiValidationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = ex.message ?: "Validation failed",
                    path = request.getDescription(false).replace("uri=", "")
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        // Log the exception for debugging, but never expose stack traces in production
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = "An unexpected error occurred",
                    path = request.getDescription(false).replace("uri=", "")
                )
            )
    }
}
```

### When to Return 400 vs 404

| Scenario | HTTP Status | Reasoning |
|----------|-------------|-----------|
| Missing required field | 400 Bad Request | Input is malformed/invalid |
| Field fails validation regex | 400 Bad Request | Input is syntactically wrong |
| Request body has unknown fields | 400 Bad Request | Client sent garbage |
| Resource ID does not exist | 404 Not Found | ID format is valid but resource is absent |
| Request body is empty/malformed JSON | 400 Bad Request | Unparseable input |
| Business rule violation | 422 Unprocessable Entity | Syntax OK but semantics violate rules (use 422 sparingly, 400 is usually fine) |

### Audit Checkpoint

Before moving to Friday, verify you can answer:
- What is the difference between @ExceptionHandler for TaskNotFoundException vs the catch-all Exception?
- Why does the generic Exception handler hide the actual message in production?
- Why is WebRequest passed to build the path field in ErrorResponse?
- What is the HTTP status code for a validation failure with @Valid? For a missing resource?

---

## Friday–Sunday — Build: DTOs, Validation, and Exception Handler

### What to Build

Take the Task CRUD API from Week 2 and refactor it to use DTOs everywhere. This is a refactoring week, not a new-features week.

### Steps

1. **Create DTO package** at `src/main/kotlin/com/example/taskapi/dto/`
2. **Create request DTOs**: CreateTaskRequest, UpdateTaskRequest
3. **Create response DTOs**: TaskResponse, TaskListResponse
4. **Create mapper package** at `src/main/kotlin/com/example/taskapi/mapper/` with TaskMapper
5. **Add validation annotations** to request DTOs using jakarta.validation.constraints
6. **Add @Valid to controller** methods that accept @RequestBody
7. **Create custom exception classes** in `src/main/kotlin/com/example/taskapi/exception/`
8. **Create GlobalExceptionHandler** with @ControllerAdvice
9. **Create ErrorResponse** DTO
10. **Update TaskService** to throw TaskNotFoundException when a task is not found
11. **Test all scenarios** with curl (see below)

### Testing Commands

```bash
# Should return 400 — missing title
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"description": "Has no title"}'

# Should return 400 — title too long
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "'"$(printf 'a%.0s' {1..300})"'"}'

# Should return 400 — invalid email
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Valid title", "assigneeEmail": "not-an-email"}'

# Should return 201 — valid request
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Valid task", "priority": 5}'

# Should return 404 — non-existent ID
curl http://localhost:8080/tasks/99999
```

### Resources for the Weekend

| Resource | Topic |
|----------|-------|
| [Baeldung — REST Validation Errors](https://www.baeldung.com/rest-validation-errors) | Structured error response patterns |
| [Baeldung — Spring Exception Handling](https://www.baeldung.com/spring-exceptions) | @ControllerAdvice deep dive |
| [Spring Framework — @ExceptionHandler](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-exceptionhandler.html) | Full reference — keep this open while coding |

### Git Hygiene

- Commit message: `refactor: add DTOs, validation, and global exception handler`
- Tag: `git tag v0.2.0`
- Push: `git push origin main --tags`

---

## Week 3 Audit Checklist

Before starting Week 4, verify you can answer **yes** to all of these:

| # | Question | Answer |
|---|----------|--------|
| 1 | Does every @RequestBody parameter use a DTO (not an entity)? | |
| 2 | Does every @ResponseBody method return a DTO (not an entity)? | |
| 3 | Are all request DTO fields annotated with appropriate validation constraints? | |
| 4 | Is @Valid present on every controller method accepting @RequestBody? | |
| 5 | Does a missing resource return HTTP 404 with a structured ErrorResponse? | |
| 6 | Does invalid input return HTTP 400 with field-level error messages? | |
| 7 | Is there a single @RestControllerAdvice handling all exceptions globally? | |
| 8 | Does the generic Exception handler hide internal messages from clients? | |
| 9 | Does ErrorResponse include status, message, timestamp, errors, and path? | |
| 10 | Does the build pass (`./gradlew build`)? | |
| 11 | Is code pushed to GitHub with appropriate commit? | |

If any answer is **no**, spend 2-3 hours before Week 4 fixing the gap.

---

## Common Pitfalls

### Validating After Save Instead of Before

This is the most common mistake. The flow must be:
1. Receive request
2. **Validate** (@Valid)
3. Map to entity
4. Save to database
5. Return response

If you validate after save, you waste a database round-trip and risk constraint violations leaking as Hibernate exceptions rather than clean 400 responses.

### Not Handling Constraint Violations

Database-level constraints (unique constraints, foreign keys, NOT NULL columns at the DB level) can still fail even after Bean Validation passes. Your @ControllerAdvice must handle at minimum:
- `DataIntegrityViolationException` (for unique/constraint violations at the DB level)
- `EntityNotFoundException` (for JPA-level not found)

### Leaking Stack Traces

The generic `Exception` handler in your @ControllerAdvice must never return `ex.message` or `ex.stackTrace` in the response body. This exposes internal implementation details to clients and is a security risk. Always return a generic message for unexpected exceptions.

### Not Mapping DTOs Before Save

A common mistake is to save the request DTO directly or to manually set entity fields one by one. Use a mapper object with clear `toEntity` and `toResponse` methods. This centralizes mapping logic and makes it testable.

### Forgetting Nested Validation

If a request DTO has a nested object with its own validation constraints (e.g., `AddressDto` inside `CreateTaskRequest`), you must annotate the nested field with `@field:Valid` to trigger cascading validation:

```kotlin
data class CreateTaskRequest(
    @field:NotBlank val title: String,
    @field:Valid
    val address: AddressDto? = null
)
```

### Using 422 When 400 Is Appropriate

HTTP 400 Bad Request is the correct status for most validation failures. HTTP 422 (Unprocessable Entity) is appropriate only when the request is syntactically valid but semantically impossible (e.g., "cancel a task that is already cancelled"). Default to 400 — do not use 422 unless you have a specific reason.

---

## Deep Dive Extras

These are optional — for if a topic confused you or you want more depth:

| Resource | When to Use |
|----------|-------------|
| [Vlad Mihalcea — Fetch Strategy](https://vladmihalcea.com/hibernate-facts-the-importance-of-fetch-strategy/) | Understand why DTOs prevent lazy loading issues — read if Week 2 JPA concepts felt fuzzy |
| [Baeldung — Custom Validator](https://www.baeldung.com/custom-validator) | Need a validation rule that goes beyond built-in annotations |
| [Spring Framework — Problem Detail (RFC 9457)](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-exceptionhandler.html) | For more structured error responses aligned with RFC 9457 |
| [Jakarta Bean Validation — Payload](https://jakarta.ee/specifications/bean-validation/3.0/jakarta-bean-validation-spec-3.0.html) | Using validation payload for severity, localization, etc. |

---

## Phase 1 Week 3 Summary

| Day | Topic | Hands-On Deliverable |
|-----|-------|---------------------|
| Tue | DTO Pattern + Kotlin Data Classes | CreateTaskRequest, TaskResponse DTOs and TaskMapper |
| Wed | Bean Validation (JSR-380) | @Valid, @NotBlank, @Size, @Email on request DTOs |
| Thu | @ControllerAdvice + Global Exception Handler | GlobalExceptionHandler with custom exceptions |
| Fri-Sun | Build | Full DTO/refactor/validation/exception-handler implementation, tested with curl |
