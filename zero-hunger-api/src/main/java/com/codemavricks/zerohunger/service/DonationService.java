package com.codemavricks.zerohunger.service;

import com.codemavricks.zerohunger.model.Donation;
import com.codemavricks.zerohunger.model.User;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

/**
 * DonationService - Business Logic for Donation Management.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. EJB INJECTION (@Inject)</h3>
 * <ul>
 *   <li><b>@Inject</b>: CDI (Contexts and Dependency Injection) - injects GeoService</li>
 *   <li><b>Loose Coupling</b>: DonationService doesn't create GeoService instance</li>
 *   <li><b>Container-Managed</b>: Jakarta EE container handles object creation</li>
 *   <li><b>Service Collaboration</b>: Session Beans can inject other Session Beans</li>
 *   <li>Compare to manual: GeoService geo = new GeoService() - BAD practice in EJB</li>
 * </ul>
 * 
 * <h3>2. JPQL QUERIES WITH PARAMETERS</h3>
 * <ul>
 *   <li><b>Named Parameters</b>: :donor instead of ? (positional)</li>
 *   <li><b>SQL Injection Safe</b>: Parameters automatically escaped</li>
 *   <li><b>WHERE Clauses</b>: d.status = 'available' AND d.expiresAt > CURRENT_TIMESTAMP</li>
 *   <li><b>ORDER BY</b>: ORDER BY d.createdAt DESC</li>
 *   <li><b>Type-Safe Results</b>: .getResultList() returns List&lt;Donation&gt;</li>
 * </ul>
 * 
 * <h3>3. TRANSACTION MANAGEMENT</h3>
 * <ul>
 *   <li>All methods run in Container-Managed Transactions (CMT)</li>
 *   <li>createDonation(): persist() requires active transaction</li>
 *   <li>updateDonation(): merge() requires transaction for DB sync</li>
 *   <li>If exception thrown, all changes rollback automatically</li>
 * </ul>
 * 
 * <h3>4. BUSINESS TIER PATTERN</h3>
 * <ul>
 *   <li>Service layer separates business logic from web layer (REST resources)</li>
 *   <li>Reusable methods: findNearby() can be called from different endpoints</li>
 *   <li>Encapsulates complex operations: findNearby() with distance calculation</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Concept</b>: Dependency Injection (CDI) - managing object dependencies</li>
 *   <li><b>Concept</b>: JPQL - querying with Java objects</li>
 *   <li><b>Concept</b>: N-Tier Architecture - Business Tier separation</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
@Stateless
public class DonationService {

    @PersistenceContext(unitName = "ZeroHungerPU")
    private EntityManager em;

    @Inject
    private GeoService geoService;

    public Donation createDonation(Donation donation, User donor) {
        donation.setDonor(donor);
        donation.setStatus("available");
        donation.setCreatedAt(LocalDateTime.now());
        donation.setUpdatedAt(LocalDateTime.now());
        em.persist(donation);
        return donation;
    }

    public Donation findById(Long id) {
        return em.find(Donation.class, id);
    }

    public List<Donation> findAllAvailable() {
        return em.createQuery("SELECT d FROM Donation d WHERE d.status = 'available' AND (d.expiresAt IS NULL OR d.expiresAt > CURRENT_TIMESTAMP)", Donation.class)
                .getResultList();
    }
    
    public List<Donation> findMyDonations(User donor) {
        return em.createQuery("SELECT d FROM Donation d WHERE d.donor = :donor ORDER BY d.createdAt DESC", Donation.class)
                .setParameter("donor", donor)
                .getResultList();
    }

    public List<Donation> findNearby(Double lat, Double lng, Double radiusKm) {
        // Fetch all available donations (Naïve approach for demonstration, optimization would use spatial DB query)
        List<Donation> allAvailable = findAllAvailable();
        List<Donation> nearby = new ArrayList<>();

        for (Donation d : allAvailable) {
            double distance = geoService.calculateDistance(lat, lng, d.getLatitude(), d.getLongitude());
            if (distance <= radiusKm) {
                // Determine if we can inject distance into the object (transient field) or wrapper
                // For now, simpler to just return the list, or we could sort by distance
                nearby.add(d); 
            }
        }
        
        // Sort by distance (closest first)
        nearby.sort((d1, d2) -> {
             double dist1 = geoService.calculateDistance(lat, lng, d1.getLatitude(), d1.getLongitude());
             double dist2 = geoService.calculateDistance(lat, lng, d2.getLatitude(), d2.getLongitude());
             return Double.compare(dist1, dist2);
        });

        return nearby;
    }
    
    public Donation updateDonation(Long id, Map<String, Object> updates) {
        Donation donation = em.find(Donation.class, id);
        if (donation == null) {
            throw new IllegalArgumentException("Donation not found");
        }
        
        if (updates.containsKey("title")) {
            donation.setTitle((String) updates.get("title"));
        }
        if (updates.containsKey("description")) {
            donation.setDescription((String) updates.get("description"));
        }
        if (updates.containsKey("quantity_kg")) {
            Object qtyObj = updates.get("quantity_kg");
            Double qty = qtyObj instanceof Number ? ((Number) qtyObj).doubleValue() : null;
            if (qty != null) donation.setQuantityKg(qty);
        }
        if (updates.containsKey("expires_at")) {
            // Handle datetime conversion if needed
            // For simplicity, assuming it's already LocalDateTime or null
            donation.setExpiresAt((java.time.LocalDateTime) updates.get("expires_at"));
        }
        
        donation.setUpdatedAt(LocalDateTime.now());
        return em.merge(donation);
    }
    
    public Donation updateDonation(Donation donation) {
        donation.setUpdatedAt(LocalDateTime.now());
        return em.merge(donation);
    }
    
    public void deleteDonation(Long id) {
        Donation donation = em.find(Donation.class, id);
        if (donation != null) {
            em.remove(donation);
        }
    }
}
