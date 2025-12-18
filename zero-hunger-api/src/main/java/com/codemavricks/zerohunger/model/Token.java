package com.codemavricks.zerohunger.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Token Entity - Represents authentication tokens for API access.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. Security Token Management</h3>
 * <ul>
 *   <li>Persistent token storage for stateless authentication</li>
 *   <li>@Column(unique=true) ensures one token string across all users</li>
 *   <li>lastUsedAt tracks token activity for security auditing</li>
 * </ul>
 * 
 * <h3>2. Relationship to Security Context</h3>
 * <ul>
 *   <li>Links tokens to User entities</li>
 *   <li>Enables role-based access control (RBAC)</li>
 *   <li>Alternative to container-managed security (JAAS)</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
@Entity
@Table(name = "personal_access_tokens")
public class Token implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tokenable_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Token() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
