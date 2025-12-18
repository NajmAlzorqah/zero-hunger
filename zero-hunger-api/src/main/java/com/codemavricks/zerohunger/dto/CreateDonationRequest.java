package com.codemavricks.zerohunger.dto;

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
 * <h3>3. OPTIONAL FIELDS</h3>
 * <ul>
 *   <li>expiresAt: Can be null (donation doesn't expire)</li>
 *   <li>description: Optional, title required</li>
 *   <li>Validation in service layer checks required fields</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Concept</b>: Type-safe request handling</li>
 *   <li><b>Concept</b>: JSON-B date/time conversion</li>
 *   <li><b>Potential</b>: Use @Embeddable for Location object</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
public class CreateDonationRequest {
    private String title;
    private String description;
    private Double quantityKg;
    private Double latitude;
    private Double longitude;
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
