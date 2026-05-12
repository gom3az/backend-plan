package com.gomaa.tasks

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.TestPropertySourceUtils
import org.testcontainers.containers.PostgreSQLContainer


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ContextConfiguration(initializers = [PostgresContainerInitializer::class])
annotation class SpringBootTestWithPostgres

class PostgresContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        // Shared container instance across all tests
        private val postgresContainer = PostgreSQLContainer("postgres:16-alpine").apply {
            withDatabaseName("testdb")
            withUsername("testuser")
            withPassword("testpass")
            start()
        }
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        // Inject properties into the environment
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            applicationContext,
            "spring.datasource.url=${postgresContainer.jdbcUrl}",
            "spring.datasource.username=${postgresContainer.username}",
            "spring.datasource.password=${postgresContainer.password}",
            "spring.datasource.driver-class-name=org.postgresql.Driver"
        )
    }
}
