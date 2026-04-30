package com.gomaa.tasks.services

import com.gomaa.tasks.dto.LoginRequest
import com.gomaa.tasks.dto.RegisterRequest
import com.gomaa.tasks.model.Role
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class AuthService(
    val tokenService: TokenService,
    val authenticationManager: AuthenticationManager,
    val userService: UserService,
) {

    fun login(userLogin: LoginRequest): String {
        try {
            val auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                    userLogin.username, userLogin.password
                )
            )
            // auth.principal is the UserDetails loaded by UserDetailsService
            val userDetails = auth.principal as UserDetails
            val roles = userDetails.authorities.mapNotNull { Role.valueOf(it.authority!!) }
            return tokenService.generateToken(userDetails.username, roles)
        } catch (ex: Exception) {
            throw ex
        }
    }

    fun register(request: RegisterRequest): String {
        val user = userService.createUser(request)
        return tokenService.generateToken(user.username, user.roles)
    }
}
