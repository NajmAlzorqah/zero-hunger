# JAAS Container-Managed Security Implementation Guide

## 📚 Overview

This document explains the **JAAS (Java Authentication and Authorization Service)** and **Bean Validation** implementations added to the Zero Hunger backend project. These are **Jakarta EE enterprise concepts** that demonstrate professional-grade security and validation patterns.

---

## 🔐 Container-Managed Security (JAAS)

### What is JAAS?

**JAAS** is Jakarta EE's pluggable authentication and authorization framework. It allows the **application server** (not your code) to handle authentication, making security **declarative** rather than programmatic.

### Key Concepts

#### 1. **Declarative Security**

Security rules are defined in `web.xml` or via annotations, not in Java code:

```xml
<security-constraint>
    <web-resource-collection>
        <url-pattern>/api/v1/donations/*</url-pattern>
    </web-resource-collection>
    <auth-constraint>
        <role-name>donor</role-name>
    </auth-constraint>
</security-constraint>
```

#### 2. **LoginModule**

Pluggable authentication mechanism that:

- Validates credentials against a data source (database, LDAP, etc.)
- Uses a **two-phase commit** pattern (login → commit)
- Populates the `Subject` with `Principal` objects (user identity and roles)

#### 3. **Subject and Principal**

- **Subject**: Represents the authenticated user
- **Principal**: User identity (e.g., email, username)
- **Role Principal**: User roles (e.g., "donor", "volunteer")

#### 4. **Security Realm**

A security domain configured in the application server that:

- Defines user repositories (database, file, LDAP)
- Maps LoginModules to authentication mechanisms
- Manages user/role mappings

---

## 🚨 Why JAAS is Commented Out

The JAAS configuration in this project is **commented out** to prevent conflicts with the existing authentication system. Here's why:

### Current Architecture (Active)

✅ **Custom JWT Token Authentication**

- REST API returns JSON responses
- Frontend sends `Authorization: Bearer <token>` header
- `AuthFilter.java` validates tokens on protected endpoints
- `CorsFilter.java` handles CORS for cross-origin requests
- Works seamlessly with SPA frontends (React, Vue, Angular)

### JAAS Architecture (Commented)

❌ **Container-Managed Form/Basic Authentication**

- Requires HTML login form or HTTP Basic Auth
- Server redirects to login page on 401
- Sessions managed by container
- **Not ideal for REST APIs** (designed for traditional web apps)
- Would interfere with CORS preflight OPTIONS requests
- Frontend would need significant changes

### Comparison Table

| Feature                | Current (JWT)   | JAAS (Commented)          |
| ---------------------- | --------------- | ------------------------- |
| Authentication         | Token-based     | Session-based             |
| Frontend Compatibility | ✅ SPA-friendly | ❌ Form-based             |
| CORS Handling          | ✅ CorsFilter   | ⚠️ Requires server config |
| API Design             | ✅ RESTful JSON | ⚠️ Redirects & HTML       |
| Scalability            | ✅ Stateless    | ⚠️ Stateful sessions      |
| Use Case               | Modern APIs     | Traditional web apps      |

---

## 📋 What Was Implemented

### 1. **Bean Validation Annotations** ✅ (Active)

Bean Validation is **fully integrated and active** in the project.

#### DTOs (Data Transfer Objects)

All request DTOs now have validation annotations:

**RegisterRequest.java:**

```java
@NotBlank(message = "Name is required")
@Size(min = 2, max = 100)
public String name;

@NotBlank(message = "Email is required")
@Email(message = "Email must be valid")
public String email;

@NotBlank(message = "Password is required")
@Size(min = 8)
public String password;

@Pattern(regexp = "donor|volunteer")
public String role;
```

**CreateDonationRequest.java:**

```java
@NotBlank
@Size(min = 3, max = 200)
private String title;

@NotNull
@Positive
@DecimalMax("10000.0")
private Double quantityKg;

@DecimalMin("-90.0")
@DecimalMax("90.0")
private Double latitude;

@Future
private LocalDateTime expiresAt;
```

#### Entities (JPA Models)

Entity classes have validation for persistence layer:

**User.java:**

```java
@NotBlank(message = "Name cannot be blank")
@Size(min = 2, max = 100)
@Column(nullable = false)
private String name;

@NotBlank
@Email
@Column(nullable = false, unique = true)
private String email;

@DecimalMin("-90.0")
@DecimalMax("90.0")
private Double latitude;

@Pattern(regexp = "active|inactive|suspended")
@Column(nullable = false)
private String status;
```

