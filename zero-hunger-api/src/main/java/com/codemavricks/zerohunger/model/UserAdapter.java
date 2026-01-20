package com.codemavricks.zerohunger.model;

import jakarta.json.bind.adapter.JsonbAdapter;
import com.codemavricks.zerohunger.model.User;

/**
 * User JSON-B Adapter - Alternative Security Approach for Entity Serialization.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. JSON-B ADAPTER PATTERN</h3>
 * <ul>
 *   <li><b>JsonbAdapter</b>: Customizes JSON serialization for User entity</li>
 *   <li>adaptToJson(): Controls what gets sent to client</li>
 *   <li>adaptFromJson(): Controls deserialization from client</li>
 *   <li>Applied with: @JsonbTypeAdapter(UserAdapter.class) on User entity</li>
 * </ul>
 * 
 * <h3>2. SECURITY: PASSWORD EXCLUSION</h3>
 * <ul>
 *   <li><b>Problem</b>: User entity has password field</li>
 *   <li><b>Solution</b>: Adapter creates copy WITHOUT password</li>
 *   <li>adaptToJson() clones user, omits password</li>
 *   <li>Alternative to DTO pattern (but DTO is cleaner)</li>
 * </ul>
 * 
 * <h3>3. ADAPTER VS DTO (DESIGN DECISION)</h3>
 * <ul>
 *   <li><b>This Project Uses</b>: Both (redundancy)</li>
 *   <li><b>UserAdapter</b>: Applied to entity, auto-removes password</li>
 *   <li><b>UserDTO</b>: Explicit conversion in resource layer</li>
 *   <li><b>Recommendation</b>: Choose one approach:</li>
 *   <ul>
 *     <li>DTOs: More control, clearer separation</li>
 *     <li>Adapters: Automatic, less boilerplate</li>
 *   </ul>
 * </ul>
 * 
 * <h3>4. CLONING OVERHEAD</h3>
 * <ul>
 *   <li>Creates new User object on every serialization</li>
 *   <li>Copies all fields except password</li>
 *   <li>Performance impact on high-traffic APIs</li>
 *   <li>Trade-off: Security vs performance</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Concept</b>: JSON-B (Jakarta JSON Binding) customization</li>
 *   <li><b>Pattern</b>: Adapter pattern for serialization control</li>
 *   <li><b>Security</b>: Preventing sensitive data exposure</li>
 * </ul>
 * 
 * <h3>Usage Example:</h3>
 * <pre>
 * &#64;Entity
 * &#64;JsonbTypeAdapter(UserAdapter.class) // ← Applied here
 * public class User {
 *     private String password; // Will be excluded in JSON
 * }
 * 
 * // When User entity is serialized to JSON:
 * User user = em.find(User.class, 1L);
 * String json = JsonbBuilder.create().toJson(user);
 * // Adapter.adaptToJson() called automatically
 * // Result: {"id":1, "name":"...", "email":"..."} (no password)
 * </pre>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
public class UserAdapter implements JsonbAdapter<User, User> {
    @Override
    public User adaptToJson(User user) {
        // Clone or modify to hide password
        User copy = new User();
        copy.setId(user.getId());
        copy.setName(user.getName());
        copy.setEmail(user.getEmail());
        // Ensure phone is null if empty or whitespace, never empty string
        String phone = user.getPhone();
        copy.setPhone((phone == null || phone.trim().isEmpty()) ? null : phone);
        copy.setLatitude(user.getLatitude());
        copy.setLongitude(user.getLongitude());
        // Ensure impactScore is never null, default to 0
        copy.setImpactScore(user.getImpactScore() != null ? user.getImpactScore() : 0);
        // Ensure status is never null, default to "active"
        copy.setStatus(user.getStatus() != null ? user.getStatus() : "active");
        copy.setRoles(user.getRoles());
        copy.setCreatedAt(user.getCreatedAt());
        // Password is NOT set
        return copy;
    }

    @Override
    public User adaptFromJson(User adapted) {
        return adapted;
    }
}
