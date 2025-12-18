package com.codemavricks.zerohunger.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.security.SecureRandom;

/**
 * PasswordUtils - Security Utility for Password Hashing and Token Generation.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. UTILITY CLASS PATTERN</h3>
 * <ul>
 *   <li><b>Static Methods</b>: No instance needed, called directly</li>
 *   <li><b>Stateless</b>: Pure functions, no internal state</li>
 *   <li><b>Reusable</b>: Used by UserService across application</li>
 *   <li><b>Not an EJB</b>: Simple utility, doesn't need container management</li>
 * </ul>
 * 
 * <h3>2. PASSWORD HASHING</h3>
 * <ul>
 *   <li><b>Algorithm</b>: SHA-256 (demonstration purposes)</li>
 *   <li><b>Why Hash</b>: Never store passwords in plain text</li>
 *   <li><b>Process</b>:</li>
 *   <ul>
 *     <li>User registers: password "mypass123" → hash "a3f2e1..."</li>
 *     <li>Stored in database: hashed value</li>
 *     <li>User logs in: hash input password, compare with stored hash</li>
 *     <li>If match → correct password</li>
 *   </ul>
 *   <li><b>SECURITY NOTE</b>: SHA-256 alone is NOT production-ready</li>
 * </ul>
 * 
 * <h3>3. PRODUCTION-READY ENHANCEMENT</h3>
 * <ul>
 *   <li><b>Problem with SHA-256</b>:</li>
 *   <ul>
 *     <li>No salt → rainbow table attacks possible</li>
 *     <li>Too fast → brute force attacks feasible</li>
 *   </ul>
 *   <li><b>Use BCrypt Instead</b>:</li>
 *   <pre>
 *   import org.mindrot.jbcrypt.BCrypt;
 *   
 *   public static String hashPassword(String password) {
 *       return BCrypt.hashpw(password, BCrypt.gensalt(12));
 *   }
 *   
 *   public static boolean checkPassword(String raw, String hashed) {
 *       return BCrypt.checkpw(raw, hashed);
 *   }
 *   </pre>
 *   <li>BCrypt includes automatic salting and configurable cost factor</li>
 * </ul>
 * 
 * <h3>4. TOKEN GENERATION</h3>
 * <ul>
 *   <li><b>SecureRandom</b>: Cryptographically secure random number generator</li>
 *   <li>Generates 24 random bytes</li>
 *   <li>Base64 URL-safe encoding (no +, /, = characters)</li>
 *   <li>Result: 32-character token string</li>
 *   <li><b>Use Case</b>: Bearer tokens for API authentication</li>
 * </ul>
 * 
 * <h3>5. ALTERNATIVE: JWT (NOT IMPLEMENTED)</h3>
 * <ul>
 *   <li><b>Current</b>: Random token stored in database</li>
 *   <li><b>JWT Approach</b>: Self-contained token with claims</li>
 *   <pre>
 *   String jwt = Jwts.builder()
 *       .setSubject(user.getEmail())
 *       .claim("userId", user.getId())
 *       .claim("roles", user.getRoles())
 *       .setExpiration(Date.from(Instant.now().plus(7, ChronoUnit.DAYS)))
 *       .signWith(secretKey)
 *       .compact();
 *   </pre>
 *   <li>Benefit: No database lookup on every request</li>
 *   <li>Trade-off: Harder to revoke, larger token size</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Concept</b>: Security - Password protection</li>
 *   <li><b>Concept</b>: Stateless authentication with tokens</li>
 *   <li><b>Pattern</b>: Utility classes vs EJBs</li>
 *   <li><b>Missing</b>: Industry-standard password hashing (BCrypt, Argon2)</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
public class PasswordUtils {

    public static String hashPassword(String password) {
        // Simple SHA-256 for demonstration (Ideally use BCrypt/PBKDF2)
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public static boolean checkPassword(String raw, String hashed) {
        return hashPassword(raw).equals(hashed);
    }
    
    public static String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
