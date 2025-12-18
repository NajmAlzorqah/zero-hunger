package com.codemavricks.zerohunger.dto;

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
 * <h3>3. POTENTIAL VALIDATION ENHANCEMENTS</h3>
 * <ul>
 *   <li>Add Bean Validation annotations:</li>
 *   <pre>
 *   &#64;NotBlank
 *   &#64;Size(min=2, max=100)
 *   public String name;
 *   
 *   &#64;Email
 *   &#64;NotNull
 *   public String email;
 *   
 *   &#64;Size(min=8)
 *   public String password;
 *   
 *   &#64;Pattern(regexp="donor|volunteer")
 *   public String role;
 *   </pre>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Concept</b>: Request DTOs for complex input</li>
 *   <li><b>Missing</b>: Bean Validation for declarative constraints</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
public class RegisterRequest {
    public String name;
    public String email;
    public String password;
    public String phone;
    public String role; // "donor", "volunteer", etc
}
