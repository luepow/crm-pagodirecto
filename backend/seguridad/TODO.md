# Seguridad Module - TODO Checklist

## ✅ Completed (29 files)

### Domain Layer (8 files)
- [x] Usuario.java
- [x] UsuarioStatus.java
- [x] Rol.java
- [x] Permiso.java
- [x] PermissionAction.java
- [x] RefreshToken.java
- [x] AuditLog.java
- [x] AuditResultado.java

### Application Layer - DTOs (8 files)
- [x] LoginRequest.java
- [x] LoginResponse.java
- [x] RegisterRequest.java
- [x] UserDTO.java
- [x] RoleDTO.java
- [x] PermissionDTO.java
- [x] RefreshTokenRequest.java
- [x] ChangePasswordRequest.java

### Application Layer - Exceptions (4 files)
- [x] SecurityException.java
- [x] AuthenticationException.java
- [x] UserNotFoundException.java
- [x] InvalidTokenException.java

### Infrastructure Layer - Repositories (5 files)
- [x] UsuarioRepository.java
- [x] RolRepository.java
- [x] PermisoRepository.java
- [x] RefreshTokenRepository.java
- [x] AuditLogRepository.java

### Infrastructure Layer - Security (4 files)
- [x] JwtTokenProvider.java
- [x] JwtAuthenticationFilter.java
- [x] RLSContextManager.java
- [x] SecurityConfig.java

---

## 🚧 Pending Implementation

### Priority 1: Core Services (REQUIRED)

#### 1. AuthenticationService
**Location**: `application/service/AuthenticationService.java`

```java
public interface AuthenticationService {
    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);
    UserDTO register(RegisterRequest request, UUID createdBy);
    LoginResponse refreshToken(String refreshToken, String ipAddress, String userAgent);
    void logout(String refreshToken, UUID userId);
    void validateMfaCode(UUID userId, String totpCode);
}
```

**Implementation**: `application/service/impl/AuthenticationServiceImpl.java`

Key responsibilities:
- Validate credentials with BCrypt
- Track login attempts and lock accounts after 5 failures
- Generate JWT access and refresh tokens
- Store refresh tokens in database
- Log all authentication events to audit log
- Support MFA validation

#### 2. UserService
**Location**: `application/service/UserService.java`

```java
public interface UserService {
    UserDTO createUser(RegisterRequest request, UUID createdBy);
    UserDTO getUserById(UUID id);
    UserDTO getUserByUsername(String username);
    Page<UserDTO> getAllUsers(Pageable pageable);
    UserDTO updateUser(UUID id, UserDTO userDto, UUID updatedBy);
    void deleteUser(UUID id, UUID deletedBy);
    void changePassword(UUID userId, ChangePasswordRequest request);
    void enableUser(UUID id, UUID updatedBy);
    void disableUser(UUID id, UUID updatedBy);
    void unlockUser(UUID id, UUID updatedBy);
    void assignRole(UUID userId, UUID roleId, UUID assignedBy);
    void removeRole(UUID userId, UUID roleId, UUID removedBy);
}
```

**Implementation**: `application/service/impl/UserServiceImpl.java`

Key responsibilities:
- CRUD operations for users
- Password validation and encryption
- Role assignment/removal
- Account status management
- Audit all user changes

#### 3. RoleService
**Location**: `application/service/RoleService.java`

```java
public interface RoleService {
    RoleDTO createRole(RoleDTO roleDto, UUID createdBy);
    RoleDTO getRoleById(UUID id);
    Page<RoleDTO> getAllRoles(Pageable pageable);
    RoleDTO updateRole(UUID id, RoleDTO roleDto, UUID updatedBy);
    void deleteRole(UUID id, UUID deletedBy);
    void assignPermission(UUID roleId, UUID permissionId, UUID assignedBy);
    void removePermission(UUID roleId, UUID permissionId, UUID removedBy);
    List<RoleDTO> getRolesByDepartment(String department);
}
```

