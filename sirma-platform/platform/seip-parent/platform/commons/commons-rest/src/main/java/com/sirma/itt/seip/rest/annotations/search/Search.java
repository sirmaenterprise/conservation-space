package com.sirma.itt.seip.rest.annotations.search;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.NameBinding;

/**
 * Binding annotation used to mark rest methods performing any kind of search
 * operation. Also used for binding filters and message body readers and writers
 * for search methods.
 * 
 * @author yasko
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Search {
	
}
