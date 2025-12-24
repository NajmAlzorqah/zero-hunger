package com.codemavricks.zerohunger.security;

import org.mindrot.jbcrypt.BCrypt;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.security.Principal;
import java.sql.*;
import java.util.*;

/**
 * Custom JAAS LoginModule for Database Authentication.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. JAAS LOGINMODULE (Java Authentication and Authorization Service)</h3>
 * <ul>
 *   <li><b>LoginModule Interface</b>: Core of JAAS pluggable authentication</li>
 *   <li><b>Two-Phase Commit</b>: login() + commit() pattern for transaction-like auth</li>
 *   <li><b>Subject</b>: Represents the authenticated user (container-managed)</li>
 *   <li><b>Principal</b>: User identity (username/email) stored in Subject</li>
 *   <li><b>Callback Handlers</b>: Secure way to request credentials from container</li>
 * </ul>
 * 
 * <h3>2. LOGINMODULE LIFECYCLE</h3>
 * <ul>
 *   <li><b>initialize()</b>: Called once when LoginModule is created</li>
 *   <li><b>login()</b>: Performs authentication, returns true/false</li>
 *   <li><b>commit()</b>: Called after successful login, adds Principals to Subject</li>
 *   <li><b>abort()</b>: Called if login fails, cleanup temporary credentials</li>
 *   <li><b>logout()</b>: Removes Principals from Subject</li>
 * </ul>
 * 
 * <h3>3. CALLBACK HANDLERS</h3>
 * <ul>
 *   <li><b>NameCallback</b>: Requests username from container</li>
 *   <li><b>PasswordCallback</b>: Requests password securely</li>
 *   <li><b>CallbackHandler</b>: Container provides callbacks (form, basic auth, etc.)</li>
 *   <li>Decouples authentication from credential collection method</li>
 * </ul>
 * 
 * <h3>4. PRINCIPAL AND ROLE MANAGEMENT</h3>
 * <ul>
 *   <li><b>UserPrincipal</b>: Represents user identity (email)</li>
 *   <li><b>RolePrincipal</b>: Represents user roles (donor, volunteer)</li>
 *   <li>Subject contains Set&lt;Principal&gt; for identity and roles</li>
 *   <li>Container uses these for @RolesAllowed and SecurityContext</li>
 * </ul>
 * 
 * <h3>5. DATABASE AUTHENTICATION</h3>
 * <ul>
 *   <li>Queries users table by email</li>
 *   <li>Uses BCrypt for password hashing verification</li>
 *   <li>Loads roles from user_roles table</li>
 *   <li>Thread-safe credential handling</li>
 * </ul>
 * 
 * <h3>6. CONFIGURATION (JAAS config file or server config)</h3>
 * <pre>
 * ZeroHungerRealm {
 *     com.codemavricks.zerohunger.security.DatabaseLoginModule required
 *     debug=true
 *     dsJndiName="jdbc/ZeroHungerDS";
 * };
 * </pre>
 * 
 * <h3>7. TWO-PHASE COMMIT PATTERN</h3>
 * <ul>
 *   <li><b>Phase 1 (login())</b>: Validate credentials, store temporarily</li>
 *   <li><b>Phase 2 (commit())</b>: Add Principals to Subject (make permanent)</li>
 *   <li>Why: Multiple LoginModules can participate, all must succeed</li>
 *   <li>If any fails: abort() called on all, no Principals added</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Chapter</b>: Security - JAAS Authentication Framework</li>
 *   <li><b>Concept</b>: LoginModule pluggable authentication</li>
 *   <li><b>Concept</b>: Subject, Principal, and Credential</li>
 *   <li><b>Concept</b>: Two-phase authentication commit</li>
 *   <li><b>Concept</b>: Callback Handlers for credential collection</li>
 * </ul>
 * 
 * <h3>Usage in Jakarta EE:</h3>
 * <pre>
 * // Configure in application server (GlassFish, WildFly)
 * // Then in resource methods:
 * 
 * &#64;Context
 * SecurityContext securityContext;
 * 
 * public Response getDonations() {
 *     Principal principal = securityContext.getUserPrincipal();
 *     String email = principal.getName(); // Email from login
 *     
 *     boolean isDonor = securityContext.isUserInRole("donor");
 *     // Use authenticated user...
 * }
 * </pre>
 * 
 * <h3>IMPORTANT NOTE:</h3>
 * <p>
 * This LoginModule is provided as a demonstration of JAAS concepts. It is NOT
 * currently integrated with the running application because:
 * </p>
 * <ul>
 *   <li>The app uses custom JWT token authentication (AuthFilter)</li>
 *   <li>JAAS form-based auth would conflict with REST API architecture</li>
 *   <li>Frontend expects Bearer token, not FORM/BASIC auth</li>
 *   <li>Requires significant server configuration to enable</li>
 * </ul>
 * <p>
 * To use this LoginModule, you would need to:
 * </p>
 * <ol>
 *   <li>Uncomment security constraints in web.xml</li>
 *   <li>Configure JAAS realm in application server</li>
 *   <li>Remove or disable AuthFilter.java</li>
 *   <li>Update frontend to use form-based authentication</li>
 * </ol>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 * @see javax.security.auth.spi.LoginModule
 * @see javax.security.auth.Subject
 * @see java.security.Principal
 */
