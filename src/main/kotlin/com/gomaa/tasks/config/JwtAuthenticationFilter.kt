package com.gomaa.tasks.config

import com.gomaa.tasks.services.TokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val tokenService: TokenService
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
            val jwt = tokenService.jwtDecoder.decode(token)
            val scope = jwt.getClaimAsString("scope") ?: ""
            val authorities = if (scope.isNotEmpty()) {
                scope.split(" ").map { SimpleGrantedAuthority(it) }
            } else {
                emptyList()
            }

            val authentication = UsernamePasswordAuthenticationToken(
                jwt.subject, null, authorities
            )
            SecurityContextHolder.getContext().authentication = authentication
        } catch (ex: Exception) {
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }

}