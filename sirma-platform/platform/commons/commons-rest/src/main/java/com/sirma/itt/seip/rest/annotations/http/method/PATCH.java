package com.sirma.itt.seip.rest.annotations.http.method;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.HttpMethod;

/**
 * Bind a resource method or a filter/handler to the PATCH HTTP method.
 * 
 * @author yasko
 *
 */
@HttpMethod("PATCH")
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface PATCH {

}