**Implementation**: `application/service/impl/RoleServiceImpl.java`

#### 4. AuditService
**Location**: `application/service/AuditService.java`

```java
public interface AuditService {
    void logAction(UUID userId, String action, String resource, UUID resourceId,
                   String ipAddress, String userAgent, AuditResultado resultado,
                   Map<String, Object> metadata, String errorMessage);
    Page<AuditLog> getAuditLogsByUser(UUID userId, Pageable pageable);
    Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable);
    Page<AuditLog> getAuditLogsByResource(String resource, Pageable pageable);
    Page<AuditLog> getAuditLogsByDateRange(Instant from, Instant to, Pageable pageable);
    Page<AuditLog> searchAuditLogs(AuditSearchCriteria criteria, Pageable pageable);
}
```

**Implementation**: `application/service/impl/AuditServiceImpl.java`

---

### Priority 2: MapStruct Mappers (REQUIRED)

#### 1. UserMapper
**Location**: `application/mapper/UserMapper.java`

See IMPLEMENTATION_SUMMARY.md for complete code.

#### 2. RoleMapper
**Location**: `application/mapper/RoleMapper.java`

See IMPLEMENTATION_SUMMARY.md for complete code.

#### 3. PermissionMapper
**Location**: `application/mapper/PermissionMapper.java`

See IMPLEMENTATION_SUMMARY.md for complete code.

---

### Priority 3: REST Controllers (REQUIRED)

#### 1. AuthController
**Location**: `api/controller/AuthController.java`

```java
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthController {

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest);

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register new user account")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request);

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Get new access token using refresh token")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request,
                                                      HttpServletRequest httpRequest);

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoke refresh token and logout user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request);
}
```

#### 2. UserController
**Location**: `api/controller/UserController.java`

```java
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User management endpoints")
@PreAuthorize("hasRole('Admin')")
public class UserController {

    @GetMapping
    @PreAuthorize("hasAuthority('users:read')")
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable);

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('users:read')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id);

    @PostMapping
    @PreAuthorize("hasAuthority('users:write')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody RegisterRequest request);

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('users:write')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable UUID id,
                                              @Valid @RequestBody UserDTO userDto);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('users:delete')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id);

    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasAuthority('users:write') or #id == authentication.principal")
    public ResponseEntity<Void> changePassword(@PathVariable UUID id,
                                               @Valid @RequestBody ChangePasswordRequest request);
}
```

#### 3. RoleController
**Location**: `api/controller/RoleController.java`

Similar structure to UserController for role management.

---

### Priority 4: Exception Handler (REQUIRED)

