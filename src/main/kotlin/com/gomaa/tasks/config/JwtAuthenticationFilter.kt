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
    private val taskUserDetailsService: TaskUserDetailsService,
    private val tokenService: TokenService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
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

            val userDetails =
                try {
                    taskUserDetailsService.loadUserByUsername(username)
                } catch (_: UsernameNotFoundException) {
                    filterChain.doFilter(request, response)
                    return
                }

            if (roles.any { tokenRole -> userDetails.authorities.none { it.authority == tokenRole } }) {
                filterChain.doFilter(request, response)
                return
            }

            val authorities = roles.map { SimpleGrantedAuthority(it) }

            val authentication =
                UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    authorities,
                )

            SecurityContextHolder.getContext().authentication = authentication
        } catch (_: Exception) {
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }
}
