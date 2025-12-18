package com.codemavricks.zerohunger.service;

import jakarta.ejb.Stateless;

/**
 * GeoService - Geolocation Calculation Service.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. STATELESS SESSION BEAN FOR UTILITY OPERATIONS</h3>
 * <ul>
 *   <li><b>Pure Functions</b>: calculateDistance() has no side effects, perfect for stateless bean</li>
 *   <li><b>Reusability</b>: Injected into DonationService for nearby donation search</li>
 *   <li><b>Thread-Safe</b>: Mathematical calculations are inherently thread-safe</li>
 *   <li><b>Pooling Benefits</b>: Container can pool instances for concurrent requests</li>
 * </ul>
 * 
 * <h3>2. SERVICE LAYER SEPARATION</h3>
 * <ul>
 *   <li>Business logic (geo calculations) separated from data access (DonationService)</li>
 *   <li>Single Responsibility Principle: Only handles distance calculations</li>
 *   <li>Testable: Can be unit tested independently</li>
 * </ul>
 * 
 * <h3>3. HAVERSINE FORMULA IMPLEMENTATION</h3>
 * <ul>
 *   <li>Calculates great-circle distance between two points on Earth</li>
 *   <li>Accounts for Earth's spherical shape (radius = 6371 km)</li>
 *   <li>Returns distance in kilometers</li>
 *   <li><b>Use Case</b>: Find donations within 10km radius of volunteer</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Concept</b>: Stateless Session Beans for stateless operations</li>
 *   <li><b>Concept</b>: Service Layer Pattern in Business Tier</li>
 *   <li><b>Concept</b>: EJB Dependency Injection (@Inject in DonationService)</li>
 * </ul>
 * 
 * <h3>Example Usage:</h3>
 * <pre>
 * // In DonationService:
 * &#64;Inject
 * private GeoService geoService;
 * 
 * public List&lt;Donation&gt; findNearby(Double userLat, Double userLng, Double radiusKm) {
 *     for (Donation d : allDonations) {
 *         double distance = geoService.calculateDistance(userLat, userLng, 
 *                                                        d.getLatitude(), d.getLongitude());
 *         if (distance <= radiusKm) {
 *             nearby.add(d);
 *         }
 *     }
 * }
 * </pre>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
@Stateless
public class GeoService {

    private static final int EARTH_RADIUS_KM = 6371;

    public double calculateDistance(double startLat, double startLong, double endLat, double endLong) {
        double dLat = Math.toRadians(endLat - startLat);
        double dLong = Math.toRadians(endLong - startLong);

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    private double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }
}
