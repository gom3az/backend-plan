# Phase 1 — Week 7: Caching + API Documentation

**Goal**: Add Redis caching for read endpoints. Document your API with OpenAPI/Swagger.

**Portfolio deliverable**: Swagger UI accessible at `/swagger-ui.html`. GET `/tasks` responses cached with configurable TTL.

**Week 7 is heavy** — caching has many subtle failure modes and API docs are often treated as afterthoughts. Both matter for your portfolio. A backend that performs well and documents itself cleanly is the mark of a senior engineer.

---

## Tuesday — Redis Setup + Cache-Aside Pattern

### Why This Day

Caching is the easiest high-leverage optimization you can add to a read-heavy API. Before writing any code, understand the mental model: **cache-aside** (lazy loading). You only populate the cache when a read actually misses. This avoids over-caching writes that never get read.

### Study Resources

| # | Resource | What It Covers |
|---|----------|----------------|
| 1 | [Spring Boot — Caching](https://docs.spring.io/spring-boot/4.0.6/reference/io/caching.html) | Full caching documentation — @Cacheable, Redis, TTL config |
| 2 | [Spring Data Redis — Reference](https://docs.spring.io/spring-data/redis/reference/) | Redis client setup, Lettuce, RedisTemplate |
| 3 | [Spring Boot Auto-configuration Classes](https://docs.spring.io/spring-boot/4.0.6/reference/appendix/auto-configuration-classes/spring-boot-data-redis.html) | What Spring Boot auto-configures for Redis |
| 4 | [Spring Guides — Caching](https://spring.io/guides/gs/caching/) | Official step-by-step caching guide |
| 5 | [GitHub — springdoc-openapi](https://github.com/springdoc/springdoc-openapi) | The library we'll use for Swagger/OpenAPI |

### What to Read

**Read first:** Resource #1 (the Spring Boot caching page). This is the canonical reference for everything we do this week. Read the entire section — it's not long. Pay special attention to:
- The diagram showing cache-aside flow (read → check cache → miss → load from DB → populate → return)
- The `spring.cache.redis.*` configuration properties
- How `RedisCacheManager` is auto-configured

**Read second:** Resource #4 (Spring Guides caching). It has working code you can copy. Work through it after reading the conceptual material.

**Read third:** Resource #5 (springdoc-openapi GitHub). Scroll through the README — it shows exactly what dependency to add and what URLs are auto-configured.

### Practical

**Step 1 — Add Redis + Caching dependencies**

In `build.gradle.kts`:

```kotlin
dependencies {
    // ... existing dependencies ...
    
    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Caching abstraction (works with Redis, Caffeine, ConcurrentMap, etc.)
    implementation("org.springframework.boot:spring-boot-starter-cache")
    
    // OpenAPI / Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4")
}
```

**Step 2 — Configure `application.yml` (or `application.properties`)**

```yaml
spring:
  application:
    name: task-management-api

  # Redis connection — Lettuce is auto-configured as the client
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms

  # Cache configuration
  cache:
    type: redis
    cache-names: tasks
    redis:
      time-to-live: 5m       # Default TTL for all caches
      prefix: task-api:      # Key prefix to avoid collisions
```

**Step 3 — Enable caching in your main Application class**

```kotlin
package com.example.taskapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching  // ← This triggers cache auto-configuration
class TaskManagementApiApplication

fun main(args: Array<String>) {
    runApplication<TaskManagementApiApplication>(*args)
}
```

**Step 4 — Verify Redis is running locally**

```bash
# If you don't have Redis installed:
docker run -d -p 6379:6379 --name redis redis:latest

# Test connection
docker exec redis redis-cli ping
# Should return: PONG
```

**Step 5 — Create a cache configuration (optional, for custom TTLs per cache)**

```kotlin
package com.example.taskapi.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
class CacheConfig {

    @Bean
    fun cacheManager(connectionFactory: RedisConnectionFactory): CacheManager {
        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    GenericJackson2JsonRedisSerializer()
                )
            )

        val tasksConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))  // tasks cache lives longer
            .prefixCacheNameWith("task-api:")

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withCacheConfiguration("tasks", tasksConfig)
            .build()
    }
}
```

### Audit Checkpoint

Before moving to Wednesday, verify:

- [ ] Redis is running locally (`docker run -d -p 6379:6379 redis`)
- [ ] Your app starts without errors after adding Redis + cache dependencies
- [ ] You can explain the cache-aside pattern: read from cache first → miss → load from DB → populate cache → return
- [ ] You know which Spring Boot auto-configuration class sets up `RedisCacheManager`

---

## Wednesday — @Cacheable, @CacheEvict, @CachePut

### Why This Day

Annotations are how you control caching behavior per-method. Wrong choices here cause stale data or cache bloat. These three are the core:

| Annotation | When to Use | Effect |
|------------|-------------|--------|
| `@Cacheable` | Read data (GET) | Write to cache on method return; read from cache on method call |
| `@CacheEvict` | Delete or update (DELETE/PUT) | Remove entries from cache |
| `@CachePut` | Update that also returns new value | Always execute method, always update cache |

### Study Resources

| # | Resource | What It Covers |
|---|----------|----------------|
| 1 | [Spring Boot Caching Docs](https://docs.spring.io/spring-boot/4.0.6/reference/io/caching.html#io.caching.provider.redis) | Redis cache provider config, TTL per cache |
| 2 | [Spring Cache Abstraction Docs](https://docs.spring.io/spring-framework/reference/integration/cache.html) | @Cacheable, @CacheEvict, @CachePut — full reference |
| 3 | [springdoc-openapi — Annotations](https://github.com/springdoc/springdoc-openapi) | @Operation, @ApiResponse, OpenAPI metadata |

### What to Read

**Read Resource #2** (Spring Framework Cache Abstraction) — it has a table listing all cache annotations and their purpose. Read the section on `注解` (annotations) carefully. Note the difference between:
- `unless` SpEL expression — conditional caching (e.g., `unless = "#result == null"`)
- `key` SpEL expression — custom cache key (default is method + args)

### Practical — Apply Caching to Your TaskController

```kotlin
package com.example.taskapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/tasks")
class TaskController(
    private val taskService: TaskService  // injected, not using in-memory list
) {

    // ✅ CACHED — every call writes result to "tasks" cache, key = "all"
    @GetMapping
    @Cacheable(value = ["tasks"], key = "'all'")
    @Operation(summary = "Get all tasks", description = "Returns all tasks. Response is cached for 10 minutes.")
    @ApiResponse(responseCode = "200", description = "List of all tasks")
    fun getTasks(): List<TaskDto> = taskService.findAll()

    // ❌ NOT CACHED — read from DB, no caching (single resource)
    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    @ApiResponse(responseCode = "200", description = "Task found")
    @ApiResponse(responseCode = "404", description = "Task not found")
    fun getTask(@PathVariable id: Long): ResponseEntity<TaskDto> {
        val task = taskService.findById(id)
        return if (task != null) {
            ResponseEntity.ok(task)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // 🔄 EVICT + UPDATE — evict "tasks::all" cache, then put new entry
    @PostMapping
    @CacheEvict(value = ["tasks"], key = "'all'")
    @CachePut(value = ["tasks"], key = "#result.id.toString()")
    @Operation(summary = "Create a new task")
    @ApiResponse(responseCode = "201", description = "Task created")
    fun createTask(@RequestBody @Valid taskDto: TaskDto): ResponseEntity<TaskDto> {
        val created = taskService.create(taskDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    // 🗑️ EVICT — remove from cache on update
    @PutMapping("/{id}")
    @CacheEvict(value = ["tasks"], key = "'all'")
    @CacheEvict(value = ["tasks"], key = "#id.toString()")
    @Operation(summary = "Update an existing task")
    @ApiResponse(responseCode = "200", description = "Task updated")
    @ApiResponse(responseCode = "404", description = "Task not found")
    fun updateTask(@PathVariable id: Long, @RequestBody @Valid taskDto: TaskDto): ResponseEntity<TaskDto> {
        val updated = taskService.update(id, taskDto)
        return if (updated != null) {
            ResponseEntity.ok(updated)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    // 🗑️ EVICT — clear cache on delete
    @DeleteMapping("/{id}")
    @CacheEvict(value = ["tasks"], key = "'all'", beforeInvocation = true)
    @CacheEvict(value = ["tasks"], key = "#id.toString()", beforeInvocation = true)
    @Operation(summary = "Delete a task")
    @ApiResponse(responseCode = "204", description = "Task deleted")
    fun deleteTask(@PathVariable id: Long): ResponseEntity<Void> {
        taskService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
```

**Key decision:** `beforeInvocation = true` on delete operations. This ensures the cache is cleared even if the method throws — you don't want a failed delete to leave stale data in cache.

### Audit Checkpoint

Before moving to Thursday, verify:

- [ ] You can explain why POST /tasks should evict "tasks::all" but not cache the new task by its ID
- [ ] You know when to use `@CacheEvict(beforeInvocation = true)` vs the default `false`
- [ ] Your GET /tasks is annotated with `@Cacheable` — does your TaskService use coroutines? If so, `@Cacheable` works with suspend functions but requires proper cache config
- [ ] If you call a cached method and the result is `null`, is the `null` value actually cached? (answer: yes, by default — use `unless = "#result == null"` to skip caching nulls)

---

## Thursday — OpenAPI + Swagger UI

### Why This Day

An API without documentation is a liability. Swagger UI at `/swagger-ui.html` is the industry standard for Spring Boot APIs. With `springdoc-openapi`, it's zero-config — you add a dependency and it works.

Your portfolio deliverable for this week is Swagger UI accessible at `/swagger-ui`. This day gets you there.

### Study Resources

| # | Resource | What It Covers |
|---|----------|----------------|
| 1 | [GitHub — springdoc-openapi](https://github.com/springdoc/springdoc-openapi) | Full README — dependency, URLs, configuration |
| 2 | [springdoc-openapi Demos](https://github.com/springdoc/springdoc-openapi-demos) | Working examples with different Spring Boot versions |
| 3 | [Spring Boot — CORS](https://docs.spring.io/spring-boot/4.0.6/reference/web/web.html#web.security.cors) | You'll need CORS config when mobile clients hit your API |

### What to Read

**Read Resource #1** (springdoc-openapi GitHub README) completely. Note these key URLs it auto-configures:
- `/swagger-ui.html` — Swagger UI (the HTML page)
- `/swagger-ui/index.html` — alternative path
- `/v3/api-docs` — OpenAPI 3 spec as JSON
- `/v3/api-docs.yaml` — OpenAPI 3 spec as YAML

Also note: if you're using Spring Security, Swagger UI requires permit rules for those paths.

### Practical

**Step 1 — Verify springdoc-openapi dependency is in place**

If you added it on Tuesday, verify your `build.gradle.kts` has:
```kotlin
implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.4")
```

**Step 2 — Configure OpenAPI metadata in `application.yml`**

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method   # sort endpoints by HTTP method
    tagsSorter: alpha         # sort tags alphabetically
    tryItOutEnabled: true     # enable "Try it out" button in Swagger UI
```

**Step 3 — Add OpenAPI configuration class**

```kotlin
package com.example.taskapi.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI().info(
            Info()
                .title("Task Management API")
                .version("v1.0.0")
                .description(
                    """
                    REST API for task management built with Spring Boot Kotlin.
                    
                    ## Features
                    - CRUD operations on tasks
                    - Redis caching for read endpoints
                    - OpenAPI 3 documentation
                    - Swagger UI at /swagger-ui.html
                    
                    ## Authentication
                    Currently no authentication (demo mode). Production deployment requires
                    Spring Security OAuth2 or JWT authentication.
                    """.trimIndent()
                )
                .contact(
                    Contact()
                        .name("Your Name")
                        .email("you@example.com")
                        .url("https://github.com/yourusername")
                )
                .license(
                    License()
                        .name("MIT")
                        .url("https://opensource.org/licenses/MIT")
                )
        )
    }
}
```

**Step 4 — Add Swagger annotations to your TaskController**

The `@Operation` and `@ApiResponse` annotations you already added in Wednesday's code provide the per-endpoint documentation. But also add a class-level description:

```kotlin
@io.swagger.v3.oas.annotations.tags.Tag(
    name = "Tasks",
    description = "Task management endpoints — Create, Read, Update, Delete tasks"
)
@RestController
@RequestMapping("/tasks")
class TaskController(private val taskService: TaskService) {
    // ...
}
```

**Step 5 — If using Spring Security, permit Swagger UI paths**

```kotlin
package com.example.taskapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }  // disable for REST API
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/actuator/health",
                        "/**"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
        return http.build()
    }
}
```

**Step 6 — Verify it works**

Start your app and visit:
- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) — Swagger UI
- [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs) — JSON spec

You should see your Task endpoints listed with the descriptions you added.

### Audit Checkpoint

Before the weekend:

- [ ] Swagger UI loads at `/swagger-ui.html` in your browser
- [ ] You can expand GET /tasks and see the cached response schema
- [ ] The OpenAPI JSON at `/v3/api-docs` is valid (paste it into [editor.swagger.com](https://editor.swagger.com) to validate)
- [ ] You've added `@Tag` at the controller class level for better grouping

---

## Friday–Sunday — Polish, Cache Invalidation Verification, Generate Client SDK

### Why This Weekend

You need to verify that cache invalidation actually works (not just theorized), and preview the mobile SDK generation that bridges your backend to your Android background.

### Friday — Verify Cache Invalidation End-to-End

The hardest part of caching isn't getting it working — it's verifying it actually invalidates correctly. Do this:

**Step 1: Add cache observability via logs**

In `application.yml`:
```yaml
logging:
  level:
    org.springframework.cache: DEBUG      # logs cache get/put/evict
    com.example.taskapi: DEBUG
```

**Step 2: Test the full cycle with curl**

```bash
# 1. Clear any existing cache (restart Redis or flush)
docker exec redis redis-cli FLUSHDB

# 2. Start your app
./gradlew bootRun

# 3. First GET /tasks — should MISS cache (log shows: "No cache entry for key")
curl -s http://localhost:8080/tasks | jq .

# 4. Second GET /tasks — should HIT cache (log shows cache hit)
curl -s http://localhost:8080/tasks | jq .

# 5. POST new task — should EVICT tasks::all cache
curl -s -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"New task from curl","completed":false}' | jq .

# 6. GET /tasks again — should MISS cache (because list was evicted)
curl -s http://localhost:8080/tasks | jq .

# 7. Check Redis directly — see what keys exist
docker exec redis redis-cli KEYS "*"
```

Watch your app logs during these steps. You should see:
- `Cache miss for key 'tasks::all'` on first GET
- `Cache hit for key 'tasks::all'` on second GET  
- `Invalidating cache key 'tasks::all'` on POST

**Step 3: Add a custom cache manager health indicator**

```kotlin
package com.example.taskapi.config

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.stereotype.Component

@Component
class RedisCacheHealthIndicator(
    private val connectionFactory: RedisConnectionFactory
) : HealthIndicator {
    
    override fun health(): Health {
        return try {
            val connection = connectionFactory.connection
            connection.ping()
            connection.close()
            Health.up()
                .withDetail("cache", "Redis cache is operational")
                .build()
        } catch (e: Exception) {
            Health.down()
                .withDetail("error", e.message)
                .build()
        }
    }
}
```

Add to `application.yml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,caches
  endpoint:
    health:
      show-details: always
```

Now visit `http://localhost:8080/actuator/health` to verify Redis connection.

---

### Saturday — Preview: Generate Client SDK from OpenAPI Spec

One of the most powerful features of OpenAPI is that it can generate client libraries for any language. This is how you'd bridge your backend to your Android app in Phase 2. This is a preview — you're just exploring, not implementing.

**Step 1: Generate a TypeScript client from your OpenAPI spec**

```bash
# Install the OpenAPI generator CLI
npm install -g @openapitools/openapi-generator-cli

# Generate a TypeScript client (what Android/Kotlin would use)
openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs.yaml \
  -g typescript-fetch \
  -o /tmp/task-api-client

# Inspect the generated client
ls /tmp/task-api-client/src/
cat /tmp/task-api-client/src/api/TaskApi.ts
```

**Step 2: Inspect what was generated**

The generator creates:
- `src/api/TaskApi.ts` — typed API methods (getTasks, createTask, etc.)
- `src/models/` — TypeScript interfaces matching your DTOs
- `src/runtime.ts` — fetch-based HTTP client

This is what your Android app's data layer would use — generated, typed, never manually written. The OpenAPI spec is the source of truth.

**Step 3: If you have a Python background, try Python client**

```bash
openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs.yaml \
  -g python \
  -o /tmp/task-api-python
```

This generates a `TaskApi` class you can pip install and use from a Python backend script.

---

### Sunday — Document Your API in README

Add an "API Documentation" section to your README:

```markdown
## API Documentation

### Swagger UI

Interactive API documentation is available at:
**http://localhost:8080/swagger-ui.html**

### OpenAPI Spec

- JSON: http://localhost:8080/v3/api-docs
- YAML: http://localhost:8080/v3/api-docs.yaml

### Authentication

Currently open (no auth) for development. Production deployment requires JWT/OAuth2.

### Rate Limiting

Currently none. Production should implement rate limiting via bucket4j or similar.
```

---

## Week 7 Audit Checklist

Complete this before starting Week 8. All items must be **yes**.

| # | Question | ✓ |
|---|----------|---|
| 1 | Does GET /tasks return cached responses (check logs for "Cache hit")? | |
| 2 | Does POST /tasks evict the "tasks::all" cache entry? | |
| 3 | Does DELETE /tasks evict both the single task and the list cache? | |
| 4 | Is Swagger UI accessible at `/swagger-ui.html`? | |
| 5 | Does the OpenAPI JSON spec validate at editor.swagger.com? | |
| 6 | Does `/actuator/health` show Redis as UP? | |
| 7 | Have you generated a TypeScript client SDK from your OpenAPI spec? | |
| 8 | Is your code pushed to GitHub with cache invalidation tested and documented? | |
| 9 | Can you explain what a "cache stampede" is and how @Cacheable prevents it? | |
| 10 | Can you explain why `beforeInvocation = true` matters on delete operations? | |

---

## Caching Pitfalls

These are the bugs that bite every team the first time they add caching.

### Cache Stampede (Thundering Herd)

**What it is:** Multiple requests all miss cache simultaneously, all hit the database, all try to populate the same cache key.

**Example:** 1000 users request GET /tasks at the same moment, cache just expired, 1000 queries hit PostgreSQL.

**How to prevent:**
```kotlin
@Cacheable(value = ["tasks"], key = "'all'", sync = true)
fun getTasks(): List<TaskDto> = taskService.findAll()
```

The `sync = true` attribute (Spring Cache 4.1+) uses a ReentrantLock per cache key so only one thread loads from DB. Others wait and read from cache.

**Alternative:** Use `spring.cache.redis.cache-null-values: false` + `unless = "#result == null"` to avoid caching null responses that trigger repeated misses.

---

### Stale Data (Missing Invalidation)

**What it is:** Cache entry lives longer than the data in the database. User sees old data for up to TTL duration.

**Example:** Update task #5 via PUT, but the list cache "tasks::all" still contains the old list for 10 minutes.

**How to prevent:** Always evict list caches on create/update/delete. The `CacheEvict` annotation on POST/PUT/DELETE handlers is mandatory for list caches.

**Rule of thumb:** Any endpoint that *writes* data must evict caches that include *lists* of that data. Single-item caches (keyed by ID) are lower risk because they're naturally invalidated when the item changes.

---

### Cache Poisoning

**What it is:** A bad value gets cached and served to every requester for the full TTL.

**Example:** A bug in serialization causes a task's description to be corrupted, gets cached, all users see garbage.

**How to prevent:**
- Keep TTLs reasonable (5-10 minutes for list caches, not hours)
- Use `spring.cache.redis.time-to-live: 5m` to set an upper bound
- Add cache versioning: key includes a version number, change version on schema changes
- Invalidate on errors: `unless = "#result == null or #cause != null"` (custom logic)

---

### Lettuce vs Jedis — Use Lettuce

Spring Boot 2+ uses **Lettuce** as the default Redis client (not Jedis). Lettuce is:
- Non-blocking (supports reactive/spring-webflux)
- Thread-safe
- Built on Netty (async I/O)

You don't need to configure anything — Lettuce is auto-configured. The pitfall is searching Stack Overflow answers from 2016 that use `JedisClient` — those are outdated. Use the `RedisConnectionFactory` interface ( Lettuce implementation) and the `RedisTemplate` / `StringRedisTemplate` that Spring Boot auto-configures.

---

## Deep Dive Extras

These are optional for if you want deeper understanding or have extra time.

### 1. Per-Method Cache Key Customization

The default key is based on method name + all arguments. For list endpoints with pagination, you need custom keys:

```kotlin
@GetMapping
@Cacheable(
    value = ["tasks"],
    key = "'page:' + #page + ':size:' + #size"
)
fun getTasks(
    @RequestParam(defaultValue = "0") page: Int,
    @RequestParam(defaultValue = "20") size: Int
): Page<TaskDto> = taskService.findAll(PageRequest.of(page, size))
```

### 2. Reactive Caching with Spring Cache + Kotlin Coroutines

If your service layer uses `suspend` functions (which it should for DB access):

```kotlin
@Service
class TaskService(
    private val taskRepository: TaskRepository
) {
    @Cacheable(value = ["tasks"], key = "'all'")
    suspend fun findAll(): List<TaskDto> {
        // This runs on the caching thread pool — make sure your cache
        // configuration supports proper async operations
        return taskRepository.findAll().map { it.toDto() }
    }
}
```

Spring Cache supports suspend functions. The cache operation blocks the coroutine thread until complete, which is fine for cache reads (usually <5ms vs DB 50ms+).

### 3. Spring Boot Cache Metrics via Actuator

Add Micrometer to expose cache metrics:

```yaml
management:
  metrics:
    enable:
      cache: true
  endpoints:
    web:
      exposure:
        include: health,metrics,caches
```

Then query: `GET /actuator/metrics/cache.gets?tag=cache:tasks` to see hit/miss ratios.

### 4. Cache Aside vs Read-Through vs Write-Through

| Pattern | Description | When to Use |
|---------|-------------|-------------|
| **Cache-Aside** (lazy) | App checks cache, misses, loads from DB, populates cache | Most common — use this |
| **Read-Through** | Cache automatically loads from DB on miss | Good when all callers go through cache |
| **Write-Through** | Write to cache + DB simultaneously | Good for read-heavy where data must be consistent |
| **Write-Behind** | Write to cache, async write to DB | High write throughput, risk of data loss |

You're implementing **cache-aside** this week.

### 5. EhCache vs Redis vs Caffeine

| Cache | Best For | Not Good For |
|-------|----------|--------------|
| **Caffeine** (in-memory) | Single-instance, low-latency | Multi-instance, distributed |
| **Redis** (distributed) | Multi-instance, persistent | Single-instance, extra latency |
| **EhCache** | Simple JVM caching | Distributed scenarios |

For a portfolio project deployed to Railway with a single instance: **Caffeine** is simpler. For distributed production: **Redis**. This week we use Redis because it's more impressive for a portfolio and teaches skills transferable to distributed systems.

---

## Week 7 Summary

| Day | Topic | Hands-On |
|-----|-------|----------|
| Tue | Redis + Cache-Aside | Docker Redis, spring.cache.redis.* config, @EnableCaching |
| Wed | @Cacheable/@CacheEvict/@CachePut | Annotate TaskController GET/POST/PUT/DELETE |
| Thu | OpenAPI + Swagger UI | springdoc-openapi, /swagger-ui.html, @Operation annotations |
| Fri | Cache invalidation testing | Full curl cycle with log verification |
| Sat | Generate client SDK | openapi-generator TypeScript client from spec |
| Sun | README polish + push | Document API, verify GitHub, final audit |

**Portfolio deliverable this week:** Swagger UI accessible at `/swagger-ui.html`, GET /tasks cached with 10-minute TTL, POST/PUT/DELETE evict list cache, pushed to GitHub.

---

## References

- [Spring Boot 4.0.6 — Caching Documentation](https://docs.spring.io/spring-boot/4.0.6/reference/io/caching.html)
- [Spring Data Redis — Reference](https://docs.spring.io/spring-data/redis/reference/)
- [Spring Framework — Cache Abstraction](https://docs.spring.io/spring-framework/reference/integration/cache.html)
- [springdoc-openapi — GitHub](https://github.com/springdoc/springdoc-openapi)
- [Spring Guides — Caching](https://spring.io/guides/gs/caching/)
- [OpenAPI Generator — CLI](https://github.com/OpenAPITools/openapi-generator)
