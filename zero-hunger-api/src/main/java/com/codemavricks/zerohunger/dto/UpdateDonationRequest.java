package com.codemavricks.zerohunger.dto;

import java.time.LocalDateTime;

/**
 * Update Donation Request DTO - Partial Update Data.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. PARTIAL UPDATE DTO</h3>
 * <ul>
 *   <li>All fields are optional (can be null)</li>
 *   <li>Client sends only fields to update: {"title": "New Title"}</li>
 *   <li>Service layer updates only non-null fields</li>
 *   <li>Implements HTTP PATCH semantics with PUT endpoint</li>
 * </ul>
 * 
 * <h3>2. NULL HANDLING PATTERN</h3>
 * <ul>
 *   <li>Null value = don't update this field</li>
 *   <li>DonationService.updateDonation() checks: if (updates.containsKey("title"))</li>
 *   <li>Alternative: Use Optional&lt;String&gt; fields</li>
 * </ul>
 * 
 * <h3>3. IMMUTABLE FIELDS (NOT INCLUDED)</h3>
 * <ul>
 *   <li>Missing: donor, status, createdAt</li>
 *   <li>These shouldn't be user-modifiable</li>
 *   <li>Only title, description, quantity, expires can change</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Pattern</b>: Partial update DTOs for PATCH-like operations</li>
 *   <li><b>Concept</b>: Data integrity through selective updates</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
public class UpdateDonationRequest {
    private String title;
    private String description;
    private Double quantityKg;
    private LocalDateTime expiresAt;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getQuantityKg() {
        return quantityKg;
    }

    public void setQuantityKg(Double quantityKg) {
        this.quantityKg = quantityKg;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
