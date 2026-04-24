# Phase 1 — Week 6: Testing with Testcontainers + Integration Tests

## Overview

**Goal**: Write tests that spin up a real PostgreSQL container. Build real confidence in your code.

**Portfolio Deliverable**: Full integration test suite. GitHub Actions runs tests on every push.

**Time Commitment**: 15-25 hours across Tuesday, Thursday, Friday-Sunday.

**Why Testcontainers?**

| Aspect | Without Testcontainers | With Testcontainers |
|--------|----------------------|---------------------|
| Database | H2 in-memory (fake) | Real PostgreSQL in Docker |
| Dev/CI/Prod Parity | Low — different engines | High — identical everywhere |
| SQL Compatibility Issues | Caught in production | Caught in tests |
| Confidence in Refactoring | Low | High |

---

## Tuesday — Introduction to Testcontainers

### Study Resources

| Resource | What It Covers |
|----------|----------------|
| [Testcontainers Getting Started](https://testcontainers.com/getting-started/) | Core concepts, Docker prerequisite, PostgreSQL module example, GenericContainer approach |
| [PostgreSQL Module](https://testcontainers.com/modules/postgresql/) | PostgreSQLContainer usage across Java, Go, C#, Node.js, Python, Rust |
| [Replace H2 with Real Database](https://testcontainers.com/guides/replace-h2-with-real-database-for-testing/) | Why H2 causes problems, two configuration approaches, JdbcTemplate testing |

### What to Read

1. Read the [Testcontainers Getting Started](https://testcontainers.com/getting-started/) page completely — understand the core workflow (start containers before tests, run against real services, Ryuk cleans up after).
2. Skim the [PostgreSQL Module](https://testcontainers.com/modules/postgresql/) page — see how the same `PostgreSQLContainer` works across multiple languages. This reinforces the concept that Testcontainers is polyglot.
3. Read the [Replace H2 with Real Database](https://testcontainers.com/guides/replace-h2-with-real-database-for-testing/) guide — understand the two approaches (JDBC URL vs JUnit5 Extension) and why you'd choose one over the other.

### Practical: Your First Testcontainer

Add the dependencies to your `build.gradle` (or `pom.xml`):

```groovy
// build.gradle.kts (Kotlin DSL)
testImplementation("org.testcontainers:junit-jupiter:1.19.7")
testImplementation("org.testcontainers:postgresql:1.19.7")
testImplementation("org.postgresql:postgresql:42.7.2")
testImplementation("io.rest-assured:rest-assured:5.4.0")
```

Create your first integration test:

```java
package com.calendar.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class EventsControllerTest {

    @LocalServerPort
    private Integer port;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Test
    void shouldGetEmptyListOfEvents() {
        given()
            .baseUri("http://localhost:" + port)
        .when()
            .get("/api/events")
        .then()
            .statusCode(200)
            .body("content", hasSize(0))
            .body("page", equalTo(0))
            .body("size", equalTo(20));
    }
}
```

**What is happening:**
- `@Testcontainers` — JUnit5 extension that starts/stops containers annotated with `@Container`
- `@Container static PostgreSQLContainer<?>` — field is started before all tests, stopped after all tests
- `@DynamicPropertySource` — injects container connection info into Spring's `Environment` before context loads
- `@SpringBootTest(webEnvironment = RANDOM_PORT)` — starts full application with random port to avoid conflicts

### Audit Checkpoint

Answer these before moving to Thursday:

1. What does Ryuk do in Testcontainers?
2. What is the difference between `@Container static` and `@Container instance` fields?
3. Why does `@DynamicPropertySource` need to be `static`?
4. What happens if you use `RANDOM_PORT` and don't inject `@LocalServerPort`?
5. What is the JDBC URL format for Testcontainers PostgreSQL?

---

## Thursday — Testcontainers Lifecycle Patterns + Auth Testing

### Study Resources

| Resource | What It Covers |
|----------|----------------|
| [Testcontainers Container Lifecycle](https://testcontainers.com/guides/testcontainers-container-lifecycle/) | Manual JUnit5 callbacks, @Testcontainers extension, Singleton pattern, caveats |
| [Testing Spring Boot REST API](https://testcontainers.com/guides/testing-spring-boot-rest-api-using-testcontainers/) | Full test setup with RestAssured, @BeforeAll/@AfterAll, @DynamicPropertySource |
| [Spring Boot Test Application Setup (Baeldung)](https://www.baeldung.com/spring-boot-test-application-setup) | @SpringBootTest vs @WebMvcTest, @MockBean, test properties |

### What to Read

1. Read the [Container Lifecycle guide](https://testcontainers.com/guides/testcontainers-container-lifecycle/) carefully — the Singleton pattern caveat is critical for CI performance.
2. Review the [REST API testing guide](https://testcontainers.com/guides/testing-spring-boot-rest-api-using-testcontainers/) — understand how `@BeforeEach` cleans data between tests.
3. Skim the Baeldung Spring Boot test setup article for `@SpringBootTest` vs slicing options.

### Practical: Lifecycle Patterns

**Pattern 1: @Testcontainers Extension (Most Common)**

```java
@Testcontainers
class LifecycleExtensionTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    // Extension handles start/stop automatically
}
```

**Pattern 2: Manual @BeforeAll/@AfterAll (More Control)**

```java
class ManualLifecycleTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @BeforeAll
    static void startContainers() {
        postgres.start();
    }

    @AfterAll
    static void stopContainers() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

**Pattern 3: Singleton Containers (Shared Across Test Classes)**

```java
class BaseIntegrationTest {
    static PostgreSQLContainer<?> postgres;

    static {
        postgres = new PostgreSQLContainer<>("postgres:16-alpine");
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}

// Subclass inherits the same container — faster for large suites
class EventsRepositoryTest extends BaseIntegrationTest { ... }
class UsersRepositoryTest extends BaseIntegrationTest { ... }
```

**CRITICAL CAVEAT** (from the lifecycle guide):
> Using `@Testcontainers` + `@Container` together with the Singleton pattern stops the containers after each test class, causing later tests to fail.

**Solution**: Use static initializer without `@Testcontainers` annotation when sharing containers:

```java
class SharedContainerBase {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        // Start once, never stop via extension
        postgres.start();
    }
}
```

### Practical: Testing Authentication with JWT

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRejectRequestWithoutToken() {
        given()
            .baseUri("http://localhost:" + port)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/events")
        .then()
            .statusCode(401);
    }

    @Test
    void shouldReturnEventsForAuthenticatedUser() {
        // Create user and get token
        User user = userRepository.save(User.builder()
            .email("test@example.com")
            .password(passwordEncoder.encode("password123"))
            .build());

        String token = tokenProvider.generateToken(user);

        given()
            .baseUri("http://localhost:" + port)
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
        .when()
            .get("/api/events")
        .then()
            .statusCode(200);
    }

    @Test
    void shouldReturn403WhenNotOwner() {
        User owner = userRepository.save(User.builder()
            .email("owner@example.com")
            .password(passwordEncoder.encode("password123"))
            .build());

        User other = userRepository.save(User.builder()
            .email("other@example.com")
            .password(passwordEncoder.encode("password123"))
            .build());

        String ownerToken = tokenProvider.generateToken(owner);

        // Create event owned by 'owner'
        Event event = eventRepository.save(Event.builder()
            .title("Owner Event")
            .user(owner)
            .build());

        // Other user tries to delete it
        given()
            .baseUri("http://localhost:" + port)
            .header("Authorization", "Bearer " + tokenProvider.generateToken(other))
            .contentType(ContentType.JSON)
        .when()
            .delete("/api/events/" + event.getId())
        .then()
            .statusCode(403);
    }
}
```

### Practical: Testing Pagination and Filtering

```java
@Test
void shouldReturnPaginatedEvents() {
    // Create 25 events
    for (int i = 0; i < 25; i++) {
        eventRepository.save(Event.builder()
            .title("Event " + i)
            .user(currentUser())
            .build());
    }

    given()
        .baseUri("http://localhost:" + port)
        .header("Authorization", "Bearer " + authToken())
        .queryParam("page", 0)
        .queryParam("size", 10)
    .when()
        .get("/api/events")
    .then()
        .statusCode(200)
        .body("content", hasSize(10))
        .body("totalElements", equalTo(25))
        .body("totalPages", equalTo(3))
        .body("first", equalTo(true))
        .body("last", equalTo(false));
}

@Test
void shouldFilterEventsByDateRange() {
    Event yesterday = eventRepository.save(createEvent(LocalDateTime.now().minusDays(1)));
    Event today = eventRepository.save(createEvent(LocalDateTime.now()));
    Event tomorrow = eventRepository.save(createEvent(LocalDateTime.now().plusDays(1)));

    String startDate = LocalDate.now().toString();
    String endDate = LocalDate.now().plusDays(1).toString();

    given()
        .baseUri("http://localhost:" + port)
        .header("Authorization", "Bearer " + authToken())
        .queryParam("startDate", startDate)
        .queryParam("endDate", endDate)
    .when()
        .get("/api/events")
    .then()
        .statusCode(200)
        .body("content", hasSize(2)); // today and tomorrow (if range includes both)
}

@Test
void shouldReturn400ForInvalidDateRange() {
    given()
        .baseUri("http://localhost:" + port)
        .header("Authorization", "Bearer " + authToken())
        .queryParam("startDate", "2025-01-01")
        .queryParam("endDate", "2024-01-01")  // end before start
    .when()
        .get("/api/events")
    .then()
        .statusCode(400)
        .body("error", equalTo("INVALID_DATE_RANGE"));
}
```

### Practical: Error Path Testing

```java
@Test
void shouldReturn404WhenEventNotFound() {
    given()
        .baseUri("http://localhost:" + port)
        .header("Authorization", "Bearer " + authToken())
    .when()
        .get("/api/events/99999")
    .then()
        .statusCode(404)
        .body("error", equalTo("EVENT_NOT_FOUND"));
}

@Test
void shouldReturn400ForInvalidEventData() {
    given()
        .baseUri("http://localhost:" + port)
        .header("Authorization", "Bearer " + authToken())
        .contentType(ContentType.JSON)
        .body("{\"title\": \"\"}")  // empty title
    .when()
        .post("/api/events")
    .then()
        .statusCode(400)
        .body("error", equalTo("VALIDATION_ERROR"))
        .body("fieldErrors.title", hasItem("Title is required"));
}

@Test
void shouldReturn422ForInvalidEventTiming() {
    given()
        .baseUri("http://localhost:" + port)
        .header("Authorization", "Bearer " + authToken())
        .contentType(ContentType.JSON)
        .body("""
            {
                "title": "Invalid Event",
                "startTime": "2025-01-01T10:00",
                "endTime": "2025-01-01T09:00"
            }
            """)  // end before start
    .when()
        .post("/api/events")
    .then()
        .statusCode(422)
        .body("error", equalTo("INVALID_EVENT_TIMING"));
}
```

### Audit Checkpoint

Before Friday:

1. What is the critical caveat when combining `@Testcontainers` + `@Container` with Singleton pattern?
2. How do you inject a JWT token into a RestAssured request?
3. What is the difference between `statusCode(404)` and `body("error", equalTo("NOT_FOUND"))`?
4. How do you test that a user CANNOT access another user's resource?
5. What does `@LocalServerPort` give you that hardcoded `8080` does not?

---

## Friday — Data Cleanup + CI Setup

### Study Resources

| Resource | What It Covers |
|----------|----------------|
| [Testcontainers Getting Started](https://testcontainers.com/getting-started/) | Revisit Docker prerequisite, Ryuk cleanup mechanism |
| [Testing Spring Boot REST API](https://testcontainers.com/guides/testing-spring-boot-rest-api-using-testcontainers/) | @BeforeEach cleanup pattern |
| [GitHub Actions Documentation](https://docs.github.com/en/actions) | Docker service containers, matrix builds |

### What to Read

1. Revisit the [Testcontainers Getting Started](https://testcontainers.com/getting-started/) — understand the Ryuk sidecar cleanup mechanism and why containers are destroyed after JVM exits.
2. Review your existing GitHub Actions workflow (if any) and identify where Testcontainers tests would run.

### Practical: Cleaning Data Between Tests

**Option 1: @BeforeEach deleteAll() — Recommended for most cases**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class DataCleanupTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Clean slate for each test
        eventRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateEvent() {
        userRepository.save(User.builder()
            .email("test@example.com")
            .build());

        given()
            .contentType(ContentType.JSON)
            .body("{\"title\": \"My Event\", \"userId\": 1}")
        .when()
            .post("/api/events")
        .then()
            .statusCode(201);

        assertEquals(1, eventRepository.count());
    }

    @Test
    void shouldHandleSecondTestIndependently() {
        // This test also starts with clean data
        assertEquals(0, eventRepository.count());
    }
}
```

**Option 2: @Transactional on test class — Auto-rollback after each test**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Transactional
class TransactionalCleanupTest {
    // Every test runs in a transaction that rolls back after the test
    // WARNING: Does not work well with @DirtiesContext and some Spring features
}
```

**Option 3: @Sql for resetting schema — Good for known state**

```java
@Sql(scripts = "/sql/test-data.sql", executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = AFTER_TEST_METHOD)
@Test
void shouldTestWithKnownState() { ... }
```

**Comparison:**

| Approach | Pros | Cons |
|----------|------|------|
| `deleteAll()` in `@BeforeEach` | Simple, explicit, works with any repository | Must inject all repositories |
| `@Transactional` | No manual cleanup needed | Can mask transaction-related bugs, doesn't work with all Spring features |
| `@Sql` scripts | Precise control, good for complex data | SQL maintenance, slower |

### Practical: GitHub Actions CI Setup

Create `.github/workflows/test.yml`:

```yaml
name: Integration Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      docker:
        # Required for Testcontainers to work
        image: docker:20.10.16
        options: --privileged
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Run integration tests with Testcontainers
        run: ./gradlew integrationTest --info
        env:
          # Tell Testcontainers to use Docker on the GitHub Actions runner
          DOCKER_HOST: unix:///var/run/docker.sock

      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: build/reports/tests/integrationTest/
          retention-days: 14
```

Or for Maven:

```yaml
name: Integration Tests (Maven)

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      docker:
        image: docker:20.10.16
        options: --privileged

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'maven'

      - name: Run integration tests
        run: ./mvnw verify -Dspring.profiles.active=test

      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: target/surefire-reports/
          retention-days: 7
```

### Audit Checkpoint

Before the weekend:

1. What are the three approaches to cleaning data between tests? Which is most common for integration tests?
2. Why is `@Transactional` not always the best choice for data cleanup?
3. What Docker image is required as a service container in GitHub Actions for Testcontainers?
4. What does `--privileged` flag do in the Docker service container?
5. What environment variable tells Testcontainers where to find Docker?

---

## Saturday — Reusable Containers + Performance Optimization

### Study Resources

| Resource | What It Covers |
|----------|----------------|
| [Reusable Containers](https://testcontainers.com/features/reusable_containers/) | `reuse=true` flag, singleton pattern, benefits and limitations |
| [Testcontainers Container Lifecycle](https://testcontainers.com/guides/testcontainers-container-lifecycle/) | Singleton pattern trade-offs |

### What to Read

1. Read the [Reusable Containers](https://testcontainers.com/features/reusable_containers/) guide — understand the `reuse=true` configuration and when it is/isn't appropriate.
2. Review the performance implications of container startup in CI.

### Reusable Containers

By default, Testcontainers destroys containers after the JVM exits. For faster local development, you can enable reuse:

**Option 1: System property (recommended for local dev)**

```bash
# Run tests with reuse enabled
./gradlew test -Dtestcontainers.reuse.enable=true
```

**Option 2: Create a `~/.testcontainers.properties` file**

```properties
# ~/.testcontainers.properties
testcontainers.reuse.enable=true
```

**What this means:**
- Containers are not stopped after tests complete
- On subsequent test runs, Testcontainers reconnects to the existing container instead of starting a new one
- Reduces container startup time from 15-30 seconds to ~1 second

**IMPORTANT CAVEATS:**
- Reuse is global — containers persist until explicitly stopped
- If you modify schema, the old schema persists in the reused container
- Not recommended for CI (where containers should be fresh each run)
- Ryuk still runs but respects the reuse label

### Practical: Performance Optimization

**1. Use `@Testcontainers(reuseable = true)` for local development only:**

```java
@Testcontainers(reuseable = true)  // Local dev only, NOT for CI
class LocalDevTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
}
```

**2. Use lightweight images:**

```java
// Instead of postgres:16
new PostgreSQLContainer("postgres:16-alpine")  // Alpine is smaller and faster
```

**3. Use static container fields (not instance-level):**

```java
// GOOD: Container shared across all test methods
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

// BAD: New container per test method (extremely slow)
@Container
PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
```

**4. Only spin up containers for integration tests, not unit tests:**

```groovy
// build.gradle.kts
sourceSets {
    create("integrationTest") {
        kotlin.srcDir("src/integrationTest/kotlin")
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

tasks.register("integrationTest", Test::class) {
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter("test")
    extensions.configure(TestcontainersExtension::class) {
        reuseContainers = true  // Only for integration tests
    }
}
```

**5. Parallelize tests with containers (advanced):**

```yaml
# .github/workflows/test.yml
jobs:
  test:
    strategy:
      fail-fast: false
      matrix:
        shard: [1, 2, 3, 4]
    runs-on: ubuntu-latest
    steps:
      - name: Run test shard ${{ matrix.shard }}
        run: ./gradlew test --tests "com.calendar.**" -Dshard=${{ matrix.shard }}
```

### Audit Checkpoint

Before Sunday:

1. What does `testcontainers.reuse.enable=true` do?
2. Why should you NOT use reusable containers in CI?
3. What is the difference between `postgres:16` and `postgres:16-alpine`?
4. Why should container fields be `static`, not instance-level?
5. How do you separate integration tests from unit tests in Gradle?

---

## Sunday — Full Integration Test Suite + Error Handling

### What to Do Today

Build a complete integration test suite for your Calendar API. Write tests for:

1. **CRUD operations** — create, read, update, delete events
2. **Authentication** — login, token refresh, expired tokens
3. **Authorization** — access control, forbidden resources
4. **Validation** — invalid input, missing fields, constraint violations
5. **Pagination** — offset/limit, sort, page metadata
6. **Error handling** — 400, 401, 403, 404, 500 responses

### Practical: Complete Integration Test Suite

```java
package com.calendar.integration

import io.restassured.http.ContentType
import io.restassured.RestAssured
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

import java.time.LocalDateTime

import static io.restassured.RestAssured.given
import static org.hamcrest.Matchers.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class CalendarApiIntegrationTest {

    @LocalServerPort
    private Integer port

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl)
        registry.add("spring.datasource.username", postgres::getUsername)
        registry.add("spring.datasource.password", postgres::getPassword)
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver")
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop")
    }

    @Autowired
    PasswordEncoder passwordEncoder

    @Autowired
    EventRepository eventRepository

    @Autowired
    UserRepository userRepository

    private static String authToken
    private static Long testUserId
    private static Long testEventId

    @BeforeAll
    static void setupClass(@Autowired UserRepository userRepository,
                           @Autowired EventRepository eventRepository,
                           @Autowired PasswordEncoder passwordEncoder) {
        // Create test user once for the entire class
        User user = userRepository.save(User.builder()
            .email("test@example.com")
            .password(passwordEncoder.encode("password123"))
            .name("Test User")
            .build())
        testUserId = user.getId()

        authToken = generateToken(user) // Your JWT generation logic
    }

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll()
        // Optionally reset sequences
    }

    // ===== CREATE EVENT TESTS =====

    @Test
    @Order(1)
    void shouldCreateEvent() {
        def requestBody = """
            {
                "title": "Team Meeting",
                "description": "Weekly sync",
                "startTime": "${LocalDateTime.now().plusDays(1)}",
                "endTime": "${LocalDateTime.now().plusDays(1).plusHours(1)}",
                "location": "Conference Room A"
            }
            """

        def response = given()
            .baseUri("http://localhost:" + port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/events")
        .then()
            .statusCode(201)
            .body("title", equalTo("Team Meeting"))
            .body("description", equalTo("Weekly sync"))
            .body("location", equalTo("Conference Room A"))
            .body("id", notNullValue())

        testEventId = response.jsonPath().getLong("id")
    }

    @Test
    @Order(2)
    void shouldRejectCreateEventWithMissingTitle() {
        def requestBody = """
            {
                "description": "Missing title",
                "startTime": "${LocalDateTime.now().plusDays(1)}",
                "endTime": "${LocalDateTime.now().plusDays(1).plusHours(1)}"
            }
            """

        given()
            .baseUri("http://localhost:" + port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/events")
        .then()
            .statusCode(400)
            .body("error", equalTo("VALIDATION_ERROR"))
            .body("fieldErrors.title", hasItem("Title is required"))
    }

    @Test
    @Order(3)
    void shouldRejectCreateEventWithEndBeforeStart() {
        def requestBody = """
            {
                "title": "Invalid Timing",
                "startTime": "${LocalDateTime.now().plusDays(1).plusHours(2)}",
                "endTime": "${LocalDateTime.now().plusDays(1)}"
            }
            """

        given()
            .baseUri("http://localhost:" + port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post("/api/events")
        .then()
            .statusCode(422)
            .body("error", equalTo("INVALID_EVENT_TIMING"))
    }

    // ===== GET EVENT TESTS =====

    @Test
    @Order(4)
    void shouldGetEventById() {
        given()
            .baseUri("http://localhost:" + port)
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/api/events/" + testEventId)
        .then()
            .statusCode(200)
            .body("id", equalTo(testEventId.intValue()))
            .body("title", equalTo("Team Meeting"))
    }

    @Test
    @Order(5)
    void shouldReturn404ForNonexistentEvent() {
        given()
            .baseUri("http://localhost:" + port)
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/api/events/99999")
        .then()
            .statusCode(404)
            .body("error", equalTo("EVENT_NOT_FOUND"))
    }

    // ===== UPDATE EVENT TESTS =====

    @Test
    @Order(6)
    void shouldUpdateEvent() {
        def updateBody = """
            {
                "title": "Updated Team Meeting",
                "description": "Updated description"
            }
            """

        given()
            .baseUri("http://localhost:" + port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(updateBody)
        .when()
            .put("/api/events/" + testEventId)
        .then()
            .statusCode(200)
            .body("title", equalTo("Updated Team Meeting"))
            .body("description", equalTo("Updated description"))
    }

    // ===== DELETE EVENT TESTS =====

    @Test
    @Order(7)
    void shouldDeleteEvent() {
        given()
            .baseUri("http://localhost:" + port)
            .header("Authorization", "Bearer " + authToken)
        .when()
            .delete("/api/events/" + testEventId)
        .then()
            .statusCode(204)

        // Verify deletion
        given()
            .baseUri("http://localhost:" + port)
            .header("Authorization", "Bearer " + authToken)
        .when()
            .get("/api/events/" + testEventId)
        .then()
            .statusCode(404)
    }

    // ===== PAGINATION TESTS =====

    @Test
    @Order(8)
    void shouldReturnPaginatedEvents() {
        // Create 15 events
        for (int i = 0; i < 15; i++) {
            createEventViaApi("Event " + i)
        }

        given()
            .baseUri("http://localhost:" + port)
            .header("Authorization", "Bearer " + authToken)
            .queryParam("page", 0)
            .queryParam("size", 5)
        .when()
            .get("/api/events")
        .then()
            .statusCode(200)
            .body("content", hasSize(5))
            .body("totalElements", equalTo(15))
            .body("totalPages", equalTo(3))
            .body("first", equalTo(true))
            .body("last", equalTo(false))
    }

    // ===== AUTHENTICATION TESTS =====

    @Test
    @Order(9)
    void shouldRejectRequestWithoutToken() {
        given()
            .baseUri("http://localhost:" + port)
        .when()
            .get("/api/events")
        .then()
            .statusCode(401)
    }

    @Test
    @Order(10)
    void shouldRejectExpiredToken() {
        given()
            .baseUri("http://localhost:" + port)
            .header("Authorization", "Bearer invalid.expired.token")
        .when()
            .get("/api/events")
        .then()
            .statusCode(401)
    }

    // ===== HELPER METHODS =====

    private void createEventViaApi(String title) {
        def body = """
            {
                "title": "$title",
                "startTime": "${LocalDateTime.now().plusDays(1)}",
                "endTime": "${LocalDateTime.now().plusDays(1).plusHours(1)}"
            }
            """

        given()
            .baseUri("http://localhost:" + port)
            .header("Authorization", "Bearer " + authToken)
            .contentType(ContentType.JSON)
            .body(body)
        .when()
            .post("/api/events")
        .then()
            .statusCode(201)
    }
}
```

### Common Pitfalls

| Pitfall | Cause | Solution |
|---------|-------|----------|
| Container not starting in CI | Docker not available or `--privileged` missing | Ensure Docker service is configured in GitHub Actions |
| Port conflict | Multiple tests trying to use same port | Always use `RANDOM_PORT` and `@LocalServerPort` |
| Slow tests in CI | Container startup + image pull | Use lightweight images (`postgres:16-alpine`), enable reuse for local dev |
| Tests pass locally but fail in CI | Timing issues, uncommitted data | Use `@BeforeEach` cleanup, avoid relying on static state |
| "Connection refused" errors | Container not ready when tests start | Testcontainers handles this automatically via wait strategy |
| Container reuse causing stale data | Old data persists between runs | Use `@BeforeEach` deleteAll, or `ddl-auto=create-drop` |
| OutOfMemoryError in CI | Too many containers | Use singleton pattern, reduce parallelization, ensure Docker memory limits |

### Performance Tips

1. **Enable container reuse for local development:**
   ```properties
   # ~/.testcontainers.properties
   testcontainers.reuse.enable=true
   ```

2. **Use Alpine-based images:**
   ```java
   new PostgreSQLContainer("postgres:16-alpine")  // Smaller, faster startup
   ```

3. **Run integration tests in parallel with test workers:**
   ```groovy
   tasks.integrationTest {
       maxParallelForks = 4
       forkEvery = 1  // One fork per test class
   }
   ```

4. **Separate unit tests from integration tests:**
   ```groovy
   tasks.register("integrationTest", Test::class) {
       shouldRunAfter("test")
   }
   ```

5. **Use `@DynamicPropertySource` instead of `@TestPropertySource` for containers:**
   - `@DynamicPropertySource` registers properties before context loads
   - Allows Spring to properly configure datasource with real container values

### Deep Dive Extras

1. **Testcontainers with other databases:** MySQL, MariaDB, Oracle, SQL Server
2. **Testcontainers for message queues:** Kafka, RabbitMQ containers for event-driven testing
3. **Testcontainers for Elasticsearch:** Integration testing search functionality
4. **Testcontainers for Redis:** Caching layer testing
5. **Custom generic containers:** Running any Docker image for testing
6. **Testcontainers on Kubernetes (Telepresence):** Remote testing with local containers
7. **Database migration testing:** Use Testcontainers to verify Flyway/Liquibase scripts
8. **Contract testing:** Consumer-driven contracts with real database state
9. **Load testing with Testcontainers:** Using containers to spin up realistic data volumes

### Week 6 Audit Checklist

Complete this checklist before moving to Week 7:

- [ ] Can explain why Testcontainers gives higher confidence than H2
- [ ] Can configure `@Testcontainers` and `@Container` correctly
- [ ] Can use `@DynamicPropertySource` to inject container properties
- [ ] Understands the difference between static and instance container fields
- [ ] Can implement JWT authentication testing with RestAssured
- [ ] Can test pagination, filtering, sorting endpoints
- [ ] Can test error paths: 400, 401, 403, 404, 422
- [ ] Can choose between deleteAll(), @Transactional, and @Sql for cleanup
- [ ] Can configure GitHub Actions with Docker service container
- [ ] Understands why `--privileged` is needed for Testcontainers in CI
- [ ] Knows how to enable container reuse for local dev
- [ ] Knows to NOT enable reuse in CI
- [ ] Can write a full integration test suite covering CRUD, auth, errors, pagination
- [ ] Integration tests are in a separate source set (`src/integrationTest`)
- [ ] GitHub Actions workflow runs integration tests on every push
- [ ] All tests pass locally
- [ ] All tests pass in CI (or you know why they don't and have a plan)

---

## Sources

- [Testcontainers Getting Started](https://testcontainers.com/getting-started/)
- [Testing Spring Boot REST API with Testcontainers](https://testcontainers.com/guides/testing-spring-boot-rest-api-using-testcontainers/)
- [Replace H2 with Real Database Using Testcontainers](https://testcontainers.com/guides/replace-h2-with-real-database-for-testing/)
- [Testcontainers Container Lifecycle Management](https://testcontainers.com/guides/testcontainers-container-lifecycle/)
- [Testcontainers PostgreSQL Module](https://testcontainers.com/modules/postgresql/)
- [Reusable Containers](https://testcontainers.com/features/reusable_containers/)
