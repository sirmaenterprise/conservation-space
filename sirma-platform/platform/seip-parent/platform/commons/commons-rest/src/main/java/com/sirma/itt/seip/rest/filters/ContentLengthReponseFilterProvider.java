package com.sirma.itt.seip.rest.filters;

import java.io.File;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

/**
 * Registers the {@link ContentLengthReponseFilter} to resources returning
 * {@link File}.
 *
 * @author yasko
 */
@Provider
public class ContentLengthReponseFilterProvider implements DynamicFeature {

	@Override
	public void configure(ResourceInfo resourceInfo, FeatureContext context) {
		Class<?> returnType = resourceInfo.getResourceMethod().getReturnType();
		if (returnType.isAssignableFrom(File.class)) {
			context.register(ContentLengthReponseFilter.class);
		}
	}

}
