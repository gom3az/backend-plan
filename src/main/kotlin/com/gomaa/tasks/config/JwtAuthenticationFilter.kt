package com.gomaa.tasks.config

import com.gomaa.tasks.services.TaskUserDetailsService
import com.gomaa.tasks.services.TokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val taskUserDetailsService: TaskUserDetailsService, private val tokenService: TokenService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substringAfter("Bearer ")

        try {
            if (!tokenService.validateToken(token)) {
                filterChain.doFilter(request, response)
                return
            }

            val username = tokenService.extractUsername(token)
            val roles = tokenService.extractRoles(token)

            if (!validUser(username, roles)) {
                filterChain.doFilter(request, response)
                return
            }

            val authorities = roles.map { SimpleGrantedAuthority(it) }

            val authentication = UsernamePasswordAuthenticationToken(
                username, null, authorities
            )

            SecurityContextHolder.getContext().authentication = authentication
        } catch (_: Exception) {
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }

    fun validUser(username: String, roles: List<String>): Boolean {
        return try {
            val userDetails = taskUserDetailsService.loadUserByUsername(username)

            return roles.all { tokenRole -> userDetails.authorities.any { it.authority == tokenRole } }
        } catch (_: UsernameNotFoundException) {
            false
        }
    }
}