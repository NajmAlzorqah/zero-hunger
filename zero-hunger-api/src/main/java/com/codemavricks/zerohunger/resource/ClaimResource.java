package com.codemavricks.zerohunger.resource;

import com.codemavricks.zerohunger.dto.ClaimDTO;
import com.codemavricks.zerohunger.model.Claim;
import com.codemavricks.zerohunger.model.User;
import com.codemavricks.zerohunger.service.ClaimService;
import com.codemavricks.zerohunger.filter.Secured;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Claim REST Resource - Web Tier for Claim Management.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. NESTED RESOURCE PATHS</h3>
 * <ul>
 *   <li><b>@Path("claims")</b>: Base path → /api/v1/claims</li>
 *   <li><b>@Path("{id}/pickup")</b>: Nested action → POST /api/v1/claims/123/pickup</li>
 *   <li><b>@Path("{id}/deliver")</b>: Another nested action → POST /api/v1/claims/123/deliver</li>
 *   <li>RESTful design: Actions as sub-resources instead of query params</li>
 * </ul>
 * 
 * <h3>2. TRANSACTION PROPAGATION</h3>
 * <ul>
 *   <li>pickup() calls ClaimService.markPickedUp() (Business Tier)</li>
 *   <li>ClaimService is @Stateless with Container-Managed Transactions</li>
 *   <li>Transaction flows: ClaimResource → ClaimService → EntityManager</li>
 *   <li>If any step fails, entire operation rolls back</li>
 * </ul>
 * 
 * <h3>3. EXCEPTION HANDLING PATTERN</h3>
 * <ul>
 *   <li>try-catch blocks convert exceptions to HTTP responses</li>
 *   <li>Business logic throws Exception with message</li>
 *   <li>Web tier catches and returns appropriate status code:</li>
 *   <ul>
 *     <li>422 Unprocessable Entity: Invalid pickup code</li>
 *     <li>409 Conflict: Cannot deliver before pickup</li>
 *     <li>403 Forbidden: Not authorized to cancel claim</li>
 *   </ul>
 * </ul>
 * 
 * <h3>4. REQUEST BODY PARSING</h3>
 * <ul>
 *   <li>pickup(@PathParam("id") Long id, <b>Map&lt;String, String&gt; body</b>)</li>
 *   <li>JAX-RS automatically deserializes JSON body to Map</li>
 *   <li>body.get("pickup_code") extracts field from JSON</li>
 *   <li>Alternative: Create PickupRequest DTO class</li>
 * </ul>
 * 
 * <h3>5. WORKFLOW ENFORCEMENT</h3>
 * <ul>
 *   <li>deliver() can only be called AFTER pickup()</li>
 *   <li>Business logic validates state transitions</li>
 *   <li>Returns 409 Conflict if workflow violated</li>
 *   <li>Demonstrates state machine pattern in REST API</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Concept</b>: JAX-RS nested resources and path parameters</li>
 *   <li><b>Concept</b>: Transaction propagation across tiers</li>
 *   <li><b>Concept</b>: Exception handling in web tier</li>
 *   <li><b>Concept</b>: RESTful workflow design</li>
 * </ul>
 * 
 * <h3>Claim Workflow:</h3>
 * <pre>
 * 1. Volunteer: POST /claims (creates claim, donation status → "reserved")
 * 2. Volunteer: POST /claims/123/pickup (with pickup_code from donor)
 *    → claim.pickedUpAt = now
 *    → donation.status → "picked_up"
 * 3. Volunteer: POST /claims/123/deliver (after delivery)
 *    → claim.deliveredAt = now
 *    → claim.status → "completed"
 *    → donation.status → "delivered"
 *    → donor.impactScore += quantity
 * </pre>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
@Path("claims")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClaimResource {

    @Inject
    private ClaimService claimService;
    
    @GET
    @Secured
    public Response list(@Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        List<Claim> claims = claimService.getMyClaims(user);
        
        List<ClaimDTO> dtos = claims.stream()
            .map(c -> new ClaimDTO(c, true))
            .collect(Collectors.toList());
            
        return Response.ok(dtos).build();
    }
    
    @POST
    @Path("{id}/pickup")
    @Secured
    public Response pickup(@PathParam("id") Long id, Map<String, String> body, @Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        
        try {
            String code = body.get("pickup_code");
            if (code == null || code.trim().isEmpty()) {
                return Response.status(422)
                    .entity(Map.of("message", "Pickup code is required"))
                    .build();
            }
            
            Claim claim = claimService.markPickedUp(id, code);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Marked as picked up successfully");
            response.put("claim", new ClaimDTO(claim, true));
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(422)
                .entity(Map.of("message", e.getMessage()))
                .build();
        }
    }
    
    @POST
    @Path("{id}/deliver")
    @Secured
    public Response deliver(@PathParam("id") Long id, Map<String, String> body, @Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        
        try {
            String notes = body != null ? body.get("notes") : null;
            Claim claim = claimService.markDelivered(id, notes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Marked as delivered successfully! Thank you for your service.");
            response.put("claim", new ClaimDTO(claim, true));
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(409)
                .entity(Map.of("message", e.getMessage()))
                .build();
        }
    }
    
    @DELETE
    @Path("{id}")
    @Secured
    public Response cancel(@PathParam("id") Long id, @Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        
        try {
            claimService.cancelClaim(id, user);
            return Response.ok(Map.of("message", "Claim cancelled successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("message", e.getMessage()))
                .build();
        }
    }
}
