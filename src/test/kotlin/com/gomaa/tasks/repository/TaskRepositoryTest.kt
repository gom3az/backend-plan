package com.gomaa.tasks.repository

import com.gomaa.tasks.SpringBootTestWithPostgres
import com.gomaa.tasks.model.Role
import com.gomaa.tasks.model.Task
import com.gomaa.tasks.model.User
import com.gomaa.tasks.services.TokenService
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.security.crypto.password.AbstractPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringBootTestWithPostgres
class TaskContainerTest(
    @Autowired private val taskRepository: TaskRepository,
    @LocalServerPort private val port: Int,
) {


    companion object {
        @JvmStatic
        private lateinit var userToken: String

        @JvmStatic
        private lateinit var adminToken: String

        private lateinit var user: User
        private lateinit var admin: User

        @BeforeAll
        @JvmStatic
        fun setupAll(
            @Autowired userRepository: UserRepository,
            @Autowired passwordEncoder: PasswordEncoder,
            @Autowired tokenService: TokenService
        ) {

            admin = User(
                username = "testadmin",
                password = passwordEncoder.encode("password123") ?: "", roles = listOf(Role.ROLE_USER, Role.ROLE_ADMIN)
            )

            user = User(
                username = "testuser",
                password = passwordEncoder.encode("password123") ?: "", roles = listOf(Role.ROLE_USER)
            )

            userRepository.save(admin)
            userRepository.save(user)

            adminToken = tokenService.generateToken("testadmin", listOf(Role.ROLE_USER, Role.ROLE_ADMIN))
            userToken = tokenService.generateToken("testuser", listOf(Role.ROLE_USER))
        }
    }

    @BeforeEach
    fun setup() {
        taskRepository.deleteAll()
        RestAssured.port = port
        RestAssured.baseURI = "http://localhost"
    }

    @Test
    fun shouldGetTaskList() {
        val createdAt = LocalDateTime.of(2020, 5, 14, 6, 30)

        taskRepository.save(
            Task(
                id = 0,
                user = user,
                title = "test",
                description = "test_description",
                completed = false,
                dueDate = null,
                createdAt = createdAt,
                updatedAt = createdAt
            )
        )

        RestAssured.given()
            .auth()
            .oauth2(userToken)
            .`when`()
            .get("/tasks")
            .then()
            .statusCode(200)
            .body("tasks.size()", org.hamcrest.Matchers.equalTo(1))
            .body("tasks[0].title", org.hamcrest.Matchers.equalTo("test"))
    }

    @Test
    fun shouldCreateTask() {

        val requestBody = """
		{
		"completed": false,
		"description": "Task description here",
		"dueDate": "2026-06-15T10:00:00",
		"title": "New Task"
		}
        """

        RestAssured.given().contentType(ContentType.JSON)
            .auth()
            .oauth2(adminToken)
            .body(requestBody)
            .`when`()
            .post("/tasks")
            .then()
            .statusCode(201)
    }

    @Test
    fun shouldRejectCreationMissingTitle() {

        val requestBody = """
		{
		"completed": false,
		"description": "Task description here",
		"dueDate": "2026-06-15T10:00:00"
		}
        """

        RestAssured.given().contentType(ContentType.JSON)
            .auth()
            .oauth2(adminToken)
            .body(requestBody)
            .`when`()
            .post("/tasks")
            .then()
            .statusCode(400)
    }

    @Test
    fun shouldGetById() {

        val createdAt = LocalDateTime.of(2020, 5, 14, 6, 30)

        val task = taskRepository.save(
            Task(
                user = admin,
                title = "test",
                description = "test_description",
                completed = false,
                dueDate = null,
                createdAt = createdAt,
                updatedAt = createdAt
            )
        )

        RestAssured.given().contentType(ContentType.JSON)
            .auth()
            .oauth2(adminToken)
            .`when`()
            .get("/tasks/${task.id}")
            .then()
            .statusCode(200)
    }


    @Test
    fun shouldReturn404NonExist() {

        RestAssured.given().contentType(ContentType.JSON)
            .auth()
            .oauth2(adminToken)
            .`when`()
            .get("/tasks/1")
            .then()
            .statusCode(404)
    }
}
