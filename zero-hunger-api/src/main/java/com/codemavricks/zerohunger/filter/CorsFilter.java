package com.codemavricks.zerohunger.filter;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * CORS Filter - Response Interceptor for Cross-Origin Requests.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. RESPONSE FILTERS (Post-Processing)</h3>
 * <ul>
 *   <li><b>ContainerResponseFilter</b>: Intercepts responses AFTER resource method executes</li>
 *   <li>Modifies HTTP headers before sending to client</li>
 *   <li><b>Global Application</b>: No @NameBinding → applies to ALL endpoints</li>
 * </ul>
 * 
 * <h3>2. CROSS-CUTTING CONCERN - CORS</h3>
 * <ul>
 *   <li>Adds CORS headers to every response automatically</li>
 *   <li>Avoids duplicating header logic in every resource method</li>
 *   <li>Enables frontend (React/Angular) on different domain to call API</li>
 * </ul>
 * 
 * <h3>3. FILTER PIPELINE</h3>
 * <ul>
 *   <li>Request: CorsFilter skipped (no ContainerRequestFilter)</li>
 *   <li>Resource method executes</li>
 *   <li>Response: CorsFilter.filter() adds headers</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Concept</b>: Response Filters vs Request Filters</li>
 *   <li><b>Concept</b>: Global interceptors for cross-cutting concerns</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}
