package com.codemavricks.zerohunger.resource;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application Configuration.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. JAX-RS (Java API for RESTful Web Services)</h3>
 * <ul>
 *   <li><b>@ApplicationPath</b>: Sets base URL path for all REST endpoints</li>
 *   <li>All resources accessible under: http://host:port/context-root/api/v1/*</li>
 *   <li><b>Automatic Discovery</b>: Container scans for @Path classes in this package</li>
 * </ul>
 * 
 * <h3>2. WEB TIER ENTRY POINT</h3>
 * <ul>
 *   <li>This is the entry point to the Web Tier (Presentation Layer)</li>
 *   <li>Part of N-Tier Architecture: Client → Web Tier (JAX-RS) → Business Tier (EJB) → Data Tier (JPA)</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Chapter</b>: N-Tier Architecture - Web Tier configuration</li>
 *   <li><b>Concept</b>: RESTful Web Services with JAX-RS</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
@ApplicationPath("/api/v1")
public class JaxRsApplication extends Application {
}
