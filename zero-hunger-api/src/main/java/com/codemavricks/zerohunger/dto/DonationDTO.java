package com.codemavricks.zerohunger.dto;

import com.codemavricks.zerohunger.model.Donation;
import com.codemavricks.zerohunger.model.User;
import java.time.LocalDateTime;
import jakarta.json.bind.annotation.JsonbProperty;

/**
 * Donation Data Transfer Object - Complex DTO with Nested Relationships.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. NESTED DTO PATTERN</h3>
 * <ul>
 *   <li><b>UserDTO donor</b>: Embeds user information without circular reference</li>
 *   <li><b>ClaimDTO claim</b>: Optionally includes claim details</li>
 *   <li>Prevents infinite loops: Donation → Claim → Donation → ...</li>
 *   <li>Constructor flag: includeClaim (controls relationship loading)</li>
 * </ul>
 * 
 * <h3>2. CONDITIONAL FIELD INCLUSION</h3>
 * <ul>
 *   <li><b>pickupCode</b>: Only visible to donor or volunteer</li>
 *   <li>Security rule in constructor checks currentUser.id</li>
 *   <li>Same entity, different JSON output based on caller</li>
 *   <li>Demonstrates field-level authorization</li>
 * </ul>
 * 
 * <h3>3. COMPUTED FIELDS (NOT IN ENTITY)</h3>
 * <ul>
 *   <li><b>isExpired</b>: Calculated from expiresAt vs current time</li>
 *   <li><b>isAvailable</b>: Calculated from status and expiration</li>
 *   <li><b>distance</b>: Can be set after geo calculation</li>
 *   <li>Not stored in database, computed on-the-fly</li>
 *   <li>Alternative: Use @Transient in entity, but DTO is cleaner</li>
 * </ul>
 * 
 * <h3>4. PREVENTING LAZY LOADING ISSUES</h3>
 * <ul>
 *   <li>donation.getDonor() might be lazy-loaded proxy</li>
 *   <li>DTO constructor accesses it within transaction</li>
 *   <li>Converts to UserDTO before transaction closes</li>
 *   <li>Client receives fully-populated JSON, no lazy exceptions</li>
 * </ul>
 * 
 * <h3>5. DTO FACTORY PATTERN</h3>
 * <ul>
 *   <li>Multiple constructors for different use cases:</li>
 *   <ul>
 *     <li>DonationDTO(donation) → basic conversion</li>
 *     <li>DonationDTO(donation, includeClaim, currentUser) → full control</li>
 *   </ul>
 *   <li>Simplifies creation in different contexts</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Pattern</b>: Nested DTOs for complex object graphs</li>
 *   <li><b>Concept</b>: Lazy loading and session management</li>
 *   <li><b>Concept</b>: Computed properties vs stored fields</li>
 *   <li><b>Security</b>: Conditional field exposure based on user</li>
 * </ul>
 * 
 * <h3>Circular Reference Prevention:</h3>
 * <pre>
 * // Without DTO:
 * Donation → Claim → Donation → Claim → ... (INFINITE LOOP)
 * 
 * // With DTO:
 * DonationDTO(includeClaim=true) {
 *     this.donor = new UserDTO(donation.getDonor());
 *     this.claim = new ClaimDTO(donation.getClaim(), includeDonation=false); // ← STOPS HERE
 * }
 * 
 * ClaimDTO(includeDonation=false) {
 *     this.donation = null; // Don't include donation to prevent loop
 * }
 * </pre>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
public class DonationDTO {
    private Long id;
    private String title;
    private String description;
    
    @JsonbProperty("quantity_kg")
    private Double quantityKg;
    
    // Frontend also uses "quantity" as alias
    @JsonbProperty("quantity")
    private Double quantity;
    
    private String status;
    
    @JsonbProperty("food_type")
    private String foodType;
    
    @JsonbProperty("pickup_code")
    private String pickupCode;
    
    @JsonbProperty("pickup_address")
    private String pickupAddress;
    
    private Double latitude;
    private Double longitude;
    
    @JsonbProperty("expires_at")
    private LocalDateTime expiresAt;
    
    @JsonbProperty("is_expired")
    private Boolean isExpired;
    
    @JsonbProperty("is_available")
    private Boolean isAvailable;
    
    // For nested donor object
    private UserDTO donor;
    
    // Donor ID for flat reference
    @JsonbProperty("donor_id")
    private Long donorId;
    
    private ClaimDTO claim;
    private Double distance; // For nearby donations
    
    @JsonbProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonbProperty("updated_at")
    private LocalDateTime updatedAt;

    public DonationDTO() {}

    public DonationDTO(Donation donation) {
        this(donation, false, null);
    }

    public DonationDTO(Donation donation, boolean includeClaim, User currentUser) {
        this.id = donation.getId();
        this.title = donation.getTitle();
        this.description = donation.getDescription();
        this.quantityKg = donation.getQuantityKg();
        this.quantity = donation.getQuantityKg(); // Alias for frontend compatibility
        this.status = donation.getStatus();
        this.latitude = donation.getLatitude();
        this.longitude = donation.getLongitude();
        this.expiresAt = donation.getExpiresAt();
        this.createdAt = donation.getCreatedAt();
        this.updatedAt = donation.getUpdatedAt();
        
        // Set donor ID for flat reference
        if (donation.getDonor() != null) {
            this.donorId = donation.getDonor().getId();
        }
        
        // Set pickup address (generate from coordinates if not available)
        this.pickupAddress = generatePickupAddress(donation.getLatitude(), donation.getLongitude());
        
        // Default food type if not set
        this.foodType = "other";
        
        // Only show pickup code to donor or volunteer
        if (currentUser != null && donation.getDonor() != null) {
            if (currentUser.getId().equals(donation.getDonor().getId()) ||
                (donation.getClaim() != null && donation.getClaim().getVolunteer() != null &&
                 currentUser.getId().equals(donation.getClaim().getVolunteer().getId()))) {
                this.pickupCode = donation.getPickupCode();
            }
        }
        
        // Calculate availability
        this.isExpired = donation.getExpiresAt() != null && 
                        donation.getExpiresAt().isBefore(LocalDateTime.now()) &&
                        !"delivered".equals(donation.getStatus());
        this.isAvailable = "available".equals(donation.getStatus()) && 
                          (donation.getExpiresAt() == null || donation.getExpiresAt().isAfter(LocalDateTime.now()));
        
        // Include relationships
        if (donation.getDonor() != null) {
            this.donor = new UserDTO(donation.getDonor());
        }
        if (includeClaim && donation.getClaim() != null) {
            this.claim = new ClaimDTO(donation.getClaim());
        }
    }
    
    /**
     * Generate a simple pickup address string from coordinates.
     * In production, this would use a geocoding service.
     */
    private String generatePickupAddress(Double lat, Double lng) {
        if (lat == null || lng == null) {
            return "Location not specified";
        }
        return String.format("Lat: %.4f, Lng: %.4f", lat, lng);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Boolean getIsExpired() { return isExpired; }
    public void setIsExpired(Boolean isExpired) { this.isExpired = isExpired; }

    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }

    public UserDTO getDonor() { return donor; }
    public void setDonor(UserDTO donor) { this.donor = donor; }

    public ClaimDTO getClaim() { return claim; }
    public void setClaim(ClaimDTO claim) { this.claim = claim; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // New getters and setters for frontend compatibility
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    
    public String getFoodType() { return foodType; }
    public void setFoodType(String foodType) { this.foodType = foodType; }
    
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    
    public Long getDonorId() { return donorId; }
    public void setDonorId(Long donorId) { this.donorId = donorId; }
}
