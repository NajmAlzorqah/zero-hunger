package com.codemavricks.zerohunger.dto;

import jakarta.validation.constraints.*;

/**
 * Login Request DTO - Request Body for Authentication.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. REQUEST DTO (INCOMING DATA)</h3>
 * <ul>
 *   <li><b>Purpose</b>: Type-safe container for JSON request body</li>
 *   <li><b>JAX-RS Deserialization</b>: JSON → Java object automatically</li>
 *   <li>POST /api/v1/login with body: {"email": "...", "password": "..."}</li>
 *   <li>JAX-RS creates LoginRequest instance and populates fields</li>
 * </ul>
 * 
 * <h3>2. PUBLIC FIELDS VS GETTERS/SETTERS</h3>
 * <ul>
 *   <li>Simple DTOs often use public fields for brevity</li>
 *   <li>JSON-B (Jakarta JSON Binding) accesses fields directly</li>
 *   <li>Alternative: Use private fields + getters/setters (more verbose)</li>
 * </ul>
 * 
 * <h3>3. BEAN VALIDATION INTEGRATION</h3>
 * <ul>
 *   <li><b>@NotBlank</b>: Ensures email and password are provided</li>
 *   <li><b>@Email</b>: Validates email format before authentication attempt</li>
 *   <li><b>Container-Automatic Validation</b>: JAX-RS validates when @Valid is used in endpoint</li>
 *   <li>Container validates automatically before method call</li>
 *   <li><b>Book Concept</b>: Bean Validation in Jakarta EE</li>
 * </ul>
 * 
 * <h3>4. VALIDATION LIFECYCLE</h3>
 * <ul>
 *   <li>1. Client sends JSON request</li>
 *   <li>2. JAX-RS deserializes JSON to LoginRequest</li>
 *   <li>3. Bean Validation validates constraints (if @Valid in endpoint)</li>
 *   <li>4. If valid: AuthResource.login() executes</li>
 *   <li>5. If invalid: 400 Bad Request with validation errors</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Concept</b>: Request DTOs for type-safe input</li>
 *   <li><b>Concept</b>: JSON-B automatic deserialization</li>
 *   <li><b>Concept</b>: Bean Validation (@NotBlank, @Email)</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
public class LoginRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    public String email;
    
    @NotBlank(message = "Password is required")
    public String password;
}
