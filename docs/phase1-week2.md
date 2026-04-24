# Phase 1 — Week 2: PostgreSQL + Spring Data JPA

**Goal**: Connect the Week 1 Task API to PostgreSQL. Model a Task entity, use Spring Data JPA repositories, add Flyway migrations.

**Portfolio deliverable**: Task CRUD endpoints backed by PostgreSQL with versioned Flyway migration scripts. Pushed to GitHub.

---

## Tuesday — JPA Internals: Entity Lifecycle + Fetch Strategies

### Why This Day

You used Room on Android — JPA is similar but has different defaults and traps. Understanding the entity lifecycle and fetch strategies prevents the most common JPA performance killer: the **N+1 query problem**.

### Study Resources

| Resource | What It Covers |
|----------|----------------|
| [Spring Data JPA — Entity Persistence](https://docs.spring.io/spring-data/jpa/reference/jpa/entity-persistence.html) | How JPA manages entities — persistence context, managed vs detached |
| [Vlad Mihalcea — N+1 Query Problem](https://vladmihalcea.com/n-plus-1-query-problem/) | The most common JPA performance killer — and how to fix it |
| [Vlad Mihalcea — Fetch Strategy](https://vladmihalcea.com/hibernate-facts-the-importance-of-fetch-strategy/) | Lazy vs eager — when each applies and why defaults matter |
| [Vlad Mihalcea — Batch Processing](https://vladmihalcea.com/the-best-way-to-do-batch-processing-with-jpa-and-hibernate/) | Performance when inserting/updating many entities |

### What to Read

- Read **Vlad Mihalcea — N+1 Query Problem** fully. This is the most important JPA article you'll read.
- Read **Spring Data JPA — Entity Persistence** — understand what "managed", "detached", and "removed" mean.
- Skim **Fetch Strategy** — focus on the examples showing the difference between `fetch = FetchType.LAZY` and `EAGER`.

### Key Concepts

```
Entity lifecycle states:
- NEW       → not yet in DB, no ID
- MANAGED   → tracked by persistence context, auto-flush to DB
- DETACHED  → was managed, no longer tracked (returned to controller)
- REMOVED   → marked for deletion, flushed on commit

Fetch strategies:
- LAZY (default for @ManyToOne, @OneToMany) → loads on access, causes N+1
- EAGER (default for @OneToOne, @ManyToOne) → loads immediately, often wasteful
```

### Audit Checkpoint

Before moving on, can you answer:
- What is the N+1 query problem? Draw it with SQL.
- What's the difference between `FetchType.LAZY` and `EAGER`?
- What does "managed" vs "detached" mean in your own words?
- Why might `@ManyToOne` defaulting to EAGER be a problem?

---

## Wednesday — Spring Data JPA + Project Setup

### Why This Day

Now that you understand the internals, connect your Task API to PostgreSQL using Spring Data JPA. Set up Flyway for database migrations from day one.

### Study Resources

| Resource | What It Covers |
|----------|----------------|
| [Spring Data JPA — Getting Started](https://docs.spring.io/spring-data/jpa/reference/jpa/getting-started.html) | Setup, configuration, repository basics |
| [Spring Data JPA — Query Methods](https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html) | Deriving queries from method names — when and how |
| [Flyway — Getting Started](https://documentation.red-gate.com/flyway/getting-started-with-flyway/first-steps-flyway-autopilot-a-beginners-guide) | Migrations concept, V1, V2 naming convention |
| [Flyway — Migrations](https://www.red-gate.com/fd/migrations-273973333.html) | SQL migration syntax, undo, repeatable migrations |

### What to Read

- **Spring Data JPA — Getting Started** — work through the entity + repository setup
- **Query Methods** — focus on method naming conventions (findBy, existsBy, countBy)
- **Flyway Getting Started** — understand versioned migrations V1__, V2__

### Practical: Project Setup

1. Add dependencies to `build.gradle.kts`:
   ```kotlin
   implementation("org.springframework.boot:spring-boot-starter-data-jpa")
   implementation("org.postgresql:postgresql")
   implementation("org.flywaydb:flyway-core")
   implementation("org.flywaydb:flyway-database-postgresql")
   ```

2. Add to `application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/taskdb
   spring.datasource.username=postgres
   spring.datasource.password=password
   spring.jpa.hibernate.ddl-auto=validate
   spring.flyway.locations=classpath:db/migration
   ```

3. Create migration file `src/main/resources/db/migration/V1__create_tasks_table.sql`:
   ```sql
   CREATE TABLE tasks (
       id BIGSERIAL PRIMARY KEY,
       title VARCHAR(255) NOT NULL,
       description TEXT,
       completed BOOLEAN NOT NULL DEFAULT FALSE,
       due_date TIMESTAMP,
       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
   );

   CREATE INDEX idx_tasks_completed ON tasks(completed);
   CREATE INDEX idx_tasks_due_date ON tasks(due_date);
   ```

### Your First JPA Entity

```kotlin
@Entity
@Table(name = "tasks")
data class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val title: String,

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "completed", nullable = false)
    val completed: Boolean = false,

    @Column(name = "due_date")
    val dueDate: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    // JPA requires no-arg constructor — Kotlin generates this as a default
    constructor() : this(0, "", null, false, null, LocalDateTime.now(), LocalDateTime.now())
}
```

### Your First Spring Data Repository

```kotlin
@Repository
interface TaskRepository : JpaRepository<Task, Long> {

    // Spring derives this from method name — no implementation needed
    fun findByCompleted(completed: Boolean): List<Task>

    // Count query
    fun countByCompleted(completed: Boolean): Long

    // Exists check — returns boolean, no fetch
    fun existsByTitle(title: String): Boolean
}
```

### Audit Checkpoint

- Can you run `./gradlew bootRun` with PostgreSQL running and see Flyway apply V1__?
- Can you query `Task` by ID via the repository?
- What happens if you run the app without PostgreSQL running?

---

## Thursday — CRUD Endpoints with JPA

### Why This Day

Replace the in-memory list from Week 1 with real database operations. Every controller method should now hit the repository.

### Study Resources

| Resource | What It Covers |
|----------|----------------|
| [Spring Data JPA — Entity Persistence](https://docs.spring.io/spring-data/jpa/reference/jpa/entity-persistence.html) | Save, delete, flush operations |
| [Spring Data JPA — Transactions](https://docs.spring.io/spring-data/jpa/reference/jpa/transactions.html) | @Transactional — what it does, propagation, isolation levels |
| [Vlad Mihalcea — Identity/Sequence Generators](https://vladmihalcea.com/hibernate-identity-sequence-and-table-sequence-generator/) | ID generation strategies — why BIGSERIAL = IDENTITY in PostgreSQL |

### What to Read

- **Transactions** — understand @Transactional, when it commits, what rollback means
- **Identity/Sequence Generators** — short read, explains ID generation differences

### Refactored TaskController

```kotlin
@RestController
@RequestMapping("/tasks")
class TaskController(
    private val taskRepository: TaskRepository
) {

    @GetMapping
    fun getTasks(): List<TaskResponse> = taskRepository.findAll()
        .map { it.toResponse() }

    @GetMapping("/{id}")
    fun getTask(@PathVariable id: Long): TaskResponse {
        val task = taskRepository.findById(id)
            .orElseThrow { TaskNotFoundException(id) }
        return task.toResponse()
    }

    @PostMapping
    fun createTask(@RequestBody @Valid request: CreateTaskRequest): TaskResponse {
        val task = request.toEntity()
        val saved = taskRepository.save(task)
        return saved.toResponse()
    }

    @PutMapping("/{id}")
    fun updateTask(
        @PathVariable id: Long,
        @RequestBody @Valid request: UpdateTaskRequest
    ): TaskResponse {
        val task = taskRepository.findById(id)
            .orElseThrow { TaskNotFoundException(id) }

        val updated = task.copy(
            title = request.title ?: task.title,
            description = request.description ?: task.description,
            completed = request.completed ?: task.completed,
            dueDate = request.dueDate ?: task.dueDate,
            updatedAt = LocalDateTime.now()
        )
        return taskRepository.save(updated).toResponse()
    }

    @DeleteMapping("/{id}")
    fun deleteTask(@PathVariable id: Long) {
        if (!taskRepository.existsById(id)) {
            throw TaskNotFoundException(id)
        }
        taskRepository.deleteById(id)
    }
}
```

### DTOs

```kotlin
data class CreateTaskRequest(
    @field:NotBlank
    @field:Size(min = 1, max = 255)
    val title: String,

    val description: String? = null,
    val completed: Boolean = false,
    val dueDate: LocalDateTime? = null
)

data class UpdateTaskRequest(
    @field:Size(min = 1, max = 255)
    val title: String? = null,

    val description: String? = null,
    val completed: Boolean? = null,
    val dueDate: LocalDateTime? = null
)

data class TaskResponse(
    val id: Long,
    val title: String,
    val description: String?,
    val completed: Boolean,
    val dueDate: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

fun Task.toResponse() = TaskResponse(
    id = id,
    title = title,
    description = description,
    completed = completed,
    dueDate = dueDate,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CreateTaskRequest.toEntity() = Task(
    title = title,
    description = description,
    completed = completed,
    dueDate = dueDate
)
```

### Audit Checkpoint

- Can you POST a task and retrieve it by ID?
- What SQL does Hibernate log when you call `findAll()`? (Enable SQL logging to see it)
- What happens when you call `deleteById` on a non-existent ID?

---

## Friday–Sunday — Flyway Migrations + Indexes + Testing

### Friday — More Flyway Migrations

Add a second migration to introduce a `users` table (for Week 4 auth):

```sql
-- V2__create_users_table.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
```

**Migration rules:**
1. Never modify an existing migration file — create a new one
2. Each migration must be independently re-runnable (idempotent)
3. Use `CREATE INDEX IF NOT EXISTS` for safety

### Saturday — Database Performance: Indexes

**Add indexes for query performance** — Flyway migration V3:

```sql
-- V3__add_indexes_for_performance.sql
-- Composite index for common query pattern
CREATE INDEX IF NOT EXISTS idx_tasks_completed_due_date
    ON tasks(completed, due_date);

-- Partial index — only for open tasks (smaller, faster)
CREATE INDEX IF NOT EXISTS idx_tasks_open
    ON tasks(id, title, due_date)
    WHERE completed = false;
```

**JPA Index annotation** (on entity):
```kotlin
@Entity
@Table(
    name = "tasks",
    indexes = [
        Index(name = "idx_tasks_completed", columnList = "completed"),
        Index(name = "idx_tasks_due_date", columnList = "due_date")
    ]
)
data class Task { ... }
```

### Sunday — Run Tests Against Real Database

Start PostgreSQL and Flyway, then run the app:

```bash
# Using Docker
docker run -d \
  --name taskdb \
  -e POSTGRES_DB=taskdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 \
  postgres:16-alpine

# Verify Flyway ran
./gradlew bootRun
# Look for: "Successfully applied 3 migrations"

# Run tests
./gradlew test

# Curl test
curl http://localhost:8080/tasks
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Test task","completed":false}'
curl http://localhost:8080/tasks
```

### Enable SQL Logging for Development

```properties
# application.properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true
```

### GitHub Hygiene

```bash
git add .
git commit -m "feat: connect Task API to PostgreSQL with Flyway migrations

- V1: create tasks table with indexes
- V2: create users table for upcoming auth
- V3: add composite and partial indexes
- Add Spring Data JPA TaskRepository
- Refactor TaskController to use repository"
git push origin main
```

---

## Week 2 Audit Checklist

Before starting Week 3, verify you can answer **yes** to all:

| # | Question | ✓ |
|---|----------|---|
| 1 | Can you explain the N+1 query problem? | |
| 2 | Do you know the difference between LAZY and EAGER fetch? | |
| 3 | Does Flyway apply migrations on startup? | |
| 4 | Can you name 3 things wrong with EAGER fetching by default? | |
| 5 | Have you added at least one index for your query patterns? | |
| 6 | Can you run `findAll()`, `findById()`, `save()`, `deleteById()` via repository? | |
| 7 | Does `./gradlew test` pass with a real PostgreSQL running? | |
| 8 | Is your code on GitHub with descriptive commit messages? | |

---

## Common JPA/Hibernate Pitfalls

| Pitfall | Symptom | Solution |
|---------|---------|----------|
| N+1 query problem | 1000 SQL statements for 1 request | Use `JOIN FETCH`, `@EntityGraph`, or batch fetching |
| LazyInitializationException | `LazyInitializationException: could not initialize proxy` | Access lazy collections inside `@Transactional` or use `fetch = FetchType.LAZY` carefully |
| `EAGER` default on `@ManyToOne` | Unintended joins on every query | Always specify `fetch = FetchType.LAZY` explicitly |
| Forgetting `@Transactional` | `EntityNotAttachedException` or no changes saved | Wrap write operations in `@Transactional` |
| `ddl-auto=create-drop` in production | Data loss on every restart | Use `validate` in prod, `create-drop` only in tests |
| Not using Flyway | Schema drift between environments | Every schema change goes through a migration file |
| Missing index | Slow queries on large tables | Check `EXPLAIN ANALYZE` output for seq scans |

---

## Deep Dive Extras

| Resource | When to Use |
|----------|-------------|
| [Vlad Mihalcea — OneToOne Mapping](https://vladmihalcea.com/the-best-way-to-map-a-onetoone-relationship-with-jpa-and-hibernate/) | When you add User → Task relationship in Week 3 |
| [Vlad Mihalcea — Identity/Sequence Generators](https://vladmihalcea.com/hibernate-identity-sequence-and-table-sequence-generator/) | Deep dive on ID generation for PostgreSQL |
| [PostgreSQL EXPLAIN ANALYZE](https://www.postgresql.org/docs/18/sql-explain.html) | How to diagnose slow queries |
| [Baeldung — JPA Repository vs EntityManager](https://www.baeldung.com/jpa-entitymanager) | When to drop to EntityManager for complex queries |
| [Spring Data JPA — Repositories](https://docs.spring.io/spring-data/jpa/reference/repositories/core-concepts.html) | Repository internals |

---

## Week 2 Summary

| Day | Topic | Hands-On Deliverable |
|-----|-------|---------------------|
| Tue | JPA Internals + Fetch Strategies | Read Vlad Mihalcea N+1 article, understand entity lifecycle |
| Wed | Spring Data JPA + Flyway Setup | PostgreSQL running, V1__ migration applied, TaskRepository created |
| Thu | CRUD with JPA Repository | All endpoints hit DB, no in-memory list |
| Fri | More Flyway Migrations | V2__users_table, V3__indexes migration |
| Sat | Database Performance | Composite indexes, partial indexes, EXPLAIN ANALYZE |
| Sun | Run Against Real DB | Tests pass, curl works, pushed to GitHub |
