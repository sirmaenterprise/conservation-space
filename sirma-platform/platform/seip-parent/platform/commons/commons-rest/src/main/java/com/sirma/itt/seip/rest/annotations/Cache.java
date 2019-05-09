package com.sirma.itt.seip.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.NameBinding;

import com.sirma.itt.seip.rest.cache.CacheHandler;
import com.sirma.itt.seip.rest.filters.CacheFilter;

/**
 * {@link NameBinding} annotation for configuring caching on a resource. The
 * annotation should be placed on resource <strong>methods</strong> that support
 * caching. The could be placed on a TYPE, but it will have no effect. It is
 * used on a type when a filter provider needs to be matched - see
 * {@link CacheFilter}.
 *
 * The annotation's value indicates a {@link CacheHandler} implementation which
 * is called on request/response to provide cache headers or to validate if a
 * resource has changed.
 *
 * @author yasko
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Cache {

	/**
	 * {@link CacheHandler} implementation used to add cache headers or to check
	 * if a resource has canged.
	 *
	 * @return {@link NoopCacheHandler} by default, or the provided
	 *         implementation.
	 */
	Class<? extends CacheHandler> value() default CacheHandler.class;
}
