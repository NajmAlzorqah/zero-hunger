package com.codemavricks.zerohunger.dto;

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
 * <h3>3. VALIDATION (POTENTIAL ENHANCEMENT)</h3>
 * <ul>
 *   <li><b>Current</b>: Manual validation in AuthResource</li>
 *   <li><b>Enhancement</b>: Add Bean Validation annotations:</li>
 *   <pre>
 *   &#64;NotNull &#64;Email
 *   public String email;
 *   
 *   &#64;NotNull &#64;Size(min=6)
 *   public String password;
 *   </pre>
 *   <li>Container validates automatically before method call</li>
 *   <li><b>Book Concept</b>: Bean Validation in Jakarta EE</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Concept</b>: Request DTOs for type-safe input</li>
 *   <li><b>Concept</b>: JSON-B automatic deserialization</li>
 *   <li><b>Missing</b>: Bean Validation (@NotNull, @Email)</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
public class LoginRequest {
    public String email;
    public String password;
}
