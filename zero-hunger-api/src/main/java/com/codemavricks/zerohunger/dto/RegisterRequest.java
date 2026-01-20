package com.codemavricks.zerohunger.dto;

import jakarta.validation.constraints.*;

/**
 * Register Request DTO - User Registration Data.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. MULTI-FIELD REQUEST DTO</h3>
 * <ul>
 *   <li>Captures all registration fields in one object</li>
 *   <li>JAX-RS deserializes JSON body to this POJO</li>
 *   <li>Cleaner than: @FormParam for each field</li>
 * </ul>
 * 
 * <h3>2. ROLE-BASED REGISTRATION</h3>
 * <ul>
 *   <li>role field: "donor" or "volunteer"</li>
 *   <li>Determines user permissions at registration time</li>
 *   <li>AuthResource converts to User.roles Set</li>
 * </ul>
 * 
 * <h3>3. BEAN VALIDATION ANNOTATIONS (JSR 380)</h3>
 * <ul>
 *   <li><b>@NotBlank</b>: Field must not be null, empty, or whitespace-only</li>
 *   <li><b>@Size</b>: Validates string length constraints</li>
 *   <li><b>@Email</b>: Validates email format (RFC 5322)</li>
 *   <li><b>@Pattern</b>: Regular expression validation</li>
 *   <li><b>Container-Automatic Validation</b>: JAX-RS validates before method call</li>
 *   <li><b>400 Bad Request</b>: Automatically returned if validation fails</li>
 * </ul>
 * 
 * <h3>4. VALIDATION ERROR HANDLING</h3>
 * <ul>
 *   <li>JAX-RS container validates parameters annotated with @Valid</li>
 *   <li>ConstraintViolationException thrown on validation failure</li>
 *   <li>Can create ExceptionMapper for custom error responses</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Chapter</b>: Bean Validation (JSR 380)</li>
 *   <li><b>Concept</b>: Declarative validation constraints</li>
 *   <li><b>Concept</b>: Request DTOs with validation</li>
 *   <li><b>Concept</b>: Container-managed validation lifecycle</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
public class RegisterRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    public String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    public String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    public String password;
    
    // Frontend sends this for client-side validation, backend ignores it
    public String password_confirmation;
    
    // Allow null, empty string, or valid phone number format
    // Pattern only validates if the field is not null/empty
    @Pattern(regexp = "^(|[+]?[0-9]{7,15})$", message = "Phone must be a valid phone number (7-15 digits)")
    public String phone;
    
    @NotBlank(message = "Role is required")
    @Pattern(regexp = "donor|volunteer|recipient", message = "Role must be 'donor', 'volunteer', or 'recipient'")
    public String role; // "donor", "volunteer", "recipient"
}
