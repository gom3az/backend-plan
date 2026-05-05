package com.gomaa.tasks.config

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http.csrf { it.disable() }.authorizeHttpRequests { auth ->
            auth.requestMatchers("/token").permitAll()
            auth.requestMatchers("/register").permitAll()
            auth.requestMatchers(HttpMethod.POST, "/tasks/**").hasAuthority("ROLE_USER")
            auth.requestMatchers(HttpMethod.PUT, "/tasks/**").hasAuthority("ROLE_USER")
            auth.requestMatchers(HttpMethod.DELETE, "/tasks/**").hasAuthority("ROLE_ADMIN")
            auth.anyRequest().authenticated()
        }.exceptionHandling { ex ->
            ex.authenticationEntryPoint { _, response, _ ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
            }
            ex.accessDeniedHandler { _, response, _ ->
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied")
            }
        }.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }.build()
    }
}