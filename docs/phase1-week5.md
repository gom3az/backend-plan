# Phase 1 — Week 5: Pagination, Filtering, Sorting

**Goal**: Build production-quality query endpoints. Handle large datasets gracefully.

**Portfolio deliverable**: Paginated, filterable, sortable task list endpoint.
```
GET /tasks?page=0&size=20&status=open&sort=dueDate,asc
```

---

## Tuesday — Offset Pagination vs Cursor-Based Pagination

### Why This Day

Before writing a single line of code, you must choose the right pagination strategy. Offset pagination (`LIMIT 10 OFFSET 30`) is simple but degrades on large tables. Cursor-based pagination (`WHERE id > $lastId`) is constant-time but requires stable sort keys. The wrong choice causes production incidents.

### Study Resources

| # | Resource | What It Covers |
|---|----------|----------------|
| 1 | [Spring Boot GraphQL Pagination — Baeldung](https://www.baeldung.com/spring-boot-graphql-pagination) | Page-based vs cursor-based pagination with code examples |
| 2 | [Stripe — API Pagination](https://stripe.com/docs/api/pagination) | Pagination patterns from a real API provider |
| 3 | [Java JDBC Pagination — Baeldung](https://www.baeldung.com/java-jdbc-pagination) | Cursor vs offset pagination in plain Java |
| 4 | [Hibernate Pagination — Baeldung](https://www.baeldung.com/hibernate-pagination) | How Hibernate handles pagination under the hood |

### What to Read

1. **Resource #1** — Read the "Page-based and cursor-based pagination" section. The key insight: cursor pagination uses stable sort keys (typically `id` or `created_at`) to avoid counting rows. Offset pagination uses `LIMIT/OFFSET` which counts all preceding rows.

2. **Resource #2** — Stripe's philosophy: use cursor pagination when data changes frequently (real-time feeds, financial transactions). Use offset pagination when users need to jump to arbitrary pages (search results, reports).

3. **Resource #4** — Hibernate translates `Pageable` to SQL `LIMIT/OFFSET`. Understanding this helps debug N+1 and count queries.

### Practical: Offset vs Cursor in SQL

```sql
-- Offset pagination (degrades at scale)
SELECT * FROM tasks ORDER BY due_date LIMIT 20 OFFSET 40;
-- The database scans 60 rows to return 20.

-- Cursor pagination (constant time)
SELECT * FROM tasks WHERE id > 1000 ORDER BY id LIMIT 20;
-- The database scans exactly 20 rows.
```

### When to Use Which

| Scenario | Strategy |
|----------|----------|
| User can jump to page 50 of results | Offset (`Page<T>`) |
| Infinite scroll / real-time feeds | Cursor (`Slice<T>`) |
| Data changes frequently (rows inserted/deleted) | Cursor |
| Stable sort with `ORDER BY created_at` | Cursor |
| Need total count for UI pagination controls | Offset (`Page<T>`) |
| First page loads, then sequential navigation | Either |

### Audit Checkpoint

Before moving to Wednesday, verify you can answer:

- Why does `OFFSET 10000` perform worse than `OFFSET 0` even with the same result set size?
- What makes a sort key "stable" and why is that required for cursor pagination?
- When would you choose `Page<T>` over `Slice<T>` in a task list API?

---

## Wednesday — Spring Data Pageable, Page, Slice

### Why This Day

Spring Data JPA has two pagination return types that look similar but have critical performance differences. `Page<T>` runs a count query. `Slice<T>` does not. On a table with 10 million rows, the count query is expensive.

### Study Resources

| # | Resource | What It Covers |
|---|----------|----------------|
| 1 | [Spring Data JPA — Core Concepts (docs.spring.io)](https://docs.spring.io/spring-data/jpa/reference/repositories/core-concepts.html) | `PagingAndSortingRepository`, `Pageable`, `Page`, `Slice` |
| 2 | [Spring Data JPA — Query Return Types Reference](https://docs.spring.io/spring-data/jpa/reference/repositories/query-return-types-reference.html) | Page vs Slice differences |
| 3 | [Spring Boot GraphQL Pagination — Baeldung](https://www.baeldung.com/spring-boot-graphql-pagination) | Code examples for `PageRequest.of()` |

### What to Read

1. **Resource #1** — Read the `PagingAndSortingRepository` section. The interface provides:
   - `Page<T> findAll(Pageable pageable)` — returns paginated results with total count
   - `Iterable<T> findAll(Sort sort)` — returns sorted results without pagination

2. **Resource #2** — The table comparing `Slice<T>` vs `Page<T>`:
   - `Slice<T>` — sized chunk, `hasNext()`, no count query
   - `Page<T>` — extends `Slice<T>`, adds `getTotalElements()`, `getTotalPages()`, executes count query

### Practical: Repository Methods

```kotlin
// In TaskRepository.kt
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.domain.Sort
import org.springframework.data.repository.PagingAndSortingRepository
import com.example.taskapi.model.Task

interface TaskRepository : PagingAndSortingRepository<Task, Long> {

    // Returns Page — count query executed for totalElements/totalPages
    Page<Task> findByStatus(String status, Pageable pageable)

    // Returns Slice — no count query, lighter weight
    Slice<Task> findByAssignee(String assignee, Pageable pageable)

    // Custom sort — multiple fields
    Page<Task> findByStatus(String status, Sort sort, Pageable pageable)
}
```

### Practical: Building PageRequest

```kotlin
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

// Page 0 (first), size 20, sort by dueDate ascending
val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "dueDate"))

// Page 2, size 10, sort by priority descending, then dueDate ascending
val pageable = PageRequest.of(2, 10, Sort.by(
    Sort.Direction.DESC, "priority",
    Sort.Direction.ASC, "dueDate"
))
```

### Practical: Response Envelope

```kotlin
// What your GET /tasks endpoint returns
data class PagedResponse<T>(
    val content: List<T>,
    val page: PageMetadata
)

data class PageMetadata(
    val totalElements: Long,
    val totalPages: Int,
    val number: Int,       // 0-indexed
    val size: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

@RestController
class TaskController(private val taskRepository: TaskRepository) {

    @GetMapping("/tasks")
    fun getTasks(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "dueDate") sort: String,
        @RequestParam(defaultValue = "asc") direction: String
    ): PagedResponse<Task> {
        val sortDirection = if (direction.equals("asc", ignoreCase = true)) 
            Sort.Direction.ASC else Sort.Direction.DESC
        val pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort))
        
        val pageResult = taskRepository.findAll(pageable)
        
        return PagedResponse(
            content = pageResult.content,
            page = PageMetadata(
                totalElements = pageResult.totalElements,
                totalPages = pageResult.totalPages,
                number = pageResult.number,
                size = pageResult.size,
                hasNext = pageResult.hasNext(),
                hasPrevious = pageResult.hasPrevious()
            )
        )
    }
}
```

### Audit Checkpoint

- Why does `Page<T>.getTotalElements()` require a second SQL query?
- When would you prefer `Slice<T>` over `Page<T>` for a mobile app backend?
- What does `PageRequest.of(0, 20)` mean in 0-indexed pagination?

---

## Thursday — JPA Specifications for Dynamic Filtering

### Why This Day

Hardcoding query methods like `findByStatus()` works until you need `findByStatusAndDueDateBetweenAndAssignee()`. The combinations explode. JPA Specifications (built on CriteriaBuilder) let you build WHERE clauses dynamically at runtime — essential for production filterable APIs.

### Study Resources

| # | Resource | What It Covers |
|---|----------|----------------|
| 1 | [Spring Data Criteria Queries — Baeldung](https://www.baeldung.com/spring-data-criteria-queries) | Building dynamic queries with `Specification<T>` |
| 2 | [Spring Data JPA Specifications — Baeldung](https://www.baeldung.com/spring-data-jpa-specifications) | `Specification<T>` pattern with code examples |

### What to Read

1. **Resource #1** — Read the `Specification<T>` section. The pattern is:
   ```kotlin
   interface TaskRepository : JpaRepository<Task, Long>,
                               JpaSpecificationExecutor<Task>
   
   // Usage in service layer
   val spec = Specification.where<Task> { root, _, cb ->
       cb.equal(root.get<String>("status"), "OPEN")
   }.and { root, _, cb ->
       cb.greaterThan(root.get<>("dueDate"), LocalDate.now())
   }
   ```

2. **Resource #2** — Focus on combining multiple `Specification` objects. This is the key pattern for composable filters.

### Practical: TaskSpecifications.kt

```kotlin
import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification
import com.example.taskapi.model.Task
import java.time.LocalDate

object TaskSpecifications {

    fun statusEquals(status: String): Specification<Task> {
        return Specification { root, _, cb ->
            cb.equal(root.get<String>("status"), status.uppercase())
        }
    }

    fun dueDateAfter(date: LocalDate): Specification<Task> {
        return Specification { root, _, cb ->
            cb.greaterThan(root.get("dueDate"), date)
        }
    }

    fun dueDateBefore(date: LocalDate): Specification<Task> {
        return Specification { root, _, cb ->
            cb.lessThan(root.get("dueDate"), date)
        }
    }

    fun assigneeEquals(assignee: String): Specification<Task> {
        return Specification { root, _, cb ->
            cb.equal(root.get<String>("assignee"), assignee)
        }
    }

    fun hasTagsContaining(tag: String): Specification<Task> {
        return Specification { root, _, cb ->
            cb.isTrue(root.join<Task, List<String>>("tags").`in`(listOf(tag)))
        }
    }

    // Combine multiple specs
    fun withFilters(
        status: String?,
        assignee: String?,
        dueDateFrom: LocalDate?,
        dueDateTo: LocalDate?
    ): Specification<Task> {
        return Specification.where<Task> { root, _, cb ->
            val predicates = mutableListOf<Predicate>()
            
            status?.let {
                predicates.add(cb.equal(root.get<String>("status"), it.uppercase()))
            }
            assignee?.let {
                predicates.add(cb.equal(root.get<String>("assignee"), it))
            }
            dueDateFrom?.let {
                predicates.add(cb.greaterThan(root.get("dueDate"), it))
            }
            dueDateTo?.let {
                predicates.add(cb.lessThan(root.get("dueDate"), it))
            }
            
            cb.and(*predicates.toTypedArray())
        }
    }
}
```

### Practical: Controller with Query Params

```kotlin
@GetMapping("/tasks")
fun getTasks(
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "20") size: Int,
    @RequestParam(defaultValue = "dueDate") sort: String,
    @RequestParam(defaultValue = "asc") direction: String,
    @RequestParam(required = false) status: String?,
    @RequestParam(required = false) assignee: String?,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dueDateFrom: LocalDate?,
    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dueDateTo: LocalDate?
): PagedResponse<Task> {
    val sortDirection = if (direction.equals("asc", ignoreCase = true))
        Sort.Direction.ASC else Sort.Direction.DESC
    
    val pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort))
    
    val spec = TaskSpecifications.withFilters(status, assignee, dueDateFrom, dueDateTo)
    
    val pageResult = taskRepository.findAll(spec, pageable)
    
    return PagedResponse(
        content = pageResult.content,
        page = PageMetadata(
            totalElements = pageResult.totalElements,
            totalPages = pageResult.totalPages,
            number = pageResult.number,
            size = pageResult.size,
            hasNext = pageResult.hasNext(),
            hasPrevious = pageResult.hasPrevious()
        )
    )
}
```

### Audit Checkpoint

- How does `Specification.where {}` differ from `Specification {}`?
- What happens if you pass `null` to `statusEquals(null)`?
- Why is it important to combine predicates with `cb.and()` rather than chaining `.and()` on Specification?

---

## Friday–Sunday — Database Indexes + Flyway Migration + Performance

### Why This Weekend

Pagination without indexes is a table scan. On a tasks table with 1 million rows, `ORDER BY dueDate LIMIT 20 OFFSET 100000` is catastrophic. You must add indexes for columns you filter and sort by.

### Study Resources

| # | Resource | What It Covers |
|---|----------|----------------|
| 1 | [Baeldung — Database Indexes in Spring Boot](https://www.baeldung.com/database-indexes-spring-boot) | Adding indexes via JPA `@Index` annotation |
| 2 | [Spring Data JPA Reference — Core Concepts](https://docs.spring.io/spring-data/jpa/reference/repositories/core-concepts.html) | Repository query method patterns |
| 3 | [Flyway Documentation](https://flywaydb.org/documentation/) | Migration-based database versioning |

### What to Read

1. **Resource #1** — Focus on the `@Index` annotation and how it translates to DDL. Key:
   ```java
   @Entity
   @Table(name = "tasks", indexes = {
       @Index(name = "idx_tasks_status", columnList = "status"),
       @Index(name = "idx_tasks_due_date", columnList = "due_date"),
       @Index(name = "idx_tasks_assignee", columnList = "assignee"),
       @Index(name = "idx_tasks_status_due_date", columnList = "status, due_date") // composite
   })
   class Task { ... }
   ```

2. **Resource #3** — Flyway uses numbered migrations (`V1__*, V2__*`). Each migration is a SQL file applied in order. This is how you add indexes to an existing production database safely.

### Practical: Flyway Migration for Indexes

```sql
-- src/main/resources/db/migration/V2__add_pagination_indexes.sql
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'OPEN';
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS assignee VARCHAR(100);
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS due_date TIMESTAMP;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS tags TEXT[];

-- Composite index for the common query pattern: filter by status, sort by dueDate
CREATE INDEX IF NOT EXISTS idx_tasks_status_due_date ON tasks(status, due_date);

-- Index for assignee filter
CREATE INDEX IF NOT EXISTS idx_tasks_assignee ON tasks(assignee);

-- Index for tag filtering (if using array column)
CREATE INDEX IF NOT EXISTS idx_tasks_tags ON tasks USING GIN(tags);
```

### Practical: Updated Task Entity with Indexes

```kotlin
import jakarta.persistence.*
import org.hibernate.annotations.Index
import java.time.LocalDateTime

@Entity
@Table(name = "tasks", indexes = [
    Index(name = "idx_tasks_status", columnList = "status"),
    Index(name = "idx_tasks_due_date", columnList = "due_date"),
    Index(name = "idx_tasks_assignee", columnList = "assignee"),
    Index(name = "idx_tasks_status_due_date", columnList = "status, due_date")
])
class Task(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var title: String = "",

    @Column(length = 20)
    var status: String = "OPEN",

    @Column(name = "due_date")
    var dueDate: LocalDateTime? = null,

    @Column(length = 100)
    var assignee: String? = null
)
```

### Practical: Verify Index Usage with EXPLAIN

```sql
-- Before adding index (full table scan — bad)
EXPLAIN SELECT * FROM tasks WHERE status = 'OPEN' ORDER BY due_date LIMIT 20 OFFSET 0;

-- After adding index (index scan — good)
CREATE INDEX idx_tasks_status_due_date ON tasks(status, due_date);
EXPLAIN SELECT * FROM tasks WHERE status = 'OPEN' ORDER BY due_date LIMIT 20 OFFSET 0;
```

### Performance Pitfalls to Avoid

| Pitfall | Symptom | Solution |
|---------|---------|----------|
| Missing index on filter column | `Seq Scan on tasks` in EXPLAIN | Add `@Index(columnList = "status")` |
| Missing index on sort column | Filesort in EXPLAIN | Add index on sort column or composite |
| Loading entire table | `getTotalElements()` times out | Use `Slice<T>` instead of `Page<T>` |
| N+1 on lazy collections | One query per task for tags | Use `fetch join` or `@EntityGraph` |
| OFFSET on large numbers | Degraded performance at high page numbers | Switch to cursor pagination |
| Count query on every request | Double query time | Cache count or use `Slice<T>` |

### Deep Dive Extras

#### Cursor Pagination Implementation

If you need cursor pagination (e.g., infinite scroll), use a keyset:

```kotlin
@GetMapping("/tasks")
fun getTasksCursor(
    @RequestParam(required = false) afterId: Long?,
    @RequestParam(defaultValue = "20") size: Int,
    @RequestParam(defaultValue = "dueDate") sort: String,
    @RequestParam(defaultValue = "asc") direction: String
): List<Task> {
    val sortDirection = if (direction.equals("asc", ignoreCase = true)) 
        Sort.Direction.ASC else Sort.Direction.DESC
    
    val sort = Sort.by(sortDirection, sort)
    val pageable = PageRequest.of(0, size + 1, sort) // +1 to check hasNext
    
    // If afterId is null, start from beginning
    // If afterId is provided, find that task and start after it
    val spec = if (afterId != null) {
        Specification.where<Task> { root, _, cb ->
            cb.greaterThan(root.get<Long>("id"), afterId)
        }
    } else {
        Specification.where<Task> { _, _, _ -> null }
    }
    
    val slice = taskRepository.findAll(spec, pageable)
    val tasks = slice.content
    val hasMore = tasks.size > size
    
    return if (hasMore) tasks.dropLast(1) else tasks
}
```

#### EntityGraph for Avoiding N+1

```kotlin
@PersistenceContext
private lateinit var entityManager: EntityManager

fun findTasksWithTags(pageable: Pageable): Page<Task> {
    val graph = entityManager.getEntityGraph("Task.withTags")
    return taskRepository.findAll(
        Specification.where<Task> { root, _, _ -> null },
        pageable
    ).map { task ->
        // Accessing tags triggers fetch if using EntityGraph
        task.tags
        task
    }
}
```

### Resources for the Weekend

| Resource | Why |
|----------|-----|
| [Spring Data JPA — Repository Query Methods](https://docs.spring.io/spring-data/jpa/reference/repositories/query-methods.html) | Derive queries from method names (`findByStatus`) |
| [Flyway — Quick Start](https://flywaydb.org/documentation/quickstart/) | Set up migrations in your project |
| [PostgreSQL EXPLAIN ANALYZE](https://www.postgresql.org/docs/current/using-explain.html) | Analyze query plans |
| [Baeldung — JPA Entity Graph](https://www.baeldung.com/jpa-entity-graph) | Avoid N+1 with `@NamedEntityGraph` |

---

## Week 5 Audit Checklist

Before starting Week 6, verify you can answer **yes** to all of these:

| # | Question | ✓ |
|---|----------|---|
| 1 | Can you explain why `OFFSET 100000` is slow even with `LIMIT 20`? | |
| 2 | Do you know the performance difference between `Page<T>` and `Slice<T>`? | |
| 3 | Can you build a `PageRequest.of(page, size, Sort.by(...))` from query params? | |
| 4 | Can you implement dynamic filtering using `Specification<T>` with multiple optional params? | |
| 5 | Do you know how to add indexes using both JPA `@Index` and Flyway SQL migrations? | |
| 6 | Can you design a composite index for a query like `WHERE status = ? ORDER BY dueDate`? | |
| 7 | Have you identified which lazy collections in your Task entity could cause N+1? | |
| 8 | Does your `/tasks` endpoint return a proper response envelope with page metadata? | |
| 9 | Can you switch between `Slice<T>` and `Page<T>` based on whether the UI needs total count? | |
| 10 | Is your pagination code tested with at least 3 different page/size combinations? | |

---

## Phase 1 Week 5 Summary

| Day | Topic | Hands-On Deliverable |
|-----|-------|---------------------|
| Tue | Offset vs Cursor Pagination | Document decision tree for choosing pagination type |
| Wed | Spring Data Pageable/Page/Slice | TaskRepository with `Page<Task>`, `Slice<Task>` methods |
| Thu | JPA Specifications | `TaskSpecifications` composable filter builder |
| Fri-Sun | Indexes + Flyway + Performance | `V2__add_pagination_indexes.sql` migration, entity indexes |

---

## Phase 1 — Week 5 Quick Reference

```kotlin
// 1. PageRequest building
val pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "dueDate"))

// 2. Repository method returning Page
Page<Task> findByStatus(String status, Pageable pageable)

// 3. Specification composable filter
Specification.where<Task> { root, _, cb ->
    cb.and(*predicates.toTypedArray())
}

// 4. Response envelope
data class PagedResponse<T>(
    val content: List<T>,
    val page: PageMetadata
)

// 5. JPA Index annotation
@Index(name = "idx_tasks_status_due_date", columnList = "status, due_date")

// 6. Flyway migration naming
// V2__add_pagination_indexes.sql
```
