# Phase 1 — Week 4: Spring Security + JWT Auth

## Overview
**Goal**: Secure the Task API with JWT authentication. Implement user registration and login endpoints.

**Portfolio Deliverable**: POST /auth/register and POST /auth/login endpoints. All write endpoints (POST/PUT/DELETE /tasks) require a valid JWT.

**Time Commitment**: 15-25 hours across Tuesday, Thursday, Friday, Saturday, Sunday

---

## Tuesday — JWT Internals and Token Structure

### Study Resources

| Resource | What It Covers |
|---------|----------------|
| [JWT.io Introduction](https://jwt.io/introduction/) | JWT concept, when to use, structure explanation |
| [JWT.io Libraries](https://jwt.io/libraries/) | JJWT library for Java, algorithm support (HS256, RS256) |
| [Spring Security Reference: Authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/index.html) | SecurityFilterChain, hasAuthority, hasRole |
| [Spring Security Reference: Authentication Architecture](https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html) | SecurityContextHolder, Authentication, ProviderManager |

### What to Read

1. **JWT.io Introduction** - Read the entire page. Focus on:
   - JWT as RFC 7519 standard
   - The three parts: header.payload.signature
   - Base64URL encoding (not Base64)
   - HMAC (HS256) vs RSA (RS256) signing

2. **JWT.io Libraries** - Browse the Java section:
   - Note the JJWT library coordinates
   - Understand algorithm options

3. **Spring Security Authorization** - Read the authorization overview:
   - SecurityFilterChain configuration pattern
   - hasAuthority() vs hasRole() difference
   - Request matcher syntax

### Practical

**JWT Structure Breakdown**:

A JWT has three Base64URL-encoded parts separated by dots:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

**Header** (first part):
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**Payload** (second part):
```json
{
  "sub": "1234567890",
  "name": "John Doe",
  "iat": 1516239022
}
```

**Signature** (third part) - computed from: `HMACSHA256(base64url(header) + "." + base64url(payload), secret)`

**HS256 vs RS256**:
- **HS256** (HMAC with SHA-256): Symmetric, same key signs and verifies. Fast, simpler. Use for single-service tokens.
- **RS256** (RSA with SHA-256): Asymmetric, private key signs, public key verifies. Use when multiple services need to verify tokens.

**Spring Security Filter Chain Setup**:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/tasks/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/tasks/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/tasks/**").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }
}
```

**Key Insight**: STATELESS means no HTTP session created. Each request must present a valid JWT.

### Audit Checkpoint

- [ ] Can you explain why JWTs are Base64URL encoded, not Base64?
- [ ] Can you draw the three JWT parts and explain what each contains?
- [ ] What is the difference between HS256 and RS256? When would you use each?
- [ ] Why does SecurityFilterChain use `.stateless()` for JWT auth?
- [ ] What does `hasAuthority("ROLE_ADMIN")` check for vs `hasRole("ADMIN")`?

---

## Thursday — Spring Security Core Components

### Study Resources

| Resource | What It Covers |
|---------|----------------|
| [Spring Security Reference: Passwords](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/index.html) | PasswordEncoder, BCrypt, DaoAuthenticationProvider |
| [Spring Security Reference: UserDetailsService](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/user-details-service.html) | Custom UserDetailsService implementation |
| [JWT.io Introduction](https://jwt.io/introduction/) | Token validation vs verification |

### What to Read

1. **Spring Security Passwords** - Read the entire page:
   - PasswordEncoderFactories for creating encoders
   - BCrypt format (starts with `$2a$`, `$2b$`)
   - DelegatingPasswordEncoder for multiple formats

2. **UserDetailsService** - Read the custom implementation section:
   - UserDetailsService interface contract
   - loadUserByUsername() method
   - Building UserDetails objects

3. **JWT.io Introduction** - Re-read the "Validation vs Verification" section

### Practical

**Password Encoding Setup**:

```java
@Configuration
public class PasswordConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
```

**Why BCrypt?** Costs ~300ms to encode (intentionally slow). Prevents rainbow table attacks. Format: `$2a$10$saltsalt...`

**Custom UserDetailsService**:

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())
            .roles(user.getRoles().toArray(new String[0]))
            .build();
    }
}
```

**DaoAuthenticationProvider Flow**:

```
User submits credentials
        ↓
DaoAuthenticationProvider receives UsernamePasswordAuthenticationToken
        ↓
loadUserByUsername() retrieves UserDetails
        ↓
PasswordEncoder.verify() compares submitted vs stored password
        ↓
Success: Returns authenticated Authentication token
Failure: Throws BadCredentialsException
```

**Authentication Manager Setup**:

```java
@Bean
public AuthenticationManager authenticationManager(
        UserDetailsService userDetailsService,
        PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(provider);
}
```

### Audit Checkpoint

- [ ] Why does BCrypt include the algorithm version and salt in the stored password?
- [ ] What happens if you try to verify a password against a BCrypt hash without providing the salt?
- [ ] Can you trace the flow from login request to authenticated SecurityContext?
- [ ] What interface must your User entity NOT implement directly, and why?

---

## Friday — JWT Token Generation and Validation

### Study Resources

| Resource | What It Covers |
|---------|----------------|
| [Spring Security Reference: JWT Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html) | JWT decoder configuration, validation rules |
| [JWT.io Introduction](https://jwt.io/introduction/) | Decoding and verifying JWTs |
| [Spring Security Reference: Authentication Architecture](https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html) | AbstractAuthenticationProcessingFilter |

### What to Read

1. **JWT Resource Server** (First half through "Custom JwtDecoder Bean"):
   - Minimal dependencies for JWT support
   - issuer-uri configuration
   - Programmatic SecurityFilterChain configuration
   - NimbusJwtDecoder setup

2. **JWT.io Introduction** - Focus on the decoder section with example tokens

3. **Authentication Architecture** - Re-read the AbstractAuthenticationProcessingFilter flow

### Practical

**Dependencies for JWT** (in pom.xml):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-security-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
```

**JJWT for Token Generation** (for your /auth/login endpoint):

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
```

**JWT Token Generation Service**:

```java
@Service
public class JwtTokenService {
    
    private final SecretKey secretKey;
    private final long expirationMs = 86400000; // 24 hours
    
    public JwtTokenService(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    public String generateToken(String username, List<String> roles) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationMs);
        
        return Jwts.builder()
            .subject(username)
            .claim("roles", roles)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(secretKey)
            .compact();
    }
    
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            return !claims.getExpiration().isBefore(Instant.now());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    private Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
```

**Filter Chain for JWT Auth**:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/tasks/**").hasAuthority("ROLE_USER")
                .requestMatchers(HttpMethod.PUT, "/tasks/**").hasAuthority("ROLE_USER")
                .requestMatchers(HttpMethod.DELETE, "/tasks/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }
}
```

**Custom JWT OncePerRequestFilter**:

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = authHeader.substring(7);
        
        if (jwtTokenService.validateToken(token)) {
            String username = jwtTokenService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
            
            authentication.setDetails(
                new AuthenticationDetailsSource<HttpServletRequest, Object>()
                    .buildDetails(request)
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### Audit Checkpoint

- [ ] What happens if you pass an expired JWT to validateToken()?
- [ ] Why does JwtAuthenticationFilter extend OncePerRequestFilter?
- [ ] What is the purpose of setDetails() on the authentication token?
- [ ] Why is SecurityContextHolder.getContext().setAuthentication() necessary?
- [ ] What does the "Bearer " prefix in the Authorization header accomplish?

---

## Saturday — Auth Endpoints and Repository Setup

### Study Resources

| Resource | What It Covers |
|---------|----------------|
| [Spring Security Reference: UserDetailsService](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/user-details-service.html) | Integration with custom UserRepository |
| [JWT.io Introduction](https://jwt.io/introduction/) | Token workflow between parties |
| [Spring Security Reference: JWT Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html) | Authority extraction from JWT claims |

### What to Read

1. **UserDetailsService** - Review the custom implementation example
2. **JWT Resource Server** - Read "Authority Extraction" section

### Practical

**User Entity**:

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<String> roles = new ArrayList<>();
    
    // getters and setters
}
```

**User Repository**:

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
    boolean existsByUsername(String username);
}
```

**Auth Service (Registration + Login)**:

```java
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.getRoles().add("ROLE_USER");
        userRepository.save(user);
        
        String token = jwtTokenService.generateToken(user.getUsername(), user.getRoles());
        return new AuthResponse(token);
    }
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        
        String token = jwtTokenService.generateToken(user.getUsername(), user.getRoles());
        return new AuthResponse(token);
    }
}
```

**Auth Controller**:

```java
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

**DTOs**:

```java
public record RegisterRequest(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    String username,
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password
) {}

public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password
) {}

public record AuthResponse(String token) {}
```

**application.properties**:

```properties
# Generate with: openssl rand -base64 64
# Must be at least 256 bits (32 bytes) for HS256
jwt.secret=your-256-bit-secret-key-here-must-be-at-least-32-bytes-long-for-hs256
```

**@AuthenticationPrincipal in Controller**:

```java
@RestController
@RequestMapping("/tasks")
public class TaskController {
    
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @RequestBody @Valid CreateTaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        // Use username to associate task with user
        return ResponseEntity.ok(taskService.create(request, username));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String username = userDetails.getUsername();
        taskService.delete(id, username);
        return ResponseEntity.noContent().build();
    }
}
```

### Audit Checkpoint

- [ ] How does @AuthenticationPrincipal get populated with the UserDetails?
- [ ] Why is the password encoder called on registration but on login we use matches()?
- [ ] What is the purpose of BCrypt's random salt?
- [ ] Why do we use ROLE_USER as the default role and where does the ROLE_ prefix come from?
- [ ] What would happen if you saved the raw password instead of encoding it?

---

## Sunday — Refresh Token Pattern and Role-Based Access

### Study Resources

| Resource | What It Covers |
|---------|----------------|
| [Spring Security Reference: Authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/index.html) | hasAuthority, hasRole, role-based access |
| [JWT.io Introduction](https://jwt.io/introduction/) | Refresh token use cases |
| [Spring Security Reference: JWT Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html) | Custom authority prefixes |

### What to Read

1. **Authorization** - Read the hasAuthority vs hasRole sections
2. **JWT.io Introduction** - Review when to use JWTs section on refresh tokens

### Practical

**Refresh Token Pattern (Simple Version)**:

```java
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private Instant expiryDate;
    
    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }
}

// In AuthService
public AuthResponse refreshToken(String refreshToken) {
    RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
        .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
    
    if (token.isExpired()) {
        refreshTokenRepository.delete(token);
        throw new IllegalArgumentException("Refresh token expired");
    }
    
    User user = userRepository.findByUsername(token.getUsername())
        .orElseThrow(() -> new IllegalArgumentException("User not found"));
    
    String newAccessToken = jwtTokenService.generateToken(user.getUsername(), user.getRoles());
    return new AuthResponse(newAccessToken);
}
```

**Role-Based Access Control Examples**:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Public endpoints
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                
                // Admin-only endpoints
                .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                
                // User endpoints (authenticated)
                .requestMatchers("/tasks/**").hasAuthority("ROLE_USER")
                
                // All other requests need authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }
}
```

**Method-Level Security**:

```java
@RestController
@RequestMapping("/tasks")
public class TaskController {
    
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/admin/delete-all")
    public ResponseEntity<Void> deleteAllTasks() {
        taskService.deleteAll();
        return ResponseEntity.noContent().build();
    }
    
    @PreAuthorize("#username == authentication.principal.username or hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/user/{username}/tasks")
    public ResponseEntity<Void> deleteUserTasks(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails userDetails) {
        taskService.deleteByUsername(username);
        return ResponseEntity.noContent().build();
    }
}
```

**@EnableMethodSecurity** required in SecurityConfig:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    // ...
}
```

**SecurityContext Access Patterns**:

```java
// In a service
@Service
public class TaskService {
    
    public Task create(CreateTaskRequest request, String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities()
            .stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        // Business logic
    }
}

// In controller
@GetMapping("/tasks/mine")
public ResponseEntity<List<TaskResponse>> getMyTasks(
        @AuthenticationPrincipal UserDetails userDetails) {
    String username = userDetails.getUsername();
    return ResponseEntity.ok(taskService.findByUsername(username));
}
```

### Audit Checkpoint

- [ ] What is the difference between hasAuthority() and hasRole()?
- [ ] Why would you use @PreAuthorize instead of request matchers in SecurityFilterChain?
- [ ] What is the security risk if your refresh token never expires?
- [ ] How does Role hierarchy work (ROLE_ADMIN includes ROLE_USER)?
- [ ] Can the same JWT be used after a role change? Why or why not?

---

## Week 4 Audit Checklist

Before proceeding to Week 5, verify you can answer:

### JWT Fundamentals
- [ ] Draw a JWT and label the three parts with their Base64URL contents
- [ ] Explain HS256 vs RS256 signing and when to use each
- [ ] Describe the JWT lifecycle: generation, transmission, validation

### Spring Security Core
- [ ] Trace the authentication flow from login request to SecurityContext
- [ ] Explain the purpose of PasswordEncoder and why BCrypt is recommended
- [ ] Describe what UserDetailsService provides to DaoAuthenticationProvider

### Security Filter Chain
- [ ] Configure SecurityFilterChain with JWT authentication
- [ ] Explain why STATELESS session management is required for JWT
- [ ] Describe what OncePerRequestFilter accomplishes

### Implementation
- [ ] Implement JWT token generation with JJWT including expiration
- [ ] Implement JWT validation that checks expiration
- [ ] Secure POST/PUT/DELETE /tasks with JWT authentication
- [ ] Use @AuthenticationPrincipal to access authenticated user in controller

### Security Best Practices
- [ ] Explain why hardcoded secrets are dangerous
- [ ] Describe the minimum secret key length for HS256
- [ ] List why plain text password storage is unacceptable

---

## Common Security Pitfalls

### 1. Storing Passwords in Plain Text
**Problem**: Database breach exposes all user passwords.

**Fix**: Always use BCrypt via PasswordEncoderFactories.createDelegatingPasswordEncoder().

```java
// WRONG - NEVER DO THIS
user.setPassword(rawPassword);

// CORRECT
user.setPassword(passwordEncoder.encode(rawPassword));

// Verification uses matches()
passwordEncoder.matches(rawPassword, storedPassword);
```

### 2. Not Validating Token Expiration
**Problem**: Expired tokens remain valid indefinitely.

**Fix**: Always check `exp` claim on token validation:

```java
// In JwtTokenService
public boolean validateToken(String token) {
    try {
        Claims claims = extractClaims(token);
        return !claims.getExpiration().isBefore(Instant.now());
    } catch (JwtException | IllegalArgumentException e) {
        return false;
    }
}
```

### 3. Hardcoded Secrets
**Problem**: Secrets in source code get committed to version control.

**Fix**: Use environment variables or configuration files outside version control:

```properties
# application.properties - NOT source controlled
jwt.secret=${JWT_SECRET}
```

```bash
# Set before running
export JWT_SECRET=$(openssl rand -base64 64)
```

### 4. Missing Token Expiration
**Problem**: Tokens issued with no expiry remain valid forever if compromised.

**Fix**: Always set reasonable expiration:

```java
// Access token: 15 minutes to 1 hour
Jwt token expires in 15 minutes
refresh token expires in 7 days

// Or for simpler app: 24 hours
.expiration(expiration)  // Set explicit expiration time
```

### 5. Not Using HTTPS in Production
**Problem**: Tokens transmitted over HTTP can be intercepted.

**Fix**: Force HTTPS in production:

```java
@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .requiresChannel(channel -> channel
                .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
                    .requires(RequiresChannel.SecurityRequirement.REQUIRES_INSECURE_CHANNEL.equals("https") 
                        ? RequiresChannel.REQUIRES_SECURE_CHANNEL 
                        : RequiresChannel.REQUIRES_INSECURE_CHANNEL)
            );
        return http.build();
    }
}
```

### 6. Insufficient Secret Key Length
**Problem**: Using short secrets for HS256.

**Fix**: Use at least 256 bits (32 bytes):

```java
// Generate secure secret
// openssl rand -base64 64
// Must be at least 32 bytes for HS256
```

---

## Deep Dive Extras

### OAuth2 Resource Server (For Production)

Spring Security OAuth2 Resource Server provides built-in JWT validation when you have an issuer URI:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-auth-server.com
```

This handles:
- JWK Set retrieval
- Signature verification with public keys
- Token expiration validation
- Issuer validation

**When to use**: When you have an external authorization server (Auth0, Okta, Keycloak).

### Spring Security 7 Updates (2024+)

Spring Security 7 introduced several changes:

1. **Lambda-based DSL**: Configuration uses lambdas instead of method chaining:
   ```java
   // Old (still works)
   http.authorizeHttpRequests()
       .requestMatchers("/public/**").permitAll();
   
   // New style
   http.authorizeHttpRequests(authorize -> authorize
       .requestMatchers("/public/**").permitAll()
   );
   ```

2. **Removed Legacy Modules**: Access decision APIs moved to `spring-security-access`

3. **JWT Improvements**: Better integration with OAuth2 resource server

**Migration note**: If following older tutorials, some method names changed. The core concepts remain the same.

### Further Reading
- [Spring Security 7.0 Release Notes](https://spring.io/security)
- [OAuth 2.0 Security Best Current Practice](https://datatracker.ietf.org/doc/html/draft-ietf-oauth-security-topics)
- [OWASP Authentication Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html)

---

## Next Week Preview

**Week 5: Database Persistence with JPA**
- JPA entities and repositories
- Entity relationships for Task and User
- Transaction management
- Query methods

---

## Sources

- [JWT.io Introduction](https://jwt.io/introduction/)
- [JWT.io Libraries](https://jwt.io/libraries/)
- [Spring Security Reference Documentation](https://docs.spring.io/spring-security/reference/)
- [Spring Security: Authentication Architecture](https://docs.spring.io/spring-security/reference/servlet/authentication/architecture.html)
- [Spring Security: Authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/index.html)
- [Spring Security: Passwords](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/index.html)
- [Spring Security: UserDetailsService](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/user-details-service.html)
- [Spring Security: JWT Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
