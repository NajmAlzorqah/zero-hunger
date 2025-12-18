package com.codemavricks.zerohunger.resource;

import com.codemavricks.zerohunger.dto.LoginRequest;
import com.codemavricks.zerohunger.dto.RegisterRequest;
import com.codemavricks.zerohunger.dto.UserDTO;
import com.codemavricks.zerohunger.model.User;
import com.codemavricks.zerohunger.service.UserService;
import com.codemavricks.zerohunger.filter.Secured;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Authentication REST Resource - Web Tier Component.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. JAX-RS REST ENDPOINT (@Path)</h3>
 * <ul>
 *   <li><b>@Path("/")</b>: Maps this class to root path under /api/v1</li>
 *   <li><b>@POST</b>: HTTP POST method for register/login endpoints</li>
 *   <li><b>@Path("register")</b>: Creates /api/v1/register endpoint</li>
 *   <li><b>@Produces(JSON)</b>: Returns JSON responses automatically</li>
 *   <li><b>@Consumes(JSON)</b>: Accepts JSON request bodies</li>
 * </ul>
 * 
 * <h3>2. N-TIER ARCHITECTURE - WEB TIER</h3>
 * <ul>
 *   <li><b>Separation of Concerns</b>:</li>
 *   <ul>
 *     <li>AuthResource (Web Tier): Handles HTTP requests/responses</li>
 *     <li>UserService (Business Tier): Contains authentication logic</li>
 *     <li>User Entity (Data Tier): Persisted to database via JPA</li>
 *   </ul>
 *   <li><b>Thin Controller</b>: Delegates business logic to service layer</li>
 * </ul>
 * 
 * <h3>3. DEPENDENCY INJECTION IN WEB TIER</h3>
 * <ul>
 *   <li><b>@Inject UserService</b>: Container injects EJB into REST resource</li>
 *   <li>Cross-tier injection: Web component using Business component</li>
 *   <li>Automatic transaction propagation from web to service layer</li>
 * </ul>
 * 
 * <h3>4. JAX-RS RESPONSE BUILDING</h3>
 * <ul>
 *   <li><b>Response.status()</b>: Sets HTTP status code (200, 201, 401, 422)</li>
 *   <li><b>.entity()</b>: Sets response body (auto-converts to JSON)</li>
 *   <li><b>.build()</b>: Constructs final HTTP response</li>
 *   <li>Matches RESTful conventions: 201 for created, 401 for unauthorized</li>
 * </ul>
 * 
 * <h3>5. CONTEXT INJECTION (@Context)</h3>
 * <ul>
 *   <li><b>@Context SecurityContext</b>: Access to security information</li>
 *   <li>Provides user principal, roles, authentication status</li>
 *   <li>Part of Jakarta EE security integration</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Chapter</b>: N-Tier Architecture - Web Tier (Presentation Layer)</li>
 *   <li><b>Concept</b>: JAX-RS for RESTful Web Services</li>
 *   <li><b>Concept</b>: Dependency Injection across tiers</li>
 *   <li><b>Concept</b>: Security Context in Web Tier</li>
 * </ul>
 * 
 * <h3>Request Flow Example:</h3>
 * <pre>
 * Client POST /api/v1/login {email, password}
 *   ↓
 * AuthResource.login() receives LoginRequest
 *   ↓
 * @Inject UserService (Business Tier)
 *   ↓
 * UserService.login() validates credentials
 *   ↓
 * EntityManager (Data Tier) queries database
 *   ↓
 * Response 200 {message, user, token} sent to client
 * </pre>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 * @see jakarta.ws.rs.Path
 * @see jakarta.ws.rs.core.Response
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    private UserService userService;
    
    @Context
    private SecurityContext securityContext;

    @POST
    @Path("register")
    public Response register(RegisterRequest req) {
        if (userService.findByEmail(req.email).isPresent()) {
            Map<String, Object> errors = new HashMap<>();
            errors.put("email", new String[]{"The email has already been taken."});
            
            return Response.status(422)
                    .entity(Map.of("message", "Validation failed", "errors", errors))
                    .build();
        }

        User user = new User();
        user.setName(req.name);
        user.setEmail(req.email);
        user.setPassword(req.password);
        user.setPhone(req.phone);
        user.addRole(req.role);
        
        userService.register(user);
        
        Optional<String> token = userService.login(req.email, req.password);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registration successful");
        response.put("user", new UserDTO(user));
        response.put("token", token.orElse(""));
        
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @POST
    @Path("login")
    public Response login(LoginRequest req) {
        if (req.email == null || req.password == null) {
            return Response.status(422)
                .entity(Map.of("message", "Email and password are required"))
                .build();
        }
        
        Optional<User> userOpt = userService.findByEmail(req.email);
        
        if (userOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("message", "The provided credentials are incorrect."))
                    .build();
        }
        
        User user = userOpt.get();
        
        if (!"active".equals(user.getStatus())) {
            return Response.status(403)
                .entity(Map.of("message", "Your account is not active. Please contact support."))
                .build();
        }
        
        Optional<String> token = userService.login(req.email, req.password);
        
        if (token.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("user", new UserDTO(user));
            response.put("token", token.get());
            return Response.ok(response).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("message", "The provided credentials are incorrect."))
                    .build();
        }
    }
    
    @POST
    @Path("logout")
    @Secured
    public Response logout() {
        // In a stateless token system, logout is typically client-side
        // Could implement token revocation/blacklist if needed
        return Response.ok(Map.of("message", "Logged out successfully")).build();
    }
    
    @GET
    @Path("me")
    @Secured
    public Response me(@Context ContainerRequestContext requestContext) {
        User user = (User) requestContext.getProperty("user");
        return Response.ok(Map.of("user", new UserDTO(user))).build();
    }
    
    @PUT
    @Path("profile")
    @Secured
    public Response updateProfile(Map<String, Object> body, @Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        
        String name = (String) body.get("name");
        String phone = (String) body.get("phone");
        Double lat = body.containsKey("latitude") ? ((Number) body.get("latitude")).doubleValue() : null;
        Double lng = body.containsKey("longitude") ? ((Number) body.get("longitude")).doubleValue() : null;
        
        User updated = userService.updateProfile(user.getId(), name, phone, lat, lng);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Profile updated successfully");
        response.put("user", new UserDTO(updated));
        
        return Response.ok(response).build();
    }
}