**Donation.java:**

```java
@NotNull
@Positive
@DecimalMax("10000.0")
@Column(name = "quantity_kg", nullable = false)
private Double quantityKg;

@Pattern(regexp = "available|claimed|completed|cancelled|expired")
@Column(nullable = false)
private String status;
```

#### How Bean Validation Works

1. **DTO Validation**: When client sends JSON request:

   ```
   Client POST /api/v1/register {"email": "invalid", "name": "a"}
      ↓
   JAX-RS deserializes JSON → RegisterRequest
      ↓
   Bean Validator checks constraints (if @Valid in endpoint)
      ↓
   If invalid: 400 Bad Request with validation errors
      ↓
   If valid: AuthResource.register() executes
   ```

2. **Entity Validation**: When persisting to database:
   ```
   userService.register(user)
      ↓
   entityManager.persist(user)
      ↓
   Bean Validator validates @NotBlank, @Email, etc.
      ↓
   If invalid: ConstraintViolationException thrown
      ↓
   If valid: User saved to database
   ```

### 2. **JAAS Configuration** 📝 (Commented, Documented)

Complete JAAS setup is provided in **commented form** for educational purposes:

#### web.xml

- **Security constraints**: Define protected URL patterns
- **Login config**: FORM or BASIC authentication setup
- **Security roles**: Declare application roles (donor, volunteer, admin)
- **Detailed comments**: Explain every concept

#### DatabaseLoginModule.java

A fully documented custom JAAS LoginModule demonstrating:

- **Login lifecycle**: `initialize()` → `login()` → `commit()` → `logout()`
- **Two-phase commit**: Why authentication happens in two steps
- **Callback handlers**: How container requests credentials
- **Subject/Principal**: How user identity is stored
- **Database authentication**: Query users and roles from MySQL
- **BCrypt password verification**: Secure password checking

---

## 🔧 How to Enable JAAS (If Needed)

If you want to **activate** Container-Managed Security:

### Step 1: Uncomment web.xml Security Sections

Edit `src/main/webapp/WEB-INF/web.xml`:

```xml
<!-- Remove comment markers around: -->
<security-constraint>...</security-constraint>
<login-config>...</login-config>
<security-role>...</security-role>
```

### Step 2: Configure JAAS Realm in Application Server

**For GlassFish/Payara:**

1. Open Admin Console: `http://localhost:4848`
2. Navigate to: **Configurations → server-config → Security → Realms**
3. Create new realm:
   - Name: `ZeroHungerRealm`
   - Class: `com.sun.enterprise.security.auth.realm.jdbc.JDBCRealm`
   - JNDI: `jdbc/ZeroHungerDS`
   - User Table: `users`
   - User Name Column: `email`
   - Password Column: `password`
   - Group Table: `user_roles`
   - Group Name Column: `role`
   - Digest Algorithm: `bcrypt`

**For WildFly:**
Edit `standalone.xml`:

```xml
<security-domain name="ZeroHungerRealm">
    <authentication>
        <login-module code="Database" flag="required">
            <module-option name="dsJndiName" value="java:jboss/datasources/ZeroHungerDS"/>
            <module-option name="principalsQuery" value="SELECT password FROM users WHERE email=?"/>
            <module-option name="rolesQuery" value="SELECT role FROM user_roles WHERE user_id=(SELECT id FROM users WHERE email=?)"/>
            <module-option name="hashAlgorithm" value="bcrypt"/>
        </login-module>
    </authentication>
</security-domain>
```

### Step 3: Disable Custom Authentication

Comment out or remove:

- `AuthFilter.java` (custom token validation)
- `CorsFilter.java` (move CORS to server config)
- `@Secured` annotations on resource methods

### Step 4: Use Programmatic Security in Resources

```java
@Path("/donations")
public class DonationResource {

    @Context
    private SecurityContext securityContext;

    @GET
    @RolesAllowed({"donor", "volunteer"})
    public Response getDonations() {
        // Get authenticated user
        Principal principal = securityContext.getUserPrincipal();
        String email = principal.getName();

        // Check roles
        boolean isDonor = securityContext.isUserInRole("donor");

        // Business logic...
    }
}
```

### Step 5: Update Frontend

Change authentication flow:

- Remove Bearer token handling
- Implement form-based login
- Handle session cookies
- Update CORS configuration

---

## 📖 Bean Validation Annotations Reference

