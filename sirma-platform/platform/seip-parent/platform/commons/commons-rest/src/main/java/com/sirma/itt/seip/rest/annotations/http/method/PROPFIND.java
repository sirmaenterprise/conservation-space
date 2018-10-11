package com.sirma.itt.seip.rest.annotations.http.method;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.ws.rs.HttpMethod;

/**
 * Defines an JAX-RS HTTP method annotation for the DAV protocol method PROPFIND.
 *
 * @since 2017-03-24
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@HttpMethod("PROPFIND")
public @interface PROPFIND {
	// nothing to add
}
