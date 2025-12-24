# Bean Validation Quick Reference

## Common Validation Annotations

### Null Checks

- `@NotNull` - Field cannot be null
- `@NotEmpty` - Collection/String cannot be null or empty
- `@NotBlank` - String cannot be null, empty, or whitespace-only
- `@Null` - Field must be null

### Size Constraints

- `@Size(min=, max=)` - String/Collection/Array size
- `@Length(min=, max=)` - String length (Hibernate)

### Numeric Constraints

- `@Min(value)` - Number must be >= value
- `@Max(value)` - Number must be <= value
- `@Positive` - Number must be > 0
- `@PositiveOrZero` - Number must be >= 0
- `@Negative` - Number must be < 0
- `@NegativeOrZero` - Number must be <= 0
- `@DecimalMin(value)` - Decimal must be >= value
- `@DecimalMax(value)` - Decimal must be <= value
- `@Digits(integer=, fraction=)` - Max digits before/after decimal

### String Patterns

- `@Email` - Valid email format (RFC 5322)
- `@Pattern(regexp=)` - Must match regular expression

### Temporal Constraints

- `@Past` - Date must be in the past
- `@PastOrPresent` - Date must be past or present
- `@Future` - Date must be in the future
- `@FutureOrPresent` - Date must be future or present

### Boolean

- `@AssertTrue` - Boolean must be true
- `@AssertFalse` - Boolean must be false

### Other

- `@Valid` - Cascade validation to nested object

## Examples from Zero Hunger Project

### RegisterRequest.java

```java
@NotBlank(message = "Name is required")
@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
public String name;

@NotBlank(message = "Email is required")
@Email(message = "Email must be a valid email address")
public String email;

@NotBlank(message = "Password is required")
@Size(min = 8, max = 100, message = "Password must be at least 8 characters")
public String password;

@Pattern(regexp = "donor|volunteer", message = "Role must be 'donor' or 'volunteer'")
public String role;
```

### CreateDonationRequest.java

```java
@NotBlank(message = "Title is required")
@Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
private String title;

@NotNull(message = "Quantity is required")
@Positive(message = "Quantity must be greater than 0")
@DecimalMax(value = "10000.0", message = "Quantity cannot exceed 10000 kg")
private Double quantityKg;

@DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
@DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
private Double latitude;

@Future(message = "Expiry date must be in the future")
private LocalDateTime expiresAt;
```

### User Entity

```java
@NotBlank(message = "Name cannot be blank")
@Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
@Column(nullable = false)
private String name;

@NotBlank(message = "Email cannot be blank")
@Email(message = "Email must be valid")
@Column(nullable = false, unique = true)
private String email;

@Pattern(regexp = "active|inactive|suspended",
         message = "Status must be active, inactive, or suspended")
@Column(nullable = false)
private String status = "active";
```

### Donation Entity

```java
@NotNull(message = "Donor cannot be null")
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "donor_id", nullable = false)
private User donor;

@NotNull(message = "Quantity cannot be null")
@Positive(message = "Quantity must be greater than 0")
@DecimalMax(value = "10000.0", message = "Quantity cannot exceed 10000 kg")
@Column(name = "quantity_kg", nullable = false)
private Double quantityKg;

@Pattern(regexp = "available|claimed|completed|cancelled|expired",
         message = "Status must be available, claimed, completed, cancelled, or expired")
@Column(nullable = false)
private String status = "available";
```

## Validation in JAX-RS Endpoints

To enable validation in JAX-RS, add `@Valid` to method parameters:

```java
@POST
@Path("register")
public Response register(@Valid RegisterRequest req) {
    // If validation fails, container returns 400 Bad Request
    // If validation passes, this method executes
    userService.register(req);
    return Response.ok().build();
}
```

## Validation in JPA

Validation happens automatically on:

- `entityManager.persist(entity)`
- `entityManager.merge(entity)`

```java
User user = new User();
user.setName("a"); // Too short - violates @Size(min=2)
user.setEmail("invalid"); // Invalid format - violates @Email

entityManager.persist(user); // Throws ConstraintViolationException
```

## Custom Validation Messages

All constraints support custom messages:

```java
@NotBlank(message = "Please provide your name")
@Size(min = 2, max = 100, message = "Name must be between {min} and {max} characters")
private String name;
```

Message parameters:

- `{min}` - Minimum value from annotation
- `{max}` - Maximum value from annotation
- `{value}` - The value being validated

## Handling Validation Errors

### Automatic (Container Handles)

When validation fails, JAX-RS automatically returns:

```json
{
  "status": 400,
  "message": "Validation failed",
  "violations": [
    {
      "field": "email",
      "message": "Email must be a valid email address"
    }
  ]
}
```

### Manual (Custom Exception Mapper)

Create an `ExceptionMapper` for custom error responses:

```java
@Provider
public class ValidationExceptionMapper
    implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Map<String, String> errors = new HashMap<>();

        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            String field = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(field, message);
        }

        return Response.status(400)
            .entity(Map.of("errors", errors))
            .build();
    }
}
```

## Validation Groups

For different validation rules in different scenarios:

```java
public interface CreateValidation {}
public interface UpdateValidation {}

public class DonationRequest {
    @NotNull(groups = CreateValidation.class)
    private String title;

    @Null(groups = UpdateValidation.class)
    private Long id; // ID should be null on create
}

// In endpoint:
@POST
public Response create(@Valid(CreateValidation.class) DonationRequest req) {
    // ...
}
```

## Testing Validation

### Valid Request

```bash
curl -X POST http://localhost:9090/api/v1/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "phone": "1234567890",
    "role": "donor"
  }'
```

### Invalid Request

```bash
curl -X POST http://localhost:9090/api/v1/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "a",
    "email": "invalid",
    "password": "123",
    "role": "invalid"
  }'
```

Expected error:

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "name": "Name must be between 2 and 100 characters",
    "email": "Email must be a valid email address",
    "password": "Password must be at least 8 characters",
    "role": "Role must be 'donor' or 'volunteer'"
  }
}
```

## Benefits of Bean Validation

✅ **Declarative** - Constraints are visible in the code  
✅ **Reusable** - Same constraints on DTOs and entities  
✅ **Automatic** - Container validates before your code runs  
✅ **Consistent** - Standard validation across the application  
✅ **Maintainable** - Easy to add/modify constraints  
✅ **Documentation** - Constraints serve as documentation

## References

- Jakarta Bean Validation Specification: https://jakarta.ee/specifications/bean-validation/
- Hibernate Validator Documentation: https://hibernate.org/validator/
- Built-in Constraints: https://jakarta.ee/specifications/bean-validation/3.0/apidocs/jakarta/validation/constraints/package-summary.html
