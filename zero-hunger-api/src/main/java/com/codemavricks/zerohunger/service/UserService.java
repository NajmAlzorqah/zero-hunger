package com.codemavricks.zerohunger.service;

import com.codemavricks.zerohunger.model.Token;
import com.codemavricks.zerohunger.model.User;
import com.codemavricks.zerohunger.util.PasswordUtils;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * UserService - Business Logic Component for User Management.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. STATELESS SESSION BEAN (@Stateless)</h3>
 * <ul>
 *   <li><b>What it is</b>: An EJB (Enterprise JavaBean) that handles business logic</li>
 *   <li><b>Stateless</b>: Does NOT remember conversation state between method calls</li>
 *   <li><b>Container-Managed</b>: Jakarta EE container creates/destroys instances automatically</li>
 *   <li><b>Pooling</b>: Container maintains a pool of instances for performance</li>
 *   <li><b>Thread-Safe</b>: Each client request gets a different instance from pool</li>
 *   <li><b>Use Case</b>: Perfect for operations like login, register, update profile</li>
 * </ul>
 * 
 * <h3>2. DEPENDENCY INJECTION (@PersistenceContext)</h3>
 * <ul>
 *   <li><b>@PersistenceContext</b>: Container automatically injects EntityManager</li>
 *   <li><b>No Manual Creation</b>: Don't need to call EntityManagerFactory.create()</li>
 *   <li><b>Managed Lifecycle</b>: Container handles opening/closing connections</li>
 *   <li><b>Thread-Safe</b>: Each thread gets correct EntityManager instance</li>
 *   <li>unitName="ZeroHungerPU" links to persistence.xml configuration</li>
 * </ul>
 * 
 * <h3>3. CONTAINER-MANAGED TRANSACTIONS (CMT)</h3>
 * <ul>
 *   <li><b>Automatic Transactions</b>: Each method runs in a transaction by default</li>
 *   <li><b>@TransactionAttribute(REQUIRED)</b>: Default - joins existing or creates new transaction</li>
 *   <li><b>Rollback on Exception</b>: If RuntimeException thrown, transaction auto-rolls back</li>
 *   <li><b>ACID Properties</b>: Container ensures Atomicity, Consistency, Isolation, Durability</li>
 *   <li><b>No Manual Commit</b>: Don't need em.getTransaction().commit()</li>
 * </ul>
 * 
 * <h3>4. JPQL (Java Persistence Query Language)</h3>
 * <ul>
 *   <li><b>Object-Oriented SQL</b>: Queries on Java objects, not database tables</li>
 *   <li>"SELECT u FROM User u" - queries User entities, not "users" table</li>
 *   <li><b>Type-Safe</b>: Returns User objects, not raw ResultSet</li>
 *   <li><b>Database Independent</b>: Same JPQL works on MySQL, PostgreSQL, Oracle</li>
 * </ul>
 * 
 * <h3>5. ENTITY MANAGER OPERATIONS</h3>
 * <ul>
 *   <li><b>em.persist(user)</b>: INSERT - makes entity Managed and saves to DB</li>
 *   <li><b>em.find(User.class, id)</b>: SELECT by primary key</li>
 *   <li><b>em.merge(user)</b>: UPDATE - synchronizes detached entity with DB</li>
 *   <li><b>em.createQuery()</b>: Execute JPQL queries</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Chapter</b>: Business Components (EJB - Enterprise JavaBeans)</li>
 *   <li><b>Section</b>: Stateless Session Beans - operations without conversation state</li>
 *   <li><b>Chapter</b>: Transactions - Container-Managed vs Bean-Managed</li>
 *   <li><b>Chapter</b>: Dependency Injection (CDI) - @Inject and @PersistenceContext</li>
 *   <li><b>Chapter</b>: Querying - JPQL queries on objects</li>
 * </ul>
 * 
 * <h3>Why Stateless Bean Here?</h3>
 * <ul>
 *   <li>login() doesn't need to remember previous login attempts</li>
 *   <li>register() is a one-time operation</li>
 *   <li>updateProfile() is independent per call</li>
 *   <li>Better performance with instance pooling</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 * @see jakarta.ejb.Stateless
 * @see jakarta.persistence.EntityManager
 */
@Stateless
public class UserService {

    @PersistenceContext(unitName = "ZeroHungerPU")
    private EntityManager em;

    public User register(User user) {
        user.setPassword(PasswordUtils.hashPassword(user.getPassword()));
        em.persist(user);
        return user;
    }

    public Optional<User> findByEmail(String email) {
        try {
            return Optional.of(em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<String> login(String email, String password) {
        Optional<User> userOpt = findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (PasswordUtils.checkPassword(password, user.getPassword())) {
                // Generate Token
                String tokenStr = PasswordUtils.generateToken();
                Token token = new Token();
                token.setToken(tokenStr);
                token.setUser(user);
                token.setLastUsedAt(LocalDateTime.now());
                em.persist(token);
                return Optional.of(tokenStr);
            }
        }
        return Optional.empty();
    }
    
    public Optional<User> findByToken(String tokenStr) {
        try {
            // First get the token with its user
            TypedQuery<Token> query = em.createQuery("SELECT t FROM Token t JOIN FETCH t.user WHERE t.token = :token", Token.class);
            query.setParameter("token", tokenStr);
            Token token = query.getSingleResult();
            
            // Update last used
            token.setLastUsedAt(LocalDateTime.now());
            em.merge(token);
            
            // Re-fetch the user with roles to ensure roles collection is loaded
            // This is necessary because @ElementCollection may not be eagerly loaded via JOIN FETCH on parent entity
            User user = token.getUser();
            TypedQuery<User> userQuery = em.createQuery(
                "SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :userId", User.class);
            userQuery.setParameter("userId", user.getId());
            User userWithRoles = userQuery.getSingleResult();
            
            return Optional.of(userWithRoles);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public User updateProfile(Long userId, String name, String phone, Double lat, Double lng) {
        User user = em.find(User.class, userId);
        if (user != null) {
            if (name != null) user.setName(name);
            if (phone != null) user.setPhone(phone);
            if (lat != null) user.setLatitude(lat);
            if (lng != null) user.setLongitude(lng);
            em.merge(user);
        }
        return user;
    }
}
