package com.codemavricks.zerohunger.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * CORS Filter - Request and Response Interceptor for Cross-Origin Requests.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. PRE-MATCHING REQUEST FILTER</h3>
 * <ul>
 *   <li><b>@PreMatching</b>: Executes BEFORE JAX-RS resource matching</li>
 *   <li>Handles OPTIONS preflight requests before any other filter (including AuthFilter)</li>
 *   <li>Prevents CORS errors by responding to preflight before authentication checks</li>
 * </ul>
 * 
 * <h3>2. RESPONSE FILTERS (Post-Processing)</h3>
 * <ul>
 *   <li><b>ContainerResponseFilter</b>: Intercepts responses AFTER resource method executes</li>
 *   <li>Modifies HTTP headers before sending to client</li>
 *   <li><b>Global Application</b>: No @NameBinding → applies to ALL endpoints</li>
 * </ul>
 * 
 * <h3>3. CROSS-CUTTING CONCERN - CORS</h3>
 * <ul>
 *   <li>Adds CORS headers to every response automatically</li>
 *   <li>Avoids duplicating header logic in every resource method</li>
 *   <li>Enables frontend (React/Angular/Next.js) on different domain to call API</li>
 *   <li>Allows ALL origins with wildcard (*)</li>
 * </ul>
 * 
 * <h3>4. FILTER PIPELINE</h3>
 * <ul>
 *   <li>Request: CorsFilter handles OPTIONS preflight immediately</li>
 *   <li>Other requests: Continue to resource method</li>
 *   <li>Response: CorsFilter.filter() adds headers to all responses</li>
 * </ul>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Concept</b>: Response Filters vs Request Filters</li>
 *   <li><b>Concept</b>: Global interceptors for cross-cutting concerns</li>
 *   <li><b>Concept</b>: PreMatching filters for early request interception</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
@Provider
@PreMatching
@Priority(Priorities.HEADER_DECORATOR)
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    /**
     * Request filter - handles OPTIONS preflight requests.
     * Runs BEFORE resource matching and authentication filters.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Handle preflight OPTIONS requests immediately
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            requestContext.abortWith(
                Response.ok()
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, X-Requested-With")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH")
                    .header("Access-Control-Max-Age", "86400")
                    .build()
            );
        }
    }

    /**
     * Response filter - adds CORS headers to all responses.
     * Uses putSingle() to avoid duplicate header values.
     * Skips OPTIONS requests since they are handled by the request filter.
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // Skip OPTIONS - already handled in request filter
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }
        
        // Use putSingle to set (not append) headers, preventing duplicates
        responseContext.getHeaders().putSingle("Access-Control-Allow-Origin", "*");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, Authorization, X-Requested-With");
        responseContext.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        responseContext.getHeaders().putSingle("Access-Control-Expose-Headers", "Authorization, Content-Type");
        responseContext.getHeaders().putSingle("Access-Control-Max-Age", "86400");
    }
}
