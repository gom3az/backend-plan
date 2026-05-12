package com.gomaa.tasks.repository

import com.gomaa.tasks.SpringBootTestWithPostgres
import io.restassured.RestAssured
import org.testcontainers.containers.PostgreSQLContainer
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SpringBootTestWithPostgres
class TaskContainerTest @Autowired constructor(
    val taskRepository: TaskRepository
) {

    @LocalServerPort
    private var port: Int = 0


    @Test
    fun shouldGetEmptyTaskList() {
        RestAssured.given().baseUri("http://localhost:" + port)
    }
}
