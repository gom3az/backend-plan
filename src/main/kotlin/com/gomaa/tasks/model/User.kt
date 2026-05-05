package com.gomaa.tasks.model

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,

    @Column(nullable = false, unique = true) val username: String,

    @Column(nullable = false) val password: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Enumerated(EnumType.STRING) // Stores "ROLE_USER" instead of 0, 1, 2
    @Column(name = "role", nullable = false)
    val roles: List<Role>
)

enum class Role {
    ROLE_USER, ROLE_ADMIN
}