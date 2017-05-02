package com.sirma.itt.seip.rest.secirity;

import javax.inject.Inject;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import com.sirma.itt.seip.rest.annotations.security.AdminResource;
import com.sirma.itt.seip.rest.filters.AdminFilter;

/**
 * JAX-RS {@link DynamicFeature} responsible for registering the {@link AdminFilter} to resources marked with
 * {@link com.sirma.itt.seip.rest.annotations.security.AdminResource}.
 *
 * @author smustafov
 */
@Provider
public class AdminFilterFeature implements DynamicFeature {

	@Inject
	private AdminFilter filter;

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		if (resourceInfo.getResourceClass().isAnnotationPresent(AdminResource.class)
				|| resourceInfo.getResourceMethod().isAnnotationPresent(AdminResource.class)) {
			context.register(filter);
		}
	}

}
