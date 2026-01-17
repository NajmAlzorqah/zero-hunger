package com.codemavricks.zerohunger.resource;

import com.codemavricks.zerohunger.dto.CreateDonationRequest;
import com.codemavricks.zerohunger.dto.UpdateDonationRequest;
import com.codemavricks.zerohunger.dto.DonationDTO;
import com.codemavricks.zerohunger.model.Donation;
import com.codemavricks.zerohunger.model.User;
import com.codemavricks.zerohunger.service.ClaimService;
import com.codemavricks.zerohunger.service.DonationService;
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
 * Donation REST Resource - Web Tier Component for Donation Management.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. JAX-RS REST ENDPOINTS</h3>
 * <ul>
 *   <li><b>@Path("donations")</b>: Base path → /api/v1/donations</li>
 *   <li><b>@GET</b>: list() → GET /api/v1/donations (all available donations)</li>
 *   <li><b>@GET @Path("nearby")</b>: nearby() → GET /api/v1/donations/nearby?latitude=X&longitude=Y</li>
 *   <li><b>@GET @Path("{id}")</b>: get() → GET /api/v1/donations/123</li>
 *   <li><b>@POST</b>: create() → POST /api/v1/donations</li>
 *   <li><b>@POST @Path("{donationId}/claim")</b>: claim() → POST /api/v1/donations/123/claim</li>
 * </ul>
 * 
 * <h3>2. QUERY PARAMETERS (@QueryParam)</h3>
 * <ul>
 *   <li><b>@QueryParam("latitude")</b>: Extracts ?latitude=40.7128 from URL</li>
 *   <li><b>@DefaultValue("10")</b>: Sets default radius to 10km if not provided</li>
 *   <li>JAX-RS automatically converts String → Double</li>
 * </ul>
 * 
 * <h3>3. PATH PARAMETERS (@PathParam)</h3>
 * <ul>
 *   <li><b>@PathParam("id")</b>: Extracts donation ID from URL path</li>
 *   <li>Example: /donations/123 → id = 123</li>
 *   <li>Type-safe conversion to Long</li>
 * </ul>
 * 
 * <h3>4. SECURED ENDPOINTS (@Secured)</h3>
 * <ul>
 *   <li>All methods have @Secured → AuthFilter runs before method execution</li>
 *   <li>Validates Bearer token from Authorization header</li>
 *   <li>Sets authenticated user in ContainerRequestContext</li>
 *   <li>If invalid token → 401 Unauthorized before method is called</li>
 * </ul>
 * 
 * <h3>5. CONTEXT INJECTION (@Context)</h3>
 * <ul>
 *   <li><b>@Context ContainerRequestContext</b>: Access to request metadata</li>
 *   <li>req.getProperty("user") → retrieves User set by AuthFilter</li>
 *   <li>Enables stateless authentication (no HttpSession)</li>
 * </ul>
 * 
 * <h3>6. N-TIER ARCHITECTURE - WEB TIER</h3>
 * <ul>
 *   <li><b>Thin Controller Pattern</b>:</li>
 *   <ul>
 *     <li>DonationResource: Handles HTTP (parse request, build response)</li>
 *     <li>DonationService: Contains business logic (find donations, validate)</li>
 *     <li>Separation prevents mixing web concerns with business logic</li>
 *   </ul>
 *   <li><b>Request Flow</b>:</li>
 *   <ul>
 *     <li>Client sends GET /api/v1/donations/nearby?latitude=40&longitude=-74</li>
 *     <li>AuthFilter validates token (Interceptor)</li>
 *     <li>DonationResource.nearby() extracts parameters (Web Tier)</li>
 *     <li>DonationService.findNearby() queries database (Business Tier)</li>
 *     <li>JPA/Hibernate executes SQL (Data Tier)</li>
 *     <li>DonationDTO transforms entities to JSON (DTO Pattern)</li>
 *     <li>Response sent to client</li>
 *   </ul>
 * </ul>
 * 
 * <h3>7. DTO PATTERN (Data Transfer Object)</h3>
 * <ul>
 *   <li><b>Problem</b>: Sending JPA entities directly causes:</li>
 *   <ul>
 *     <li>LazyInitializationException (when relationships not loaded)</li>
 *     <li>Circular references (Donation ↔ Claim infinite loop)</li>
 *     <li>Security issues (exposing password fields)</li>
 *   </ul>
 *   <li><b>Solution</b>: Convert Donation entity → DonationDTO</li>
 *   <li>DTOs control exactly what data is sent to client</li>
 *   <li>Prevents entity state changes from affecting database</li>
 * </ul>
 * 
 * <h3>8. ROLE-BASED ACCESS CONTROL</h3>
 * <ul>
 *   <li>create() checks: user.getRoles().contains("donor")</li>
 *   <li>Only donors can create donations</li>
 *   <li>Returns 403 Forbidden if not authorized</li>
 *   <li>Alternative to declarative @RolesAllowed annotation</li>
 * </ul>
 * 
 * <h3>9. HTTP STATUS CODES (RESTful Conventions)</h3>
 * <ul>
 *   <li><b>200 OK</b>: Successful GET/PUT/POST (list, get, claim)</li>
 *   <li><b>201 Created</b>: Successful POST that creates resource (create)</li>
 *   <li><b>400 Bad Request</b>: Missing required parameters</li>
 *   <li><b>403 Forbidden</b>: User not authorized for action</li>
 *   <li><b>404 Not Found</b>: Donation doesn't exist</li>
 *   <li><b>409 Conflict</b>: Donation already claimed (race condition)</li>
 *   <li><b>422 Unprocessable Entity</b>: Validation failed</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Chapter</b>: N-Tier Architecture - Web Tier (Presentation Layer)</li>
 *   <li><b>Concept</b>: JAX-RS for RESTful Web Services</li>
 *   <li><b>Concept</b>: Dependency Injection across tiers (@Inject)</li>
 *   <li><b>Concept</b>: DTO Pattern for decoupling entities from API</li>
 *   <li><b>Concept</b>: Security - Authorization checks</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 * @see DonationService
 * @see DonationDTO
 * @see AuthFilter
 */