public class DatabaseLoginModule implements LoginModule {

    // JAAS framework provided
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map<String, ?> sharedState;
    private Map<String, ?> options;

    // Authentication state
    private boolean loginSucceeded = false;
    private boolean commitSucceeded = false;
    
    // Temporary credentials (cleared after commit/abort)
    private String username;
    private char[] password;
    
    // Principals to add to Subject on commit
    private Principal userPrincipal;
    private Set<Principal> rolePrincipals = new HashSet<>();
    
    // Configuration options
    private String dsJndiName;
    private boolean debug = false;

    /**
     * Initialize the LoginModule.
     * 
     * <p><b>JAAS Concept: Initialization Phase</b></p>
     * <ul>
     *   <li>Called once when LoginModule is instantiated</li>
     *   <li>Receives Subject to populate, CallbackHandler for credentials</li>
     *   <li>sharedState: Share data between LoginModules</li>
     *   <li>options: Configuration from JAAS config file</li>
     * </ul>
     */
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;

        // Read configuration options
        this.dsJndiName = (String) options.get("dsJndiName");
        if (this.dsJndiName == null) {
            this.dsJndiName = "jdbc/ZeroHungerDS"; // Default
        }
        
        String debugStr = (String) options.get("debug");
        this.debug = "true".equalsIgnoreCase(debugStr);
        
