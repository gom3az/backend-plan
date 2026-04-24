# Phase 1 — Week 1: Spring Boot Project Setup + REST Fundamentals

**Goal**: Get a bare-bones Spring Boot Kotlin app running with a REST controller. Understand how Spring Boot bootstraps and auto-configures.

**Portfolio deliverable**: A task API with GET/POST endpoints returning JSON. Pushed to GitHub by end of week.

---

## Tuesday — Kotlin Coroutines Deep Dive

### Why This Week

You already know Kotlin syntax from Android. But Spring Boot is synchronous by default — understanding coroutines matters for:
1. Spring WebFlux (reactive) which you may encounter in Phase 2-3
2. Writing non-blocking service layer code even in Spring MVC
3. Kotlin Flow for streaming responses

### Study Resources

| # | Resource | What You'll Learn |
|---|----------|-------------------|
| 1 | [Guide to Kotlin Coroutines by Example](https://kotlinlang.org/docs/coroutines-guide.html) | Core concepts: suspend, launch, async, structured concurrency |
| 2 | [Kotlin Coroutines API Reference](https://kotlinlang.org/api/kotlinx.coroutines/) | Full API — use this as your lookup reference |
| 3 | [Kotlin Coroutines — Deep Dive](https://kotlinlang.org/docs/coroutines-guide.html) | Comprehensive guide covering Flow, Channels, structured concurrency — equivalent to a deep dive video |
| 4 | [Kotlinlang — Server Overview](https://kotlinlang.org/docs/server-overview.html) | Why Kotlin backend, Spring vs Ktor vs other frameworks, AWS SDK for Kotlin |

### What to Actually Read

- Read **Resource #1** fully. The examples are in Kotlin — type them out, don't just skim.
- Skip ahead to **Flow** and **Channels** sections — you use these in Phase 2.
- **Resource #4** is short — read it to understand the landscape. You're using Spring Boot, not Ktor. But know that coroutines are shared knowledge.

### Practical

```kotlin
// From the coroutines guide — type this, run it, break it, fix it
import kotlinx.coroutines.*

fun main() = runBlocking {
    val time = measureTimeMillis {
        val one = async { doSomethingUsefulOne() }
        val two = async { doSomethingUsefulTwo() }
        println("The answer is ${one.await() + two.await()}")
    }
    println("Completed in $time ms")
}

suspend fun doSomethingUsefulOne(): Int {
    delay(1000L)
    return 13
}

suspend fun doSomethingUsefulTwo(): Int {
    delay(1000L)
    return 29
}
```

### Audit Checkpoint

Before moving on, can you answer:
- What does `suspend` mean at the JVM bytecode level?
- What's the difference between `launch` and `async`?
- Why does `runBlocking` exist and when would you **never** use it in production code?

---

## Wednesday — Spring Boot REST API with Kotlin

### Why This Day

Spring Boot is the framework. Understanding how it bootstraps (auto-configuration, starters) is the difference between following tutorials and knowing why your app works.

### Study Resources

| # | Resource | What You'll Learn |
|---|----------|-------------------|
| 1 | [Building REST Services with Spring](https://spring.io/guides/tutorials/rest/) | The canonical Spring REST guide — has Kotlin equivalents |
| 2 | [Getting Started — REST Service (gs-rest-service)](https://spring.io/guides/gs/rest-service/) | Simpler, step-by-step. Kotlin data class + controller |
| 3 | [Spring Initializr](https://start.spring.io/#!language=kotlin) | Bootstrap your project — add dependencies visually |
| 4 | [Spring Boot Reference Docs — Features](https://docs.spring.io/spring-boot/reference/) | The authoritative reference — bookmark this |

### What to Actually Read

- **Resource #1** — Read the first 3 sections: "The Story So Far" (nonrest), "Spring HATEOAS" (rest), "Simplifying Link Creation" (evolution). The GitHub repos are [here](https://github.com/spring-guides/tut-rest). Clone and run `rest` module.
- **Resource #2** — Work through this end-to-end. It's simpler than #1.
- **Resource #4** — Read Chapter 1 (Getting Started) and Chapter 5 (Building REST APIs). Don't read it cover-to-cover.

### Project Setup (Hands-On)

1. Go to [start.spring.io/#!language=kotlin](https://start.spring.io/#!language=kotlin)
2. Add dependencies:
   - Spring Web
   - Spring Data JPA
   - PostgreSQL Driver
   - Flyway
   - Spring Security
   - Spring Boot Actuator
   - Validation
3. Generate, unzip, open in IntelliJ
4. Build with `./gradlew bootRun` before adding any code — verify it starts

### Your First Kotlin Controller

```kotlin
package com.example.taskapi

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TaskController {

    private val tasks = mutableListOf<Task>()

    @GetMapping("/tasks")
    fun getTasks(): List<Task> = tasks

    @PostMapping("/tasks")
    fun createTask(@RequestBody task: Task): Task {
        tasks.add(task)
        return task
    }
}

data class Task(val id: Long, val title: String, val completed: Boolean = false)
```

### Audit Checkpoint

- Can you explain what Spring Boot auto-configures when you add `spring-boot-starter-web`?
- What's the difference between `@RestController` and `@Controller`?
- Where does the JSON serialization happen automatically?

---

## Thursday — Build: Task API v1

### Deliverable

A running API at `http://localhost:8080` with:
- `GET /tasks` — returns a hardcoded list of 3 tasks as JSON
- `POST /tasks` — accepts a task JSON body, adds to list, returns created task with 201

### Steps

1. **Start from zero code** — you already have the scaffold from Wednesday
2. **Add the Task data class** in `src/main/kotlin/com/example/taskapi/model/`
3. **Add the TaskController** in `src/main/kotlin/com/example/taskapi/controller/`
4. **Test with curl**:
   ```bash
   curl http://localhost:8080/tasks
   curl -X POST http://localhost:8080/tasks \
     -H "Content-Type: application/json" \
     -d '{"id":1,"title":"Learn Spring Boot","completed":false}'
   ```
5. **Enable Kotlin DSL for build.gradle.kts** (optional but recommended for Kotlin projects):
   ```kotlin
   import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

   plugins {
       id("org.springframework.boot") version "3.4.4"
       id("io.spring.dependency-management") version "1.1.7"
       kotlin("jvm") version "2.1.0"
       kotlin("plugin.spring") version "2.1.0"
       kotlin("plugin.jpa") version "2.1.0"
   }

   tasks.withType<KotlinCompile> {
       kotlinOptions {
           freeCompilerArgs += "-Xjsr305=strict"
           jvmTarget = "21"
       }
   }
   ```
6. **Push to GitHub** — `task-management-api`, public repo, initial commit

### Reading for Thursday

| Resource | Why |
|----------|-----|
| [Spring Boot Kotlin DSL Gradle Plugin](https://docs.spring.io/spring-boot/kotlin-sdk-buildtools-kotlin-dsl-gradle.html) | Converts your build to idiomatic Kotlin DSL |
| [HTTP Methods Reference (MDN)](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods) | Refresh GET/POST/PUT/DELETE semantics |

---

## Friday–Sunday — Polish, Validation, Error Responses

### What to Add

Extend your TaskController from Thursday:

1. **Input validation** — use `@Valid` and basic bean validation annotations
2. **Proper error responses** — return 400 for bad input, 404 for missing
3. **Response headers** — add `Location` header on 201 Created

```kotlin
@PostMapping("/tasks")
fun createTask(@RequestBody @Valid task: Task, uriComponentsBuilder: UriComponentsBuilder): ResponseEntity<Task> {
    tasks.add(task)
    val uri = uriComponentsBuilder.path("/tasks/{id}").buildAndExpand(task.id).toUri()
    return ResponseEntity.created(uri).body(task)
}
```

### Error Handling — Global Exception Handler

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

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

data class ErrorResponse(val message: String, val status: Int)
```

### Resources for the Weekend

| Resource | Topic |
|----------|-------|
| [Baeldung — Spring Exception Handling](https://www.baeldung.com/spring-exceptions) | @ControllerAdvice, @ExceptionHandler, error DTO patterns |
| [Baeldung — Bean Validation](https://www.baeldung.com/javax-validation) | @NotNull, @Size, @Email — annotate your Task class |
| [Baeldung — REST Pagination](https://www.baeldung.com/spring-data-pagination) | Preview Week 5 — read through it so it registers |

### GitHub Hygiene

- Commit message format: `feat: add GET/POST task endpoints with validation`
- Tag the commit: `git tag v0.1.0`
- Push: `git push origin main --tags`
- Write a minimal README:
  ```markdown
  # Task Management API

  Spring Boot Kotlin REST API — Phase 1, Week 1

  ## Endpoints

  | Method | Path | Description |
  |--------|------|-------------|
  | GET | /tasks | List all tasks |
  | POST | /tasks | Create a task |

  ## Running locally

  ./gradlew bootRun
  ```
- Deploy to Railway free tier if feeling ambitious — or defer to Week 8

---

## Week 1 Audit Checklist

Before starting Week 2, verify you can answer **yes** to all of these:

| # | Question | ✓ |
|---|----------|---|
| 1 | Can you create a Spring Boot Kotlin project from scratch using start.spring.io? | |
| 2 | Do you know the difference between `@Controller` and `@RestController`? | |
| 3 | Can you explain what `spring-boot-starter-web` transitively pulls in? | |
| 4 | Does your API return proper HTTP status codes (200, 201, 400, 404)? | |
| 5 | Is your code pushed to GitHub with a README? | |
| 6 | Can you explain what a `suspend` function does at a basic level? | |
| 7 | Does your build pass (`./gradlew build`)? | |

If any answer is **no**, spend 2-3 hours before Week 2 starts filling the gap.

---

## Additional Resources (Deep Dives)

These are optional — for if a topic confused you or you want more depth:

| Resource | When to Use |
|----------|-------------|
| [Kotlinlang — Coroutines Guide (PDF)](https://kotlinlang.org/docs/coroutines-guide.html) | If you prefer reading over video |
| [Spring Boot — Kotlin Support](https://docs.spring.io/spring-boot/reference/features/kotlin.html) | Convert your build.gradle to build.gradle.kts |
| [IntelliJ IDEA — Debugging](https://www.jetbrains.com/help/idea/debugging.html) | Debug coroutines in the IDE |
| [HTTP Status Codes (MDN)](https://developer.mozilla.org/en-US/docs/Web/HTTP/Status) | Quick reference — bookmark it |
| [REST API Design — Fielding's Dissertation Ch 5](https://ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm) | The original source on REST — read if you want deep understanding, skip if not |
| [Spring Boot Actuator Docs](https://docs.spring.io/spring-boot/reference/actuator/) | Week 2 preview — expose health checks early |

---

## Phase 1 Week 1 Summary

| Day | Topic | Hands-On Deliverable |
|-----|-------|---------------------|
| Tue | Kotlin Coroutines | Type and run 5 coroutine examples from the guide |
| Wed | Spring Boot REST | Project created at start.spring.io, runs with `./gradlew bootRun` |
| Thu | TaskController | GET + POST endpoints, tested with curl, pushed to GitHub |
| Fri-Sun | Validation + Error Handling | @Valid, GlobalExceptionHandler, proper HTTP codes, README |
