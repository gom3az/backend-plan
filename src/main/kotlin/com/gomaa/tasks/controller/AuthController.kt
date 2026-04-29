package com.gomaa.tasks.controller

import com.gomaa.tasks.dto.LoginRequest
import com.gomaa.tasks.dto.RegisterRequest
import com.gomaa.tasks.services.AuthService
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthController(
    val authService: AuthService,
    val logger: Logger = LoggerFactory.getLogger(AuthController::class.java)
) {

    @PostMapping("/token")
    @Throws(AuthenticationException::class)
    fun token(@RequestBody @Valid userLogin: LoginRequest): ResponseEntity<String> {
        return ResponseEntity.ok(authService.login(userLogin))
    }

    @PostMapping("/register")
    fun register(@RequestBody @Valid request: @Valid RegisterRequest): ResponseEntity<Unit> {
        return ResponseEntity.ok(authService.register(request))
    }
}