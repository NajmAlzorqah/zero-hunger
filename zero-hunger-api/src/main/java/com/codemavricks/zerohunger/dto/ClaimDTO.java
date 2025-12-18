package com.codemavricks.zerohunger.dto;

import com.codemavricks.zerohunger.model.Claim;
import java.time.LocalDateTime;

/**
 * Claim Data Transfer Object - Preventing Circular References.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. CIRCULAR REFERENCE PREVENTION</h3>
 * <ul>
 *   <li>Entity relationship: Claim ↔ Donation (bidirectional)</li>
 *   <li>Problem: claim.donation.claim.donation... (infinite loop)</li>
 *   <li>Solution: includeDonation flag controls depth</li>
 *   <li>When creating ClaimDTO from Donation context, includeDonation=false</li>
 * </ul>
 * 
 * <h3>2. CONDITIONAL RELATIONSHIP LOADING</h3>
 * <ul>
 *   <li>Constructor: ClaimDTO(Claim claim, boolean includeDonation)</li>
 *   <li>If includeDonation=true: creates DonationDTO(donation, includeClaim=false)</li>
 *   <li>If includeDonation=false: donation field stays null</li>
 *   <li>Different JSON structure for different API endpoints</li>
 * </ul>
 * 
 * <h3>3. DTO CHAINING STRATEGY</h3>
 * <ul>
 *   <li><b>GET /donations/123</b>:</li>
 *   <ul>
 *     <li>DonationDTO(donation, includeClaim=true, user)</li>
 *     <li>→ ClaimDTO(claim, includeDonation=false)</li>
 *     <li>Result: {donation: {claim: {volunteer: {...}}}}</li>
 *   </ul>
 *   <li><b>GET /claims</b>:</li>
 *   <ul>
 *     <li>ClaimDTO(claim, includeDonation=true)</li>
 *     <li>→ DonationDTO(donation, includeClaim=false, null)</li>
 *     <li>Result: {claim: {donation: {donor: {...}}}}</li>
 *   </ul>
 * </ul>
 * 
 * <h3>4. DENORMALIZATION FOR API</h3>
 * <ul>
 *   <li>donationId field: Duplicate of donation.getId()</li>
 *   <li>Allows client to reference donation even when full object not included</li>
 *   <li>Trade-off: Slightly larger JSON vs always including full donation</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Pattern</b>: Controlled depth DTO conversion</li>
 *   <li><b>Concept</b>: Bidirectional relationships and serialization</li>
 *   <li><b>Concept</b>: API response optimization</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
public class ClaimDTO {
    private Long id;
    private Long donationId;
    private String status;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private String notes;
    private UserDTO volunteer;
    private DonationDTO donation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ClaimDTO() {}

    public ClaimDTO(Claim claim) {
        this(claim, false);
    }

    public ClaimDTO(Claim claim, boolean includeDonation) {
        this.id = claim.getId();
        this.donationId = claim.getDonation() != null ? claim.getDonation().getId() : null;
        this.status = claim.getStatus();
        this.pickedUpAt = claim.getPickedUpAt();
        this.deliveredAt = claim.getDeliveredAt();
        this.notes = claim.getNotes();
        this.createdAt = claim.getCreatedAt();
        this.updatedAt = claim.getUpdatedAt();
        
        if (claim.getVolunteer() != null) {
            this.volunteer = new UserDTO(claim.getVolunteer());
        }
        
        // Prevent circular reference: when including donation, don't include claim in the donation
        if (includeDonation && claim.getDonation() != null) {
            this.donation = new DonationDTO(claim.getDonation(), false, null);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDonationId() { return donationId; }
    public void setDonationId(Long donationId) { this.donationId = donationId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }

    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public UserDTO getVolunteer() { return volunteer; }
    public void setVolunteer(UserDTO volunteer) { this.volunteer = volunteer; }

    public DonationDTO getDonation() { return donation; }
    public void setDonation(DonationDTO donation) { this.donation = donation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