        if (debug) {
            System.out.println("[DatabaseLoginModule] Initialized with datasource: " + dsJndiName);
        }
    }

    /**
     * Authenticate the user.
     * 
     * <p><b>JAAS Concept: Phase 1 - Authentication</b></p>
     * <ul>
     *   <li>Prompt for credentials using CallbackHandler</li>
     *   <li>Verify credentials against database</li>
     *   <li>Store credentials temporarily (not in Subject yet)</li>
     *   <li>Return true if authentication succeeds, false otherwise</li>
     * </ul>
     * 
     * <p><b>Why not add Principals here?</b> Multiple LoginModules might be
     * configured. All must succeed before any Principals are added.</p>
     */
    @Override
    public boolean login() throws LoginException {
        if (debug) {
            System.out.println("[DatabaseLoginModule] login() called");
        }

        // Step 1: Request credentials from container via callbacks
        NameCallback nameCallback = new NameCallback("Email: ");
        PasswordCallback passwordCallback = new PasswordCallback("Password: ", false);
        
        try {
            callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});
        } catch (IOException | UnsupportedCallbackException e) {
            throw new LoginException("Error retrieving credentials: " + e.getMessage());
        }

        // Step 2: Extract credentials
        username = nameCallback.getName();
        password = passwordCallback.getPassword();
        passwordCallback.clearPassword(); // Security best practice
        
        if (username == null || password == null || password.length == 0) {
            throw new LoginException("Username or password is null");
        }

        if (debug) {
            System.out.println("[DatabaseLoginModule] Authenticating user: " + username);
        }

        // Step 3: Validate against database
        try {
            if (authenticateUser(username, new String(password))) {
                loginSucceeded = true;
                if (debug) {
                    System.out.println("[DatabaseLoginModule] Authentication successful");
                }
                return true;
            } else {
                if (debug) {
                    System.out.println("[DatabaseLoginModule] Authentication failed");
                }
                throw new LoginException("Invalid credentials");
            }
        } catch (SQLException e) {
            throw new LoginException("Database error: " + e.getMessage());
        }
    }

    /**
     * Commit the authentication.
     * 
     * <p><b>JAAS Concept: Phase 2 - Commit</b></p>
     * <ul>
     *   <li>Called after ALL LoginModules' login() methods succeed</li>
     *   <li>Add Principals to Subject (make authentication permanent)</li>
     *   <li>Clear temporary credentials for security</li>
     *   <li>Return true to indicate commit succeeded</li>
     * </ul>
     */
    @Override
    public boolean commit() throws LoginException {
        if (!loginSucceeded) {
            return false;
        }

        if (debug) {
            System.out.println("[DatabaseLoginModule] commit() - Adding principals to Subject");
        }

        // Add user principal (identity)
        userPrincipal = new UserPrincipal(username);
        subject.getPrincipals().add(userPrincipal);

        // Add role principals (authorization)
        for (Principal rolePrincipal : rolePrincipals) {
            subject.getPrincipals().add(rolePrincipal);
        }

        // Clear sensitive data
        clearCredentials();
        
        commitSucceeded = true;
        return true;
    }

    /**
     * Abort the authentication.
     * 
     * <p><b>JAAS Concept: Abort on Failure</b></p>
     * <ul>
     *   <li>Called if any LoginModule's login() fails</li>
     *   <li>Or if commit() fails</li>
     *   <li>Clean up any temporary state</li>
     *   <li>Do NOT add Principals to Subject</li>
     * </ul>
     */
    @Override
    public boolean abort() throws LoginException {
        if (debug) {
            System.out.println("[DatabaseLoginModule] abort() - Cleaning up");
        }

        loginSucceeded = false;
        commitSucceeded = false;
        clearCredentials();
        userPrincipal = null;
        rolePrincipals.clear();
        
        return true;
    }

    /**
     * Logout the user.
     * 
     * <p><b>JAAS Concept: Logout</b></p>
     * <ul>
     *   <li>Remove Principals from Subject</li>
     *   <li>Called when user logs out or session expires</li>
     *   <li>Clean up authentication state</li>
     * </ul>
     */
    @Override
    public boolean logout() throws LoginException {
        if (debug) {
            System.out.println("[DatabaseLoginModule] logout() - Removing principals");
        }

        // Remove principals from subject
        if (userPrincipal != null) {
            subject.getPrincipals().remove(userPrincipal);
        }
        
        for (Principal rolePrincipal : rolePrincipals) {
            subject.getPrincipals().remove(rolePrincipal);
        }

        // Clear state
        loginSucceeded = false;
        commitSucceeded = false;
        clearCredentials();
        userPrincipal = null;
        rolePrincipals.clear();
        
        return true;
    }

    /**
     * Authenticate user against database.
     * 
     * <p>Queries users table and validates password using BCrypt.</p>
     * <p>Loads user roles from user_roles table.</p>
     */
    private boolean authenticateUser(String email, String password) throws SQLException {
        // In production, use JNDI datasource: InitialContext.lookup(dsJndiName)
        // For this demo, we'll use direct connection (NOT recommended in production)
        
        // NOTE: In a real JAAS implementation, you'd use:
        // InitialContext ctx = new InitialContext();
        // DataSource ds = (DataSource) ctx.lookup(dsJndiName);
        // Connection conn = ds.getConnection();
        
        Connection conn = null;
        PreparedStatement userStmt = null;
        PreparedStatement roleStmt = null;
        ResultSet userRs = null;
        ResultSet roleRs = null;

        try {
            // Get connection (in production, use JNDI datasource)
            conn = getDatabaseConnection();
            
            // Query user
            userStmt = conn.prepareStatement(
                "SELECT id, email, password, status FROM users WHERE email = ?"
            );
            userStmt.setString(1, email);
            userRs = userStmt.executeQuery();

            if (!userRs.next()) {
                return false; // User not found
            }

            Long userId = userRs.getLong("id");
            String storedPassword = userRs.getString("password");
            String status = userRs.getString("status");

            // Check if account is active
            if (!"active".equals(status)) {
                throw new SQLException("Account is not active");
            }

            // Verify password using BCrypt
            if (!BCrypt.checkpw(password, storedPassword)) {
                return false; // Invalid password
            }

            // Load user roles
            roleStmt = conn.prepareStatement(
                "SELECT role FROM user_roles WHERE user_id = ?"
            );
            roleStmt.setLong(1, userId);
            roleRs = roleStmt.executeQuery();

            while (roleRs.next()) {
                String role = roleRs.getString("role");
                rolePrincipals.add(new RolePrincipal(role));
                
                if (debug) {
                    System.out.println("[DatabaseLoginModule] Loaded role: " + role);
                }
            }

            return true;

        } finally {
            // Clean up resources
            if (roleRs != null) roleRs.close();
            if (userRs != null) userRs.close();
            if (roleStmt != null) roleStmt.close();
            if (userStmt != null) userStmt.close();
            if (conn != null) conn.close();
        }
    }

    /**
     * Get database connection.
     * 
     * <p><b>PRODUCTION NOTE:</b> Use JNDI datasource lookup, not direct connection.</p>
     * <p>This is a simplified implementation for demonstration purposes.</p>
     */
    private Connection getDatabaseConnection() throws SQLException {
        // In production, use JNDI:
        // InitialContext ctx = new InitialContext();
        // DataSource ds = (DataSource) ctx.lookup(dsJndiName);
        // return ds.getConnection();
        
        // For demo (NOT for production):
        String url = "jdbc:mysql://localhost:3306/zerohunger_db";
        String user = "root";
        String pass = ""; // Configure properly
        
        return DriverManager.getConnection(url, user, pass);
    }

    /**
     * Clear sensitive credentials from memory.
     */
    private void clearCredentials() {
        username = null;
        if (password != null) {
            Arrays.fill(password, ' ');
            password = null;
        }
    }

    /**
     * Principal representing user identity.
     */
    private static class UserPrincipal implements Principal {
        private final String name;

        public UserPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof UserPrincipal)) return false;
            UserPrincipal other = (UserPrincipal) obj;
            return name.equals(other.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return "UserPrincipal[" + name + "]";
        }
    }

    /**
     * Principal representing user role.
     */
    private static class RolePrincipal implements Principal {
        private final String name;

        public RolePrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof RolePrincipal)) return false;
            RolePrincipal other = (RolePrincipal) obj;
            return name.equals(other.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return "RolePrincipal[" + name + "]";
        }
    }
}
