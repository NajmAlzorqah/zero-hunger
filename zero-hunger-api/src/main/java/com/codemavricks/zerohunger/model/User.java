package com.codemavricks.zerohunger.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User Entity - Represents a user in the Zero Hunger system.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. JPA Entity (Java Persistence API)</h3>
 * <ul>
 *   <li><b>@Entity</b>: Marks this class as a JPA entity that maps to a database table</li>
 *   <li><b>@Table</b>: Specifies the table name in the database ("users")</li>
 *   <li><b>Object-Relational Mapping (ORM)</b>: Automatic conversion between Java objects and database rows</li>
 *   <li><b>Serializable</b>: Allows the entity to be serialized for distributed systems and caching</li>
 * </ul>
 * 
 * <h3>2. Entity Lifecycle States</h3>
 * <ul>
 *   <li><b>New/Transient</b>: When created with 'new User()' - not yet persisted</li>
 *   <li><b>Managed</b>: After em.persist() or em.find() - tracked by EntityManager</li>
 *   <li><b>Detached</b>: After transaction commits - no longer tracked</li>
 *   <li><b>Removed</b>: After em.remove() - marked for deletion</li>
 * </ul>
 * 
 * <h3>3. Primary Key Generation</h3>
 * <ul>
 *   <li><b>@Id</b>: Marks the primary key field</li>
 *   <li><b>@GeneratedValue(IDENTITY)</b>: Database auto-generates the ID (AUTO_INCREMENT in MySQL)</li>
 * </ul>
 * 
 * <h3>4. Column Mapping & Constraints</h3>
 * <ul>
 *   <li><b>@Column(nullable=false)</b>: NOT NULL constraint at JPA level</li>
 *   <li><b>@Column(unique=true)</b>: UNIQUE constraint for email field</li>
 *   <li><b>@Column(name="...")</b>: Maps Java field to specific database column name</li>
 * </ul>
 * 
 * <h3>5. Collection Mapping (@ElementCollection)</h3>
 * <ul>
 *   <li><b>@ElementCollection</b>: Maps a collection of basic types (roles as Strings)</li>
 *   <li><b>@CollectionTable</b>: Creates a separate table (user_roles) for the collection</li>
 *   <li><b>FetchType.EAGER</b>: Loads roles immediately with the user (vs LAZY loading)</li>
 * </ul>
 * 
 * <h3>6. Database Schema Generation</h3>
 * <ul>
 *   <li>JPA can auto-create tables from this entity definition</li>
 *   <li>Controlled by hibernate.hbm2ddl.auto in persistence.xml</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li>Chapter: Data Persistence (JPA - Java Persistence API)</li>
 *   <li>Concept: Entities - Mapping Java classes to database tables</li>
 *   <li>Concept: Entity Lifecycle (New, Managed, Detached, Removed)</li>
 *   <li>Concept: Object-Relational Mapping (ORM)</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 * @see jakarta.persistence.Entity
 * @see jakarta.persistence.EntityManager
 */
@Entity
@Table(name = "users")
@jakarta.json.bind.annotation.JsonbTypeAdapter(UserAdapter.class)
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    private Double latitude;
    private Double longitude;

    @Column(name = "impact_score")
    private Integer impactScore = 0;

    @Column(nullable = false)
    private String status = "active";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    public User() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public void addRole(String role) {
        this.roles.add(role);
    }
}
