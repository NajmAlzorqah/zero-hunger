package com.codemavricks.zerohunger.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Claim Entity - Represents a volunteer's claim on a donation.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. Bidirectional JPA Relationships</h3>
 * <ul>
 *   <li><b>@OneToOne</b>: One claim per donation (enforced by unique=true)</li>
 *   <li><b>unique=true</b>: Database-level constraint preventing duplicate claims</li>
 *   <li><b>FetchType.LAZY</b>: Donation loaded only when accessed (performance optimization)</li>
 *   <li><b>@ManyToOne</b>: Many claims can belong to one volunteer</li>
 * </ul>
 * 
 * <h3>2. Relationship Ownership</h3>
 * <ul>
 *   <li>This entity OWNS the relationship (has @JoinColumn)</li>
 *   <li>Donation has 'mappedBy' - indicates inverse side of relationship</li>
 *   <li>Owner side controls the foreign key in database</li>
 * </ul>
 * 
 * <h3>3. Entity Lifecycle Management</h3>
 * <ul>
 *   <li><b>@PreUpdate</b>: Automatically updates 'updatedAt' on any change</li>
 *   <li>Default values set in Java constructor (status, timestamps)</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li>Concept: Bidirectional Relationships (owner vs inverse side)</li>
 *   <li>Concept: Lazy vs Eager Loading strategies</li>
 *   <li>Concept: Entity constraints and validation</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
@Entity
@Table(name = "claims")
public class Claim implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_id", nullable = false, unique = true)
    private Donation donation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "volunteer_id", nullable = false)
    private User volunteer;

    @Column(nullable = false)
    private String status = "active";

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Claim() {}

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Donation getDonation() { return donation; }
    public void setDonation(Donation donation) { this.donation = donation; }

    public User getVolunteer() { return volunteer; }
    public void setVolunteer(User volunteer) { this.volunteer = volunteer; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
