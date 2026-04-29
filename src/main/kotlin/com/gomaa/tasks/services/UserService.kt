package com.gomaa.tasks.services

import com.gomaa.tasks.dto.RegisterRequest
import com.gomaa.tasks.exceptions.UserExistException
import com.gomaa.tasks.model.User
import com.gomaa.tasks.repository.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    val userRepository: UserRepository,
    val passwordEncoder: PasswordEncoder,
    val logger: Logger = LoggerFactory.getLogger(UserService::class.java)
) {

    fun createUser(request: RegisterRequest): User {
        logger.info("Creating user {}", request.username)

        if (userRepository.existsByUsername(request.username)) {
            throw UserExistException()
        }

        val userEntity = User(
            username = request.username,
            password = passwordEncoder.encode(request.password)!!,
            roles = listOf("ROLE_USER")
        )

        return userRepository.save(userEntity)
    }

}