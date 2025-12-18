package com.codemavricks.zerohunger.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Notification Entity - Represents user notifications in the system.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. Advanced Column Definitions</h3>
 * <ul>
 *   <li><b>@Column(columnDefinition="TEXT")</b>: Stores large text data (JSON strings)</li>
 *   <li>Demonstrates storing semi-structured data in relational database</li>
 *   <li>Alternative to using @Lob for large objects</li>
 * </ul>
 * 
 * <h3>2. Nullable DateTime Patterns</h3>
 * <ul>
 *   <li>readAt field is null until notification is read</li>
 *   <li>Common pattern for tracking state changes</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
@Entity
@Table(name = "notifications")
public class Notification implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String type;

    @Column(columnDefinition = "TEXT")
    private String data; // Stores JSON data as string

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Notification() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