@Path("donations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DonationResource {

    @Inject
    private DonationService donationService;
    
    @Inject
    private ClaimService claimService;

    @GET
    @Secured
    public Response list(@Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        List<Donation> donations = donationService.findAllAvailable();
        
        List<DonationDTO> dtos = donations.stream()
            .map(d -> new DonationDTO(d, false, user))
            .collect(Collectors.toList());
            
        return Response.ok(dtos).build();
    }
    
    @GET
    @Path("nearby")
    @Secured
    public Response nearby(@QueryParam("latitude") Double lat, 
                          @QueryParam("longitude") Double lng, 
                          @QueryParam("radius") @DefaultValue("10") Double radius,
                          @Context ContainerRequestContext req) {
        if (lat == null || lng == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("message", "Latitude and longitude are required"))
                .build();
        }
        
        User user = (User) req.getProperty("user");
        List<Donation> donations = donationService.findNearby(lat, lng, radius);
        
        // Convert to DTOs and calculate distance for each
        List<DonationDTO> dtos = donations.stream()
            .map(d -> {
                DonationDTO dto = new DonationDTO(d, false, user);
                // Calculate and set distance for nearby endpoint
                Double distance = donationService.calculateDistance(lat, lng, d.getLatitude(), d.getLongitude());
                dto.setDistance(Math.round(distance * 100.0) / 100.0); // Round to 2 decimal places
                return dto;
            })
            .collect(Collectors.toList());
            
        return Response.ok(dtos).build();
    }
    
    @GET
    @Path("{id}")
    @Secured
    public Response get(@PathParam("id") Long id, @Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        Donation donation = donationService.findById(id);
        
        if (donation == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "Resource not found"))
                .build();
        }
        
        return Response.ok(new DonationDTO(donation, true, user)).build();
    }
    
    @POST
    @Secured
    public Response create(CreateDonationRequest request, @Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        
        if (!user.getRoles().contains("donor")) {
             return Response.status(Response.Status.FORBIDDEN)
                 .entity(Map.of("message", "Only donors can create donations"))
                 .build();
        }
        
        Donation donation = new Donation();
        donation.setTitle(request.getTitle());
        donation.setDescription(request.getDescription());
        donation.setQuantityKg(request.getQuantityKg());
        donation.setLatitude(request.getLatitude());
        donation.setLongitude(request.getLongitude());
        donation.setExpiresAt(request.getExpiresAt());
        
        Donation created = donationService.createDonation(donation, user);
        return Response.status(Response.Status.CREATED)
            .entity(new DonationDTO(created, false, user))
            .build();
    }
    
    @POST
    @Path("{id}/claim")
    @Secured
    public Response claim(@PathParam("id") Long id, @Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        
        if (!user.getRoles().contains("volunteer")) {
             return Response.status(Response.Status.FORBIDDEN)
                 .entity(Map.of("message", "Only volunteers can claim donations"))
                 .build();
        }
        
        try {
            var claim = claimService.claimDonation(id, user);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Donation claimed successfully");
            response.put("donation", new DonationDTO(claim.getDonation(), true, user));
            response.put("pickup_code", claim.getDonation().getPickupCode());
            return Response.ok(response).build();
        } catch (Exception e) {
             return Response.status(409)
                 .entity(Map.of("message", e.getMessage()))
                 .build();
        }
    }
    
    @PUT
    @Path("{id}")
    @Secured
    public Response update(@PathParam("id") Long id, UpdateDonationRequest request, @Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        Donation donation = donationService.findById(id);
        
        if (donation == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "Donation not found"))
                .build();
        }
        
        // Check ownership
        if (!donation.getDonor().getId().equals(user.getId())) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("message", "Unauthorized"))
                .build();
        }
        
        // Cannot update if claimed
        if (!"available".equals(donation.getStatus())) {
            return Response.status(409)
                .entity(Map.of("message", "Cannot update donation that has already been claimed"))
                .build();
        }
        
        // Apply updates from request
        if (request.getTitle() != null) {
            donation.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            donation.setDescription(request.getDescription());
        }
        if (request.getQuantityKg() != null) {
            donation.setQuantityKg(request.getQuantityKg());
        }
        if (request.getExpiresAt() != null) {
            donation.setExpiresAt(request.getExpiresAt());
        }
        
        Donation updated = donationService.updateDonation(donation);
        return Response.ok(new DonationDTO(updated, false, user)).build();
    }
    
    @DELETE
    @Path("{id}")
    @Secured
    public Response delete(@PathParam("id") Long id, @Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        Donation donation = donationService.findById(id);
        
        if (donation == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "Donation not found"))
                .build();
        }
        
        // Check ownership
        if (!donation.getDonor().getId().equals(user.getId())) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("message", "Unauthorized"))
                .build();
        }
        
        // Cannot delete if claimed
        if (!"available".equals(donation.getStatus())) {
            return Response.status(409)
                .entity(Map.of("message", "Cannot delete donation that has been claimed"))
                .build();
        }
        
        donationService.deleteDonation(id);
        return Response.ok(Map.of("message", "Donation deleted successfully")).build();
    }
}
