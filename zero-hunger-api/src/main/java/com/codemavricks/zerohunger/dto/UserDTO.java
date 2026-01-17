package com.codemavricks.zerohunger.dto;

import com.codemavricks.zerohunger.model.User;
import java.util.Set;
import java.time.LocalDateTime;
import jakarta.json.bind.annotation.JsonbProperty;

/**
 * User Data Transfer Object - Secure User Representation for API Responses.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. DATA TRANSFER OBJECT (DTO) PATTERN</h3>
 * <ul>
 *   <li><b>Purpose</b>: Decouple JPA entities from API responses</li>
 *   <li><b>Security</b>: Excludes password field (User entity has it, DTO doesn't)</li>
 *   <li><b>Serialization</b>: Plain POJO, safe to serialize to JSON</li>
 *   <li><b>No JPA Annotations</b>: Not an entity, just a data container</li>
 * </ul>
 * 
 * <h3>2. WHY NOT SEND ENTITIES DIRECTLY?</h3>
 * <ul>
 *   <li><b>LazyInitializationException</b>:</li>
 *   <ul>
 *     <li>If User.donations is LAZY loaded and not fetched</li>
 *     <li>JSON serializer tries to access it outside transaction → CRASH</li>
 *   </ul>
 *   <li><b>Circular References</b>:</li>
 *   <ul>
 *     <li>User → Donation → Claim → User (infinite loop)</li>
 *     <li>JSON serializer fails or creates huge response</li>
 *   </ul>
 *   <li><b>Security Leaks</b>:</li>
 *   <ul>
 *     <li>User entity has password field</li>
 *     <li>Accidentally exposing it in API = major security breach</li>
 *   </ul>
 *   <li><b>Database Changes Persist</b>:</li>
 *   <ul>
 *     <li>If you send managed entity and client modifies it</li>
 *     <li>Changes might sync to database unintentionally</li>
 *   </ul>
 * </ul>
 * 
 * <h3>3. ENTITY → DTO CONVERSION PATTERN</h3>
 * <ul>
 *   <li>Constructor: new UserDTO(User entity)</li>
 *   <li>Copies safe fields: id, name, email, phone, coordinates, score, roles</li>
 *   <li>Omits sensitive fields: password</li>
 *   <li>Called in service/resource layer before returning response</li>
 * </ul>
 * 
 * <h3>4. AUTOMATIC JSON SERIALIZATION</h3>
 * <ul>
 *   <li>JAX-RS automatically converts UserDTO → JSON</li>
 *   <li>Uses Jackson or JSON-B (Jakarta JSON Binding)</li>
 *   <li>Field names become JSON keys: impactScore → "impactScore"</li>
 *   <li>Alternative: Use @JsonProperty to customize names</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Pattern</b>: Data Transfer Object (DTO) for decoupling layers</li>
 *   <li><b>Concept</b>: Detached entities vs DTOs</li>
 *   <li><b>Concept</b>: JSON serialization in JAX-RS</li>
 *   <li><b>Security</b>: Preventing sensitive data exposure</li>
 * </ul>
 * 
 * <h3>Example Usage:</h3>
 * <pre>
 * // BAD: Don't do this
 * &#64;GET
 * public Response getUser() {
 *     User user = em.find(User.class, 1L);
 *     return Response.ok(user).build(); // ❌ Exposes password!
 * }
 * 
 * // GOOD: Use DTO
 * &#64;GET
 * public Response getUser() {
 *     User user = em.find(User.class, 1L);
 *     UserDTO dto = new UserDTO(user); // ✓ Safe conversion
 *     return Response.ok(dto).build();
 * }
 * </pre>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 * @see User
 */
public class UserDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Double latitude;
    private Double longitude;
    
    @JsonbProperty("impact_score")
    private Integer impactScore;
    
    private String status;
    private Set<String> roles;
    
    @JsonbProperty("created_at")
    private LocalDateTime createdAt;

    public UserDTO() {}

    public UserDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.latitude = user.getLatitude();
        this.longitude = user.getLongitude();
        this.impactScore = user.getImpactScore();
        this.status = user.getStatus();
        this.roles = user.getRoles();
        this.createdAt = user.getCreatedAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getImpactScore() { return impactScore; }
    public void setImpactScore(Integer impactScore) { this.impactScore = impactScore; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
