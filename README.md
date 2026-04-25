# Task Management API

A production-quality Task Management REST API built with Spring Boot Kotlin — serving as the Phase 1 portfolio project of a 6-month backend transition plan from Senior Mobile Developer to Backend.

## Purpose

This project is the foundation of a structured learning path aimed at mastering backend engineering. Phase 1 focuses on REST API mastery with Spring Boot and PostgreSQL, building toward event-driven systems (Phase 2) and cloud-native full-stack deployment (Phase 3).

## Tech Stack

- **Language**: Kotlin
- **Framework**: Spring Boot 3.4+
- **Database**: PostgreSQL with Spring Data JPA
- **Migrations**: Flyway
- **Build**: Gradle with Kotlin DSL
- **Security**: Spring Security with JWT authentication (planned for Week 4)

## Current Phase

**Phase 1 — Months 1-2: REST API Mastery**

Currently in Week 2-3 of development, building:
- Task CRUD endpoints with PostgreSQL backing
- DTOs, validation, and global exception handling
- JWT authentication

## Features

- RESTful task management (CRUD operations)
- PostgreSQL-backed persistence with Flyway migrations
- Input validation with proper error responses
- JWT-secured endpoints
- Pagination, filtering, and sorting
- Redis caching for read endpoints
- OpenAPI/Swagger documentation
- Docker and Docker Compose support

## Project Structure

```
src/main/kotlin/com/gomaa/tasks/
├── model/          # JPA entities and domain models
├── repository/     # Spring Data JPA repositories
├── controller/     # REST controllers
├── service/        # Business logic layer
├── dto/            # Data transfer objects
├── config/         # Security and app configuration
└── exception/      # Custom exceptions and exception handlers
```

## Running Locally

```bash
./gradlew bootRun
```

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /tasks | List all tasks |
| GET | /tasks/{id} | Get a task by ID |
| POST | /tasks | Create a new task |
| PUT | /tasks/{id} | Update a task |
| DELETE | /tasks/{id} | Delete a task |
| POST | /auth/register | Register a new user |
| POST | /auth/login | Login and get JWT token |

## Learning Roadmap

This project follows a structured 24-week curriculum:

| Phase | Months | Theme |
|-------|--------|-------|
| 1 | 1-2 | REST API Mastery |
| 2 | 3-4 | Event-Driven Systems |
| 3 | 5-6 | Cloud-Native Full Stack |