| Annotation           | Description                            | Example                                   |
| -------------------- | -------------------------------------- | ----------------------------------------- |
| `@NotNull`           | Field cannot be null                   | `@NotNull Long id`                        |
| `@NotBlank`          | String cannot be null/empty/whitespace | `@NotBlank String name`                   |
| `@NotEmpty`          | Collection/String cannot be empty      | `@NotEmpty Set<String> roles`             |
| `@Size(min, max)`    | String/Collection size constraints     | `@Size(min=2, max=100) String name`       |
| `@Min(value)`        | Number must be >= value                | `@Min(0) Integer score`                   |
| `@Max(value)`        | Number must be <= value                | `@Max(100) Integer age`                   |
| `@Positive`          | Number must be > 0                     | `@Positive Double quantity`               |
| `@DecimalMin(value)` | Decimal must be >= value               | `@DecimalMin("-90") Double lat`           |
| `@DecimalMax(value)` | Decimal must be <= value               | `@DecimalMax("90") Double lat`            |
| `@Email`             | String must be valid email             | `@Email String email`                     |
| `@Pattern(regexp)`   | String must match regex                | `@Pattern(regexp="donor\|volunteer")`     |
| `@Past`              | Date must be in the past               | `@Past LocalDateTime createdAt`           |
| `@PastOrPresent`     | Date must be past or now               | `@PastOrPresent LocalDateTime pickedUpAt` |
| `@Future`            | Date must be in the future             | `@Future LocalDateTime expiresAt`         |
| `@Valid`             | Cascade validation to nested object    | `@Valid Address address`                  |

---

## 🎓 Related Jakarta EE Concepts

### 1. **Container-Managed vs Application-Managed Security**

- **Container-Managed (JAAS)**: Server handles authentication
- **Application-Managed (Current)**: Code handles authentication

### 2. **Declarative vs Programmatic Security**

- **Declarative**: `web.xml` security constraints, `@RolesAllowed`
- **Programmatic**: `SecurityContext.isUserInRole()`, manual checks

### 3. **Two-Phase Commit Pattern**

- **Phase 1 (login)**: Validate credentials
- **Phase 2 (commit)**: Add Principals to Subject
- Allows multiple LoginModules to participate

### 4. **Bean Validation Lifecycle**

- **Request validation**: Before controller method executes
- **Entity validation**: Before `persist()` or `merge()`
- **Custom validators**: Implement `ConstraintValidator`

---

## 🚀 Best Practices

### ✅ DO:

- Use Bean Validation for input validation (active in this project)
- Add custom error messages to validation annotations
- Validate at both DTO and Entity levels
- Document why security approaches are chosen
- Keep sensitive credentials out of code (use config files)

### ❌ DON'T:

- Mix JAAS and custom authentication (will conflict)
- Store passwords in plain text
- Use FORM auth for REST APIs
- Skip validation on user input
- Ignore validation errors

---

## 📚 Book References

- **Chapter**: Security in Jakarta EE

  - Declarative security with `web.xml`
  - Programmatic security with `SecurityContext`
  - JAAS LoginModules and Realms
  - Role-Based Access Control (RBAC)

- **Chapter**: Bean Validation (JSR 380)
  - Constraint annotations (`@NotNull`, `@Size`, etc.)
  - Custom validators
  - Validation groups
  - Integration with JPA and JAX-RS

---

## 🔍 Summary

| Concept                | Status             | Location                   | Notes                            |
| ---------------------- | ------------------ | -------------------------- | -------------------------------- |
| **Bean Validation**    | ✅ **Active**      | DTOs, Entities             | Validates input and persistence  |
| **JAAS Configuration** | 📝 **Documented**  | `web.xml`                  | Commented out to avoid conflicts |
| **Custom LoginModule** | 📝 **Implemented** | `DatabaseLoginModule.java` | Demo implementation              |
| **Current Auth**       | ✅ **Active**      | `AuthFilter.java`          | JWT token-based authentication   |
| **CORS Handling**      | ✅ **Active**      | `CorsFilter.java`          | Response filter for cross-origin |

---

## 📞 Next Steps

To fully utilize JAAS in production:

1. Evaluate if form-based auth fits your use case
2. Configure application server security realm
3. Migrate from token-based to session-based auth
4. Update frontend authentication flow
5. Test security constraints with different roles

For questions or clarifications, review:

- `web.xml` comments (detailed JAAS explanation)
- `DatabaseLoginModule.java` Javadocs (JAAS lifecycle)
- Entity and DTO files (Bean Validation examples)

---

**Authors**: Zero Hunger Team  
**Date**: December 25, 2025  
**Version**: 1.0
