package com.sirma.itt.seip.rest.secirity;

import javax.inject.Inject;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.rest.annotations.security.PublicResource;
import com.sirma.itt.seip.rest.filters.AuthorizationFilter;
import com.sirma.itt.seip.rest.filters.TenantInitializationForPublicAccessFilter;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * JAX-RS {@link DynamicFeature} responsible for registering the {@link AuthorizationFilter} to non public resources.
 *
 * @author yasko
 */
@Provider
public class AuthorizationRegistrationFeature implements DynamicFeature {

	@Inject
	private AuthorizationFilter filter;

	@Inject
	private SecurityContextManager securityContextManager;

	@Override
	public void configure(ResourceInfo resource, FeatureContext context) {
		PublicResource publicResource = resource.getResourceMethod().getAnnotation(PublicResource.class);
		if (publicResource == null) {
			context.register(filter);
		} else if (StringUtils.isNotBlank(publicResource.tenantParameterName())) {
			context.register(new TenantInitializationForPublicAccessFilter(securityContextManager,
					publicResource.tenantParameterName()));
		}
	}

}
