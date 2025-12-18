package com.codemavricks.zerohunger.filter;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Secured - Custom Annotation for Authentication Required.
 * 
 * <h2>Java EE / Jakarta EE Concepts Demonstrated:</h2>
 * 
 * <h3>1. NAME BINDING ANNOTATION</h3>
 * <ul>
 *   <li><b>@NameBinding</b>: Links filters/interceptors to specific endpoints</li>
 *   <li><b>How it works</b>:</li>
 *   <ul>
 *     <li>AuthFilter has @Secured annotation</li>
 *     <li>Resource methods with @Secured trigger the filter</li>
 *     <li>Methods without @Secured skip the filter</li>
 *   </ul>
 * </ul>
 * 
 * <h3>2. CUSTOM ANNOTATIONS (Meta-Programming)</h3>
 * <ul>
 *   <li><b>@Retention(RUNTIME)</b>: Annotation available at runtime for reflection</li>
 *   <li><b>@Target</b>: Can be applied to classes (TYPE) or methods (METHOD)</li>
 * </ul>
 * 
 * <h3>3. DECLARATIVE SECURITY</h3>
 * <ul>
 *   <li>Cleaner than: if (!isAuthenticated()) throw Exception;</li>
 *   <li>Usage: @Secured on method → authentication enforced automatically</li>
 *   <li>Similar to: @RolesAllowed, @PermitAll in Jakarta EE Security</li>
 * </ul>
 * 
 * <h3>Example Usage:</h3>
 * <pre>
 * &#64;POST
 * &#64;Path("donations")
 * &#64;Secured  // ← AuthFilter runs before this method
 * public Response createDonation(...) { ... }
 * 
 * &#64;POST
 * &#64;Path("login")
 * // No @Secured → AuthFilter skipped
 * public Response login(...) { ... }
 * </pre>
 * 
 * <h3>Related Book Concepts:</h3>
 * <ul>
 *   <li><b>Concept</b>: Interceptor Binding with custom annotations</li>
 *   <li><b>Concept</b>: Declarative vs Programmatic Security</li>
 * </ul>
 * 
 * @author ZeroHunger Team
 * @version 1.0
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Secured {
}
