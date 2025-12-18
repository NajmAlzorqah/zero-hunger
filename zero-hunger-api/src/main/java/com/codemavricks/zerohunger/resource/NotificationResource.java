package com.codemavricks.zerohunger.resource;

import com.codemavricks.zerohunger.model.Notification;
import com.codemavricks.zerohunger.model.User;
import com.codemavricks.zerohunger.service.NotificationService;
import com.codemavricks.zerohunger.filter.Secured;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.*;
import java.util.List;
import java.util.Map;

/**
 * Notification REST Resource - User Notification Management.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. RESTful ACTION ENDPOINTS</h3>
 * <ul>
 *   <li><b>GET /notifications</b>: Retrieve notifications (standard REST)</li>
 *   <li><b>POST /notifications/{id}/read</b>: Action endpoint (mark as read)</li>
 *   <li><b>POST /notifications/read-all</b>: Bulk action endpoint</li>
 *   <li><b>GET /notifications/unread-count</b>: Computed property endpoint</li>
 * </ul>
 * 
 * <h3>2. PAGINATION HINT</h3>
 * <ul>
 *   <li>list() returns latest 20 notifications</li>
 *   <li>NotificationService uses setMaxResults(20)</li>
 *   <li>Prevents loading thousands of records into memory</li>
 *   <li><b>Missing</b>: Full pagination with offset/limit query params</li>
 *   <li><b>Enhancement</b>: Add @QueryParam("page") and @QueryParam("limit")</li>
 * </ul>
 * 
 * <h3>3. BULK OPERATIONS</h3>
 * <ul>
 *   <li>markAllAsRead() updates multiple rows in single transaction</li>
 *   <li>Uses JPQL UPDATE query: "UPDATE Notification SET readAt = :now WHERE..."</li>
 *   <li>More efficient than: for (n : notifications) { n.setReadAt(); em.merge(n); }</li>
 *   <li>Demonstrates bulk update pattern in JPA</li>
 * </ul>
 * 
 * <h3>4. AGGREGATE QUERIES</h3>
 * <ul>
 *   <li>getUnreadCount() returns COUNT(*) from database</li>
 *   <li>More efficient than: notifications.stream().filter(n → n.readAt == null).count()</li>
 *   <li>Database does aggregation instead of loading all data to Java</li>
 * </ul>
 * 
 * <h3>5. ASYNCHRONOUS PATTERN (POTENTIAL ENHANCEMENT)</h3>
 * <ul>
 *   <li><b>Current</b>: Notifications created synchronously in ClaimService</li>
 *   <li><b>Enhancement</b>: Use JMS + Message-Driven Bean for async notifications</li>
 *   <li><b>Benefits</b>: Claim operation doesn't wait for notification creation</li>
 *   <li><b>Book Concept</b>: Message-Driven Beans (MDB) for asynchronous processing</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Concept</b>: RESTful API design for actions</li>
 *   <li><b>Concept</b>: JPQL bulk operations for performance</li>
 *   <li><b>Concept</b>: Aggregate queries vs loading collections</li>
 *   <li><b>Missing Concept</b>: JMS for async notifications (not implemented)</li>
 * </ul>
 * 
 * <h3>Potential Enhancement with MDB:</h3>
 * <pre>
 * // Instead of:
 * notificationService.createNotification(user, "claim_created", data);
 * 
 * // Use JMS:
 * &#64;Inject
 * private JMSContext jmsContext;
 * 
 * &#64;Resource(lookup = "java:/jms/queue/NotificationQueue")
 * private Queue notificationQueue;
 * 
 * jmsContext.createProducer().send(notificationQueue, notificationMessage);
 * 
 * // Separate MDB processes queue asynchronously:
 * &#64;MessageDriven
 * public class NotificationMDB implements MessageListener {
 *     public void onMessage(Message msg) {
 *         // Create notification in background
 *     }
 * }
 * </pre>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
@Path("notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationResource {

    @Inject
    private NotificationService notificationService;

    /**
     * Get user's notifications (latest 20)
     * GET /api/v1/notifications
     */
    @GET
    @Secured
    public Response list(@Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        List<Notification> notifications = notificationService.getUserNotifications(user.getId(), 20);
        return Response.ok(notifications).build();
    }
    
    /**
     * Mark single notification as read
     * POST /api/v1/notifications/{id}/read
     */
    @POST
    @Path("{id}/read")
    @Secured
    public Response markRead(@PathParam("id") Long id, @Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        try {
            notificationService.markAsRead(id, user.getId());
            return Response.ok(Map.of("message", "Marked as read")).build();
        } catch (Exception e) {
            return Response.status(404).entity(Map.of("message", e.getMessage())).build();
        }
    }
    
    /**
     * Mark all notifications as read
     * POST /api/v1/notifications/read-all
     */
    @POST
    @Path("read-all")
    @Secured
    public Response markAllAsRead(@Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        notificationService.markAllAsRead(user.getId());
        return Response.ok(Map.of("message", "All notifications marked as read")).build();
    }
    
    /**
     * Get count of unread notifications
     * GET /api/v1/notifications/unread-count
     */
    @GET
    @Path("unread-count")
    @Secured
    public Response getUnreadCount(@Context ContainerRequestContext req) {
        User user = (User) req.getProperty("user");
        long count = notificationService.getUnreadCount(user.getId());
        return Response.ok(Map.of("count", count)).build();
    }
}
