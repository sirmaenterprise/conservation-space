package com.sirma.itt.seip.rest.filters.response;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.annotations.Cache;

/**
 * Registers the {@link NoCacheResponseFilter} to all resources not having the
 * {@link Cache} annotation.
 *
 * @author yasko
 */
@Provider
public class NoCacheFilterProvider implements DynamicFeature {

	@Override
	public void configure(ResourceInfo resource, FeatureContext context) {
		Cache cache = resource.getResourceMethod().getAnnotation(Cache.class);
		if (cache == null) {
			context.register(NoCacheResponseFilter.class);
		}
	}

}
