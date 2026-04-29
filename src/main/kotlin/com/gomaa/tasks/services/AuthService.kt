package com.gomaa.tasks.services

import com.gomaa.tasks.controller.AuthController
import com.gomaa.tasks.dto.LoginRequest
import com.gomaa.tasks.dto.RegisterRequest
import com.gomaa.tasks.exceptions.UserExistException
import com.gomaa.tasks.model.User
import com.gomaa.tasks.repository.UserRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    val tokenService: TokenService,
    val authenticationManager: AuthenticationManager,
    val userRepository: UserRepository,
    val passwordEncoder: PasswordEncoder,
    val logger: Logger = LoggerFactory.getLogger(AuthController::class.java)
) {

    fun login(userLogin: LoginRequest): String {
        logger.info("Login attempt for user: ${userLogin.username}")
        try {
            val auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    userLogin.username, userLogin.password
                )
            )
            logger.info("Authentication successful for user: ${userLogin.username}")
            return tokenService.generateToken(auth)
        } catch (ex: Exception) {
            logger.error("Authentication failed for user: ${userLogin.username}", ex)
            throw ex
        }
    }

    fun register(request: RegisterRequest) {
        logger.info("Register with ${request.username}")
        if (userRepository.existsByUsername(request.username)) {
            throw UserExistException()
        }

        val user = User(
            username = request.username,
            password = passwordEncoder.encode(request.password)!!,
            roles = listOf("ROLE_USER")
        )

        userRepository.save(user)
    }

}