#### GlobalExceptionHandler
**Location**: `api/exception/GlobalExceptionHandler.java`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(AuthenticationException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED, ex.getMessage()
        );
        problem.setTitle("Authentication Failed");
        problem.setType(URI.create("https://api.pagodirecto.com/errors/authentication-failed"));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFoundException(UserNotFoundException ex) {
        // Implement RFC 7807 Problem Details
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ProblemDetail> handleInvalidTokenException(InvalidTokenException ex) {
        // Implement RFC 7807 Problem Details
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {
        // Handle Jakarta Validation errors
    }
}
```

---

### Priority 5: Database Migration (REQUIRED)

#### RLS Function Migration
**Location**: `application/src/main/resources/db/migration/V4__create_rls_function.sql`

```sql
-- Función para establecer contexto de sesión RLS
CREATE OR REPLACE FUNCTION set_app_session_context(
    p_tenant_id UUID,
    p_user_id UUID,
    p_roles TEXT
) RETURNS VOID AS $$
BEGIN
    PERFORM set_config('app.current_tenant', p_tenant_id::TEXT, false);
    PERFORM set_config('app.current_user', p_user_id::TEXT, false);
    PERFORM set_config('app.current_roles', p_roles, false);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

COMMENT ON FUNCTION set_app_session_context IS
'Establece variables de sesión para Row-Level Security';
```

---

### Priority 6: Unit Tests (REQUIRED)

Create tests in `src/test/java/com/pagodirecto/seguridad/`:

1. **Service Tests**
   - `AuthenticationServiceTest.java`
   - `UserServiceTest.java`
   - `RoleServiceTest.java`
   - `AuditServiceTest.java`

2. **Security Tests**
   - `JwtTokenProviderTest.java`
   - `RLSContextManagerTest.java`

3. **Repository Tests**
   - `UsuarioRepositoryTest.java`
   - `RolRepositoryTest.java`
   - `RefreshTokenRepositoryTest.java`

Target: **>80% code coverage**

---

### Priority 7: Integration Tests (OPTIONAL)

1. **AuthenticationIntegrationTest.java**
   - Test login flow
   - Test token refresh
   - Test logout

2. **UserManagementIntegrationTest.java**
   - Test user CRUD with authorization
   - Test password change
   - Test account locking

3. **RoleManagementIntegrationTest.java**
   - Test role CRUD
   - Test permission assignment

---

## Implementation Order

Follow this order for smooth implementation:

```
1. MapStruct Mappers (compile-time dependency)
   ├── PermissionMapper
   ├── RoleMapper
   └── UserMapper

2. Application Services (business logic)
   ├── AuditService (used by all others)
   ├── UserService
   ├── RoleService
   └── AuthenticationService

3. REST Controllers (API layer)
   ├── AuthController
   ├── UserController
   └── RoleController

4. Exception Handler (cross-cutting)
   └── GlobalExceptionHandler

5. Database Migration (infrastructure)
   └── V4__create_rls_function.sql

6. Unit Tests (quality assurance)
   └── All test files

7. Integration Tests (end-to-end validation)
   └── Integration test files
```

---

## Verification Commands

### 1. Build Project
```bash
cd /Users/lperez/Workspace/Development/next/crm_pd/backend
./mvnw clean install
```

### 2. Generate MapStruct Mappers
```bash
./mvnw clean compile -pl seguridad
```

### 3. Run Unit Tests
```bash
./mvnw test -pl seguridad
```

### 4. Check Code Coverage
```bash
./mvnw jacoco:report -pl seguridad
# Open: target/site/jacoco/index.html
```

### 5. Run Integration Tests
```bash
./mvnw verify -pl seguridad
```

### 6. Generate OpenAPI Docs
```bash
# Start application and visit:
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

---

## Success Criteria

The Seguridad module is complete when:

- [ ] All 29 completed files compile without errors
- [ ] All pending files are implemented (mappers, services, controllers)
- [ ] MapStruct generates mapper implementations successfully
- [ ] All unit tests pass with >80% coverage
- [ ] Integration tests validate authentication flow
- [ ] OpenAPI documentation is generated and accessible
- [ ] Database migrations run successfully
- [ ] JWT authentication works end-to-end
- [ ] RLS context is set correctly for all requests
- [ ] Audit logging captures all security events
- [ ] Password encryption uses BCrypt(12)
- [ ] Account lockout works after 5 failed attempts
- [ ] Token refresh mechanism works correctly

---

## Estimated Time

- **Mappers**: 1-2 hours
- **Services**: 4-6 hours
- **Controllers**: 3-4 hours
- **Exception Handler**: 1-2 hours
- **Unit Tests**: 4-6 hours
- **Integration Tests**: 2-3 hours
- **Testing & Debugging**: 2-4 hours

**Total**: 17-27 hours for complete implementation

---

## Need Help?

Refer to:
- `IMPLEMENTATION_SUMMARY.md` - Detailed code examples
- `README.md` - Architecture and usage guide
- `/Users/lperez/Workspace/Development/next/crm_pd/CLAUDE.md` - Project guidelines
- [Spring Security Docs](https://spring.io/projects/spring-security)
- [MapStruct Docs](https://mapstruct.org/)
