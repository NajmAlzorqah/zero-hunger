package com.codemavricks.zerohunger.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Donation Entity - Represents a food donation in the system.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. JPA Entity Relationships</h3>
 * <ul>
 *   <li><b>@ManyToOne</b>: Many donations can belong to one donor (User)</li>
 *   <li><b>@JoinColumn</b>: Specifies the foreign key column (donor_id)</li>
 *   <li><b>FetchType.EAGER</b>: Loads the donor immediately (vs LAZY which loads on-demand)</li>
 *   <li><b>@OneToOne(mappedBy)</b>: Bidirectional relationship with Claim entity</li>
 *   <li><b>CascadeType.ALL</b>: Operations on Donation cascade to Claim (delete, update, etc.)</li>
 * </ul>
 * 
 * <h3>2. Entity Lifecycle Callbacks</h3>
 * <ul>
 *   <li><b>@PrePersist</b>: Executed BEFORE entity is inserted into database</li>
 *   <li><b>@PreUpdate</b>: Executed BEFORE entity is updated in database</li>
 *   <li>Automatically sets createdAt and updatedAt timestamps</li>
 *   <li>Alternative to database triggers - logic stays in Java code</li>
 * </ul>
 * 
 * <h3>3. Advanced Column Mapping</h3>
 * <ul>
 *   <li>Maps Java camelCase (quantityKg) to snake_case (quantity_kg) columns</li>
 *   <li>Default values set in Java (status = "available")</li>
 *   <li>LocalDateTime automatically mapped to DATETIME/TIMESTAMP in MySQL</li>
 * </ul>
 * 
 * <h3>4. Relationship Cardinality</h3>
 * <ul>
 *   <li><b>Many-to-One</b>: donor relationship (many donations → one user)</li>
 *   <li><b>One-to-One</b>: claim relationship (one donation → one claim max)</li>
 *   <li>Demonstrates complex entity graphs in ORM</li>
 * </ul>
 * 
 * <h3>5. BEAN VALIDATION ON ENTITIES</h3>
 * <ul>
 *   <li><b>@NotBlank</b>: Title and status must not be null/empty</li>
 *   <li><b>@Size</b>: String length constraints for title and description</li>
 *   <li><b>@NotNull</b>: Donor and quantity cannot be null</li>
 *   <li><b>@Positive</b>: Quantity must be > 0 (business rule)</li>
 *   <li><b>@DecimalMin/@DecimalMax</b>: Geographic coordinate validation</li>
 *   <li><b>@Pattern</b>: Status must match specific values</li>
 *   <li><b>Validation Timing</b>: Executes on em.persist() and em.merge()</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li>Chapter: Advanced Persistence - Relationships (One-to-One, One-to-Many, Many-to-One)</li>
 *   <li>Concept: Entity Lifecycle Callbacks (@PrePersist, @PreUpdate, @PostLoad)</li>
 *   <li>Concept: Fetch Strategies (EAGER vs LAZY loading)</li>
 *   <li>Concept: Cascade Operations (persist, merge, remove)</li>
 *   <li>Concept: Bean Validation integration with JPA lifecycle</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
@Entity
@Table(name = "donations")
public class Donation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Donor cannot be null")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @NotBlank(message = "Title cannot be blank")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    @Column(nullable = false)
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be greater than 0")
    @DecimalMax(value = "10000.0", message = "Quantity cannot exceed 10000 kg")
    @Column(name = "quantity_kg", nullable = false)
    private Double quantityKg;

    @NotBlank(message = "Status cannot be blank")
    @Pattern(regexp = "available|reserved|claimed|picked_up|delivered|completed|cancelled|expired", 
             message = "Status must be available, reserved, claimed, picked_up, delivered, completed, cancelled, or expired")
    @Column(nullable = false)
    private String status = "available";

    @Column(name = "pickup_code")
    private String pickupCode;
    
    @Column(name = "delivery_code")
    private String deliveryCode;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    private Double longitude;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToOne(mappedBy = "donation", cascade = CascadeType.ALL)
    private Claim claim;

    public Donation() {}
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getDonor() { return donor; }
    public void setDonor(User donor) { this.donor = donor; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getQuantityKg() { return quantityKg; }
    public void setQuantityKg(Double quantityKg) { this.quantityKg = quantityKg; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPickupCode() { return pickupCode; }
    public void setPickupCode(String pickupCode) { this.pickupCode = pickupCode; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Claim getClaim() { return claim; }
    public void setClaim(Claim claim) { this.claim = claim; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getDeliveryCode() { return deliveryCode; }
    public void setDeliveryCode(String deliveryCode) { this.deliveryCode = deliveryCode; }
}
