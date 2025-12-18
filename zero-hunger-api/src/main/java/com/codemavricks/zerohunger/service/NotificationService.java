package com.codemavricks.zerohunger.service;

import com.codemavricks.zerohunger.model.Notification;
import com.codemavricks.zerohunger.model.User;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.time.LocalDateTime;

/**
 * NotificationService - Business Logic for User Notifications.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. JPQL BULK UPDATE OPERATIONS</h3>
 * <ul>
 *   <li><b>UPDATE Query</b>: Updates multiple rows in single SQL statement</li>
 *   <li>markAllAsRead(): UPDATE notifications SET read_at = NOW() WHERE user_id = ? AND read_at IS NULL</li>
 *   <li><b>Performance</b>: Updates N rows with 1 query (vs N individual updates)</li>
 *   <li><b>executeUpdate()</b>: Returns number of affected rows</li>
 * </ul>
 * 
 * <h3>2. QUERY RESULT LIMITS</h3>
 * <ul>
 *   <li><b>setMaxResults(limit)</b>: SQL LIMIT clause for pagination</li>
 *   <li>Prevents loading thousands of notifications in memory</li>
 *   <li>Essential for scalability</li>
 * </ul>
 * 
 * <h3>3. TRANSACTIONAL CONSISTENCY</h3>
 * <ul>
 *   <li>createNotification() and markAsRead() in same transaction</li>
 *   <li>If notification creation fails, rollback ensures no orphan data</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Concept</b>: JPQL Bulk Operations (UPDATE, DELETE)</li>
 *   <li><b>Concept</b>: Query pagination and performance optimization</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
@Stateless
public class NotificationService {

    @PersistenceContext(unitName = "ZeroHungerPU")
    private EntityManager em;

    public void createNotification(User user, String type, String data) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setData(data); // In real app, serialize object to JSON
        em.persist(notification);
    }
    
    public List<Notification> getUserNotifications(Long userId, int limit) {
        return em.createQuery("SELECT n FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC", Notification.class)
                .setParameter("userId", userId)
                .setMaxResults(limit)
                .getResultList();
    }
    
    public List<Notification> getMyNotifications(User user) {
        return getUserNotifications(user.getId(), 20);
    }
    
    public void markAsRead(Long id, Long userId) throws Exception {
        Notification n = em.find(Notification.class, id);
        if (n == null) throw new Exception("Notification not found");
        if (!n.getUser().getId().equals(userId)) throw new Exception("Unauthorized");
        
        n.setReadAt(LocalDateTime.now());
        em.merge(n);
    }
    
    public void markAllAsRead(Long userId) {
        em.createQuery("UPDATE Notification n SET n.readAt = :now WHERE n.user.id = :userId AND n.readAt IS NULL")
            .setParameter("now", LocalDateTime.now())
            .setParameter("userId", userId)
            .executeUpdate();
    }
    
    public long getUnreadCount(Long userId) {
        return em.createQuery("SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.readAt IS NULL", Long.class)
            .setParameter("userId", userId)
            .getSingleResult();
    }
}
