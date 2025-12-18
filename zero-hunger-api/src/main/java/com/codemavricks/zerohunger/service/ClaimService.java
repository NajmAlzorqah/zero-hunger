package com.codemavricks.zerohunger.service;

import com.codemavricks.zerohunger.model.Claim;
import com.codemavricks.zerohunger.model.Donation;
import com.codemavricks.zerohunger.model.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.security.SecureRandom;

/**
 * ClaimService - Business Logic for Donation Claims.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. CONCURRENCY CONTROL - PESSIMISTIC LOCKING</h3>
 * <ul>
 *   <li><b>@LockModeType.PESSIMISTIC_WRITE</b>: Database row-level lock (SELECT FOR UPDATE)</li>
 *   <li><b>Problem Solved</b>: Prevents race condition when 2 volunteers claim same donation</li>
 *   <li><b>How it Works</b>:</li>
 *   <ul>
 *     <li>Thread 1: em.find(Donation, id, PESSIMISTIC_WRITE) → acquires lock</li>
 *     <li>Thread 2: tries same → WAITS until Thread 1 commits/rollbacks</li>
 *     <li>Thread 1: commits → releases lock</li>
 *     <li>Thread 2: gets donation, sees status='reserved' → throws exception</li>
 *   </ul>
 *   <li><b>Alternative</b>: OPTIMISTIC locking (@Version field) - allows conflicts, detects at commit</li>
 * </ul>
 * 
 * <h3>2. TRANSACTION ISOLATION</h3>
 * <ul>
 *   <li>Pessimistic lock ensures SERIALIZABLE isolation for this operation</li>
 *   <li>Other transactions can't read/modify locked row until commit</li>
 *   <li>Prevents "double booking" in high-concurrency scenarios</li>
 * </ul>
 * 
 * <h3>3. CROSS-SERVICE INJECTION</h3>
 * <ul>
 *   <li><b>@Inject NotificationService</b>: One EJB calling another EJB</li>
 *   <li>Transaction propagates: claimDonation() and createNotification() in SAME transaction</li>
 *   <li>If notification fails, claim also rolls back (all-or-nothing)</li>
 * </ul>
 * 
 * <h3>4. ENTITY STATE TRANSITIONS</h3>
 * <ul>
 *   <li>donation.setStatus("reserved") → modifies Managed entity</li>
 *   <li>em.merge(donation) → synchronizes changes to DB</li>
 *   <li>em.persist(claim) → makes New entity Managed and inserts to DB</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Chapter</b>: Concurrency - Managing multi-threaded access to beans</li>
 *   <li><b>Concept</b>: Pessimistic vs Optimistic Locking strategies</li>
 *   <li><b>Chapter</b>: Transactions - ACID properties, isolation levels</li>
 *   <li><b>Concept</b>: Transaction Propagation across EJB calls</li>
 * </ul>
 * 
 * <h3>Real-World Scenario:</h3>
 * <pre>
 * Time | Thread A (Volunteer 1)          | Thread B (Volunteer 2)
 * -----|---------------------------------|--------------------------------
 * t1   | find(donation, LOCK) ✓          |
 * t2   | check: status=available ✓       | find(donation, LOCK) ⏳ WAITING
 * t3   | set status=reserved             |
 * t4   | merge(donation)                 |
 * t5   | persist(claim)                  |
 * t6   | COMMIT ✓                        |
 * t7   |                                 | ✓ Lock released, reads donation
 * t8   |                                 | check: status=reserved ❌ FAIL
 * t9   |                                 | throws Exception
 * </pre>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
@Stateless
public class ClaimService {

    @PersistenceContext(unitName = "ZeroHungerPU")
    private EntityManager em;

    @jakarta.inject.Inject
    private NotificationService notificationService;

    public Claim claimDonation(Long donationId, User volunteer) throws Exception {
        Donation donation = em.find(Donation.class, donationId, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        
        if (donation == null) {
             throw new Exception("Donation not found");
        }
        
        // Race condition check (Optimistic locking via JPA versioning or explicit check)
        // Here we rely on Transaction Isolation, but explicitly checking status
        if (!"available".equals(donation.getStatus())) {
            throw new Exception("Donation is no longer available");
        }

        // Lock donation for update implies we update it now
        donation.setStatus("reserved");
        
        // Generate Pickup Code
        String pickupCode = String.format("%06d", new SecureRandom().nextInt(999999));
        donation.setPickupCode(pickupCode);
        
        em.merge(donation);

        Claim claim = new Claim();
        claim.setDonation(donation);
        claim.setVolunteer(volunteer);
        claim.setStatus("active");
        em.persist(claim);

        // Notify Donor
        notificationService.createNotification(donation.getDonor(), 
            "App\\Notifications\\DonationClaimed", 
            String.format("{\"donation_id\": %d, \"donation_title\": \"%s\", \"volunteer_name\": \"%s\", \"pickup_code\": \"%s\", \"message\": \"Your donation has been claimed by %s\"}", 
                donation.getId(), donation.getTitle(), volunteer.getName(), pickupCode, volunteer.getName()));

        return claim;
    }
    
    public List<Claim> getMyClaims(User volunteer) {
        return em.createQuery("SELECT c FROM Claim c WHERE c.volunteer = :volunteer ORDER BY c.createdAt DESC", Claim.class)
                .setParameter("volunteer", volunteer)
                .getResultList();
    }

    public Claim markPickedUp(Long claimId, String code) throws Exception {
        Claim claim = em.find(Claim.class, claimId);
        if (claim == null) throw new Exception("Claim not found");
        
        if (!claim.getDonation().getPickupCode().equals(code)) {
            throw new Exception("Invalid pickup code");
        }

        claim.setStatus("picked_up");
        claim.setPickedUpAt(LocalDateTime.now());
        claim.getDonation().setStatus("picked_up");
        
        em.merge(claim);
        em.merge(claim.getDonation());
        
        return claim;
    }

    public Claim markDelivered(Long claimId, String notes) throws Exception {
        Claim claim = em.find(Claim.class, claimId);
        if (claim == null) throw new Exception("Claim not found");

        if (!"picked_up".equals(claim.getStatus())) {
            throw new Exception("Donation must be picked up first");
        }

        claim.setStatus("delivered");
        claim.setDeliveredAt(LocalDateTime.now());
        claim.setNotes(notes);
        claim.getDonation().setStatus("delivered");
        
        // Impact Score Logic
        User volunteer = claim.getVolunteer();
        User donor = claim.getDonation().getDonor();
        
        // Volunteer get 2x points
        int volPoints = (int) (claim.getDonation().getQuantityKg() * 2);
        volunteer.setImpactScore(volunteer.getImpactScore() + volPoints);
        
        // Donor gets 1x points
        int donorPoints = claim.getDonation().getQuantityKg().intValue();
        donor.setImpactScore(donor.getImpactScore() + donorPoints);

        em.merge(claim);
        em.merge(claim.getDonation());
        em.merge(volunteer);
        em.merge(donor);

        // Notify Donor
        notificationService.createNotification(donor, 
            "App\\Notifications\\DonationDelivered", 
            String.format("{\"donation_id\": %d, \"donation_title\": \"%s\", \"message\": \"Your donation has been delivered! Thank you for fighting hunger.\"}", 
                claim.getDonation().getId(), claim.getDonation().getTitle()));

        return claim;
    }
    
    public void cancelClaim(Long claimId, User volunteer) throws Exception {
        Claim claim = em.find(Claim.class, claimId);
        if (claim == null) throw new Exception("Claim not found");
        
        if (!claim.getVolunteer().getId().equals(volunteer.getId())) {
             throw new Exception("Unauthorized");
        }
        
        Donation donation = claim.getDonation();
        donation.setStatus("available");
        donation.setPickupCode(null);
        
        em.remove(claim);
        em.merge(donation);
    }
}
