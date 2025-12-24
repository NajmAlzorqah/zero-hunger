package com.codemavricks.zerohunger.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Create Donation Request DTO - Donation Creation Data.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. DATETIME DESERIALIZATION</h3>
 * <ul>
 *   <li>expiresAt: LocalDateTime field</li>
 *   <li>JSON-B automatically converts ISO-8601 string to LocalDateTime</li>
 *   <li>Example JSON: "expiresAt": "2025-12-20T18:00:00"</li>
 *   <li>Java 8+ Date/Time API integration</li>
 * </ul>
 * 
 * <h3>2. GEOLOCATION DATA</h3>
 * <ul>
 *   <li>latitude, longitude: Capture donation location</li>
 *   <li>Used by GeoService for nearby search</li>
 *   <li>Alternative: Use custom Location embedded object</li>
 * </ul>
 * 
 * <h3>3. BEAN VALIDATION FOR BUSINESS RULES</h3>
 * <ul>
 *   <li><b>@NotBlank</b>: Title is mandatory</li>
 *   <li><b>@Size</b>: Title length constraints for database schema</li>
 *   <li><b>@NotNull</b>: Quantity is required (can be 0.0 but not null)</li>
 *   <li><b>@Positive</b>: Quantity must be > 0 (business rule)</li>
 *   <li><b>@DecimalMin/@DecimalMax</b>: Latitude/Longitude range validation</li>
 *   <li><b>@Future</b>: Expiry date must be in the future</li>
 * </ul>
 * 
 * <h3>4. VALIDATION GROUPS (OPTIONAL)</h3>
 * <ul>
 *   <li>Can define validation groups for different scenarios</li>
 *   <li>Example: CreateValidation.class, UpdateValidation.class</li>
 *   <li>Enable different rules for creation vs update</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Concept</b>: Type-safe request handling with validation</li>
 *   <li><b>Concept</b>: JSON-B date/time conversion</li>
 *   <li><b>Concept</b>: Business rule validation with Bean Validation</li>
 *   <li><b>Potential</b>: Use @Embeddable for Location object</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
public class CreateDonationRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    @DecimalMax(value = "10000.0", message = "Quantity cannot exceed 10000 kg")
    private Double quantityKg;
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
    
    @Future(message = "Expiry date must be in the future")
    private LocalDateTime expiresAt;

    public CreateDonationRequest() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getQuantityKg() { return quantityKg; }
    public void setQuantityKg(Double quantityKg) { this.quantityKg = quantityKg; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
