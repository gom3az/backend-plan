package com.gomaa.tasks.repository

import com.gomaa.tasks.SpringBootTestWithPostgres
import com.gomaa.tasks.model.Role
import com.gomaa.tasks.model.Task
import com.gomaa.tasks.model.User
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import java.time.LocalDateTime
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringBootTestWithPostgres
class TaskContainerTest @Autowired constructor(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
) {

    @field:LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun setup() {
        taskRepository.deleteAll()
        userRepository.deleteAll()
        RestAssured.port = port
        RestAssured.baseURI = "http://localhost"
    }


    @Test
    fun shouldGetTaskList() {
        val createdAt = LocalDateTime.of(2020, 5, 14, 6, 30)

        val registerResponse = RestAssured
            .given()
            .contentType(ContentType.JSON)
            .body(
                """
            {
              "username":"testuser",
              "password":"password"
            }
            """.trimIndent()
            )
            .post("/register")

        registerResponse.then().statusCode(200)

        val token = registerResponse.asString().replace("\"", "").trim()

        val user = userRepository.findByUsername("testuser") ?: error("User not found")

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
            .contentType(ContentType.JSON)
            .auth()
            .oauth2(token)
            .accept(ContentType.JSON)
            .`when`()
            .get("/tasks")
            .then()
            .statusCode(200)
            .body("tasks.size()", org.hamcrest.Matchers.equalTo(1))
            .body("tasks[0].title", org.hamcrest.Matchers.equalTo("test"))
    }
}
