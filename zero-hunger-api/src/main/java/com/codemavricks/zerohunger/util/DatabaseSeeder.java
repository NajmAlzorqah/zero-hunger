package com.codemavricks.zerohunger.util;

import com.codemavricks.zerohunger.model.Donation;
import com.codemavricks.zerohunger.model.User;
import com.codemavricks.zerohunger.service.DonationService;
import com.codemavricks.zerohunger.service.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * DatabaseSeeder - Automatic Test Data Population.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. SINGLETON SESSION BEAN (@Singleton)</h3>
 * <ul>
 *   <li><b>@Singleton</b>: Only ONE instance exists in entire application</li>
 *   <li><b>Shared State</b>: All requests access same instance (vs @Stateless pooling)</li>
 *   <li><b>Concurrency</b>: Default = container-managed locking (@Lock(WRITE))</li>
 *   <li><b>Use Case</b>: Application-wide initialization, caching, counters</li>
 *   <li><b>Book Concept</b>: Singleton Session Beans (Chapter on EJB types)</li>
 * </ul>
 * 
 * <h3>2. AUTOMATIC STARTUP (@Startup)</h3>
 * <ul>
 *   <li><b>@Startup</b>: Bean is instantiated when application deploys</li>
 *   <li>Without @Startup: Lazy initialization on first use</li>
 *   <li>With @Startup: Eager initialization at deployment time</li>
 *   <li>Perfect for: Database seeding, cache warming, system checks</li>
 * </ul>
 * 
 * <h3>3. LIFECYCLE CALLBACK (@PostConstruct)</h3>
 * <ul>
 *   <li><b>@PostConstruct</b>: Method called AFTER bean is created and dependencies injected</li>
 *   <li><b>Execution Order</b>:</li>
 *   <ul>
 *     <li>1. Container creates DatabaseSeeder instance</li>
 *     <li>2. Container injects UserService, DonationService (@Inject)</li>
 *     <li>3. Container calls seedDatabase() (@PostConstruct)</li>
 *   </ul>
 *   <li>Alternative callbacks: @PreDestroy (before shutdown)</li>
 * </ul>
 * 
 * <h3>4. DEPENDENCY INJECTION IN LIFECYCLE</h3>
 * <ul>
 *   <li>@Inject services are available in @PostConstruct method</li>
 *   <li>Can call userService.register(), donationService.create()</li>
 *   <li>Demonstrates cross-bean collaboration at startup</li>
 * </ul>
 * 
 * <h3>5. IDEMPOTENT SEEDING</h3>
 * <ul>
 *   <li>createUserIfNotExists() checks if user already exists</li>
 *   <li>Safe to run multiple times (redeploy doesn't duplicate data)</li>
 *   <li>Production pattern: Database migrations should be idempotent</li>
 * </ul>
 * 
 * <h3>6. TRANSACTION CONTEXT</h3>
 * <ul>
 *   <li>@PostConstruct method runs in transaction (Singleton default = CMT)</li>
 *   <li>Calls to UserService.register() join same transaction</li>
 *   <li>If seeding fails midway, ALL changes rollback</li>
 *   <li>Ensures data consistency during initialization</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Chapter</b>: Singleton Session Beans - single shared instance</li>
 *   <li><b>Concept</b>: EJB Lifecycle callbacks (@PostConstruct, @PreDestroy)</li>
 *   <li><b>Concept</b>: Application initialization patterns</li>
 *   <li><b>Concept</b>: Dependency injection timing and availability</li>
 * </ul>
 * 
 * <h3>Singleton vs Stateless Comparison:</h3>
 * <pre>
 * ┌─────────────────┬──────────────────┬─────────────────┐
 * │                 │ @Singleton       │ @Stateless      │
 * ├─────────────────┼──────────────────┼─────────────────┤
 * │ Instances       │ 1 per app        │ Pool of N       │
 * │ Shared State    │ Yes (be careful) │ No              │
 * │ Concurrency     │ Locked by default│ Concurrent safe │
 * │ @Startup        │ Can use          │ Cannot use      │
 * │ Use Case        │ Init, caching    │ Business logic  │
 * └─────────────────┴──────────────────┴─────────────────┘
 * </pre>
 * 
 * <h3>Startup Sequence:</h3>
 * <pre>
 * 1. Application deployment starts
 * 2. Container scans for @Singleton @Startup beans
 * 3. DatabaseSeeder instance created
 * 4. Container injects UserService, DonationService
 * 5. Container calls seedDatabase() @PostConstruct
 * 6. Method checks if test users exist
 * 7. If not exists, creates users via UserService
 * 8. Creates sample donations via DonationService
 * 9. Transaction commits
 * 10. Application ready to serve requests
 * </pre>
 * 
 * <h3>Potential Enhancement - EJB Timer:</h3>
 * <pre>
 * // Add scheduled re-seeding:
 * &#64;Schedule(hour="2", minute="0") // Every day at 2 AM
 * public void resetTestData() {
 *     // Clear and re-seed database
 * }
 * </pre>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 * @see jakarta.ejb.Singleton
 * @see jakarta.ejb.Startup
 * @see jakarta.annotation.PostConstruct
 */
@Singleton
@Startup
public class DatabaseSeeder {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseSeeder.class.getName());
    
    @Inject
    private UserService userService;
    
    @Inject
    private DonationService donationService;
    
    @PostConstruct
    public void seedDatabase() {
        LOGGER.info("Starting database seeding...");
        
        // Seed users
        List<User> donors = seedUsers();
        
        // Seed donations
        if (!donors.isEmpty()) {
            seedDonations(donors);
        }
        
        LOGGER.info("Database seeding completed!");
    }
    
    private List<User> seedUsers() {
        List<User> donors = new ArrayList<>();
        
        // Create admin account
        createUserIfNotExists(
            "admin@test.com",
            "password",
            "Admin User",
            "+1234567890",
            30.0444,
            31.2357,
            "admin",
            1000
        );
        
        // Create donor accounts
        User donor1 = createUserIfNotExists(
            "donor@test.com",
            "password",
            "John Donor",
            "+1234567891",
            30.0444,
            31.2357,
            "donor",
            150
        );
        if (donor1 != null) donors.add(donor1);
        
        User donor2 = createUserIfNotExists(
            "donor2@test.com",
            "password",
            "Ahmed Restaurant",
            "+1234567894",
            30.0600,
            31.2500,
            "donor",
            500
        );
        if (donor2 != null) donors.add(donor2);
        
        // Create volunteer accounts
        createUserIfNotExists(
            "volunteer@test.com",
            "password",
            "Jane Volunteer",
            "+1234567892",
            30.0500,
            31.2400,
            "volunteer",
            350
        );
        
        createUserIfNotExists(
            "volunteer2@test.com",
            "password",
            "Mike Driver",
            "+1234567895",
            30.0300,
            31.2200,
            "volunteer",
            720
        );
        
        // Create recipient account
        createUserIfNotExists(
            "recipient@test.com",
            "password",
            "Sara Recipient",
            "+1234567893",
            30.0400,
            31.2300,
            "recipient",
            50
        );
        
        return donors;
    }
    
    private void seedDonations(List<User> donors) {
        LOGGER.info("Seeding sample donations...");
        
        // Check if donations already exist
        List<Donation> existing = donationService.findAllAvailable();
        if (!existing.isEmpty()) {
            LOGGER.info("Donations already exist. Skipping donation seeding.");
            return;
        }
        
        User donor = donors.get(0);
        
        // Sample donation data
        Object[][] donationData = {
            {"Fresh Bread from Bakery", "20 loaves of fresh whole wheat bread, baked this morning", 5.0, 30.0444, 31.2357, 6},
            {"Restaurant Surplus Meals", "Cooked rice and vegetables, properly packaged", 15.5, 30.0500, 31.2400, 4},
            {"Fresh Fruits and Vegetables", "Apples, oranges, tomatoes, and cucumbers", 12.0, 30.0600, 31.2500, 48},
            {"Canned Goods", "Assorted canned beans, soup, and vegetables", 8.0, 30.0400, 31.2300, null},
            {"Dairy Products", "Milk, cheese, and yogurt from supermarket", 6.5, 30.0550, 31.2450, 12},
            {"Pastries and Desserts", "Cakes and pastries from bakery closing", 4.0, 30.0350, 31.2250, 8},
            {"Packaged Snacks", "Chips, crackers, and cookies - all sealed", 10.0, 30.0650, 31.2550, 720},
            {"Prepared Sandwiches", "Fresh sandwiches from cafe", 3.5, 30.0250, 31.2150, 3}
        };
        
        for (Object[] data : donationData) {
            Donation donation = new Donation();
            donation.setTitle((String) data[0]);
            donation.setDescription((String) data[1]);
            donation.setQuantityKg((Double) data[2]);
            donation.setLatitude((Double) data[3]);
            donation.setLongitude((Double) data[4]);
            
            if (data[5] != null) {
                donation.setExpiresAt(LocalDateTime.now().plusHours((Integer) data[5]));
            }
            
            // Alternate between donors
            User selectedDonor = donors.get((int) (Math.random() * donors.size()));
            donationService.createDonation(donation, selectedDonor);
        }
        
        LOGGER.info("Sample donations created successfully!");
    }
    
    private User createUserIfNotExists(String email, String password, String name, 
                                      String phone, Double latitude, Double longitude, 
                                      String role, Integer impactScore) {
        Optional<User> existingUser = userService.findByEmail(email);
        if (existingUser.isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setPassword(password);
            user.setName(name);
            user.setPhone(phone);
            user.setLatitude(latitude);
            user.setLongitude(longitude);
            user.addRole(role);
            user.setStatus("active");
            user.setImpactScore(impactScore);
            
            userService.register(user);
            LOGGER.info("Created " + role + " account: " + email);
            return user;
        } else {
            LOGGER.info(role + " account already exists: " + email);
            return existingUser.get();
        }
    }
}
