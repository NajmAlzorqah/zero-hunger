package com.codemavricks.zerohunger.filter;

import com.codemavricks.zerohunger.model.User;
import com.codemavricks.zerohunger.service.UserService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.NameBinding;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Optional;

/**
 * Authentication Filter - Request Interceptor for Security.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. INTERCEPTORS (Cross-Cutting Concerns)</h3>
 * <ul>
 *   <li><b>What it is</b>: Code that runs BEFORE/AFTER method calls</li>
 *   <li><b>@Provider</b>: Registers this as a JAX-RS extension component</li>
 *   <li><b>ContainerRequestFilter</b>: Intercepts HTTP requests before reaching resource methods</li>
 *   <li><b>Use Case</b>: Authentication, logging, validation, authorization</li>
 *   <li><b>Cross-Cutting Concern</b>: Security logic applies to MANY endpoints, not duplicated in each</li>
 * </ul>
 * 
 * <h3>2. NAME BINDING (@Secured Annotation)</h3>
 * <ul>
 *   <li><b>Selective Interception</b>: Only applies to methods/classes annotated with @Secured</li>
 *   <li><b>@NameBinding</b>: Links filter to custom annotation</li>
 *   <li>Login/Register endpoints: NO @Secured → filter doesn't run</li>
 *   <li>Protected endpoints: @Secured → filter runs automatically</li>
 * </ul>
 * 
 * <h3>3. FILTER PRIORITY</h3>
 * <ul>
 *   <li><b>@Priority(AUTHENTICATION)</b>: Runs in authentication phase</li>
 *   <li>Order: AUTHENTICATION → AUTHORIZATION → ENTITY_CODER</li>
 *   <li>Ensures authentication happens before other filters</li>
 * </ul>
 * 
 * <h3>4. REQUEST CONTEXT MANIPULATION</h3>
 * <ul>
 *   <li><b>requestContext.abortWith()</b>: Stops request processing, returns 401 Unauthorized</li>
 *   <li><b>requestContext.setProperty("user")</b>: Passes authenticated user to resource method</li>
 *   <li>Resource methods retrieve with: @Context ContainerRequestContext ctx; User user = ctx.getProperty("user")</li>
 * </ul>
 * 
 * <h3>5. SECURITY WITHOUT CONTAINER-MANAGED SECURITY</h3>
 * <ul>
 *   <li><b>Alternative to JAAS</b>: Custom token-based authentication</li>
 *   <li>More flexible than web.xml security-constraint</li>
 *   <li>Works with JWT, OAuth, custom tokens</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Chapter</b>: Interceptors - injecting code before/after method calls</li>
 *   <li><b>Concept</b>: Cross-cutting concerns (security, logging, transactions)</li>
 *   <li><b>Chapter</b>: Security - Authentication and Authorization in enterprise apps</li>
 *   <li><b>Concept</b>: Filter chains and request processing pipeline</li>
 * </ul>
 * 
 * <h3>Request Flow with Interceptor:</h3>
 * <pre>
 * Client GET /api/v1/donations (with Authorization: Bearer token123)
 *   ↓
 * JAX-RS Container receives request
 *   ↓
 * AuthFilter.filter() executes (BEFORE resource method)
 *   → Extracts token from header
 *   → Calls UserService.findByToken()
 *   → If valid: sets user in context, continues
 *   → If invalid: abortWith(401), stops here
 *   ↓
 * DonationResource.getAvailableDonations() executes
 *   ↓
 * Response returned to client
 * </pre>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 * @see jakarta.ws.rs.container.ContainerRequestFilter
 * @see Secured
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    @Inject
    private UserService userService;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        String token = authHeader.substring("Bearer ".length()).trim();

        try {
            Optional<User> user = userService.findByToken(token);
            if (user.isPresent()) {
                 // Pass user to the endpoint via property
                 requestContext.setProperty("user", user.get());
            } else {
                 requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        } catch (Exception e) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }
    }
}
