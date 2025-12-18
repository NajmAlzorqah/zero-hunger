package com.codemavricks.zerohunger.resource;

import com.codemavricks.zerohunger.dto.DonationDTO;
import com.codemavricks.zerohunger.service.DonationService;
import com.codemavricks.zerohunger.model.Donation;
import com.codemavricks.zerohunger.model.User;
import com.codemavricks.zerohunger.filter.Secured;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MyDonation REST Resource - Donor's Own Donations Management.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. RESOURCE SEGREGATION PATTERN</h3>
 * <ul>
 *   <li><b>DonationResource</b>: Public donations (view all, search nearby)</li>
 *   <li><b>MyDonationResource</b>: Owner operations (update, delete own donations)</li>
 *   <li>Different @Path: /donations vs /my-donations</li>
 *   <li>Clearer API design, easier to apply different security rules</li>
 * </ul>
 * 
 * <h3>2. AUTHORIZATION ENFORCEMENT</h3>
 * <ul>
 *   <li>update() checks: donation.getDonor().getId().equals(user.getId())</li>
 *   <li>Prevents users from modifying others' donations</li>
 *   <li>Returns 403 Forbidden if unauthorized</li>
 *   <li>Business logic authorization (not container-managed)</li>
 * </ul>
 * 
 * <h3>3. BUSINESS RULE VALIDATION</h3>
 * <ul>
 *   <li>Cannot update/delete if status != "available"</li>
 *   <li>Returns 409 Conflict if donation already claimed</li>
 *   <li>Prevents data corruption (changing claimed donation details)</li>
 *   <li>Validates state before allowing operations</li>
 * </ul>
 * 
 * <h3>4. PARTIAL UPDATE PATTERN</h3>
 * <ul>
 *   <li>update() accepts Map&lt;String, Object&gt; instead of full entity</li>
 *   <li>Only updates provided fields (title, description, quantity, expires_at)</li>
 *   <li>Alternative to PUT (full replacement) vs PATCH (partial update)</li>
 *   <li>DonationService.updateDonation() handles field-by-field update</li>
 * </ul>
 * 
 * <h3>5. SOFT DELETE PATTERN</h3>
 * <ul>
 *   <li>delete() changes status to "deleted" instead of removing from database</li>
 *   <li>Preserves audit trail and historical data</li>
 *   <li>Alternative: hard delete with em.remove(donation)</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Concept</b>: RESTful resource design and segregation</li>
 *   <li><b>Concept</b>: Authorization at business logic level</li>
 *   <li><b>Concept</b>: State validation before operations</li>
 *   <li><b>Concept</b>: Partial updates with JPA merge</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
@Path("my-donations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MyDonationResource {

    @Inject
    private DonationService donationService;

    @GET
    @Secured
    public Response myDonations(@Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        List<Donation> donations = donationService.findMyDonations(user);
        
        List<DonationDTO> dtos = donations.stream()
            .map(d -> new DonationDTO(d, true, user))
            .collect(Collectors.toList());
            
        return Response.ok(dtos).build();
    }
    
    @PUT
    @Path("{id}")
    @Secured
    public Response update(@PathParam("id") Long id, 
                          Map<String, Object> updates,
                          @Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        Donation donation = donationService.findById(id);
        
        if (donation == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "Resource not found"))
                .build();
        }
        
        if (!donation.getDonor().getId().equals(user.getId())) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("message", "Unauthorized"))
                .build();
        }
        
        if (!"available".equals(donation.getStatus())) {
            return Response.status(409)
                .entity(Map.of("message", "Cannot update donation that has already been claimed"))
                .build();
        }
        
        try {
            Donation updated = donationService.updateDonation(id, updates);
            return Response.ok(new DonationDTO(updated, false, user)).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("message", e.getMessage()))
                .build();
        }
    }
    
    @DELETE
    @Path("{id}")
    @Secured
    public Response delete(@PathParam("id") Long id, @Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        Donation donation = donationService.findById(id);
        
        if (donation == null) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "Resource not found"))
                .build();
        }
        
        if (!donation.getDonor().getId().equals(user.getId())) {
            return Response.status(Response.Status.FORBIDDEN)
                .entity(Map.of("message", "Unauthorized"))
                .build();
        }
        
        if (!"available".equals(donation.getStatus())) {
            return Response.status(409)
                .entity(Map.of("message", "Cannot delete donation that has been claimed"))
                .build();
        }
        
        try {
            donationService.deleteDonation(id);
            return Response.ok(Map.of("message", "Donation deleted successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("message", "Failed to delete donation"))
                .build();
        }
    }
}
