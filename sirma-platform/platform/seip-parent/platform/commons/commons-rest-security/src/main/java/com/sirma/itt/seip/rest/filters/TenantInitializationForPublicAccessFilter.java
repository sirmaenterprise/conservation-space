package com.sirma.itt.seip.rest.filters;

import java.io.IOException;
import java.util.Objects;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Authorization filter that automatically initialize tenant context for tenant passed as query or path parameter. The
 * parameter name should be passed via constructor. <br>
 * If this class and AuthorizationFilter are added for a single resource this class should be run before the
 * AuthorizationFilter. For that reason the priority is {@code Priorities.AUTHORIZATION - 1}
 *
 * @author BBonev
 */
@Priority(Priorities.AUTHORIZATION - 1)
public class TenantInitializationForPublicAccessFilter extends AuthorizationFilter
		implements ContainerResponseFilter {

	private final String param;

	/**
	 * Instantiate new filter instance for the given security manager and tenant parameter name
	 *
	 * @param securityContextManager
	 *            the security context manager to use for context initialization
	 * @param tenantParam
	 *            parameter name to be used for tenant resolving
	 */
	public TenantInitializationForPublicAccessFilter(SecurityContextManager securityContextManager,
			String tenantParam) {
		super(securityContextManager);
		param = Objects.requireNonNull(StringUtils.trimToNull(tenantParam),
				"Paramer name for tenant resolving is required");
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		UriInfo info = requestContext.getUriInfo();
		String tenantId = info.getQueryParameters().getFirst(param);
		if (StringUtils.isBlank(tenantId)) {
			tenantId = info.getPathParameters().getFirst(param);
		}
		if (StringUtils.isNotBlank(tenantId)) {
			securityContextManager.initializeTenantContext(tenantId);
		} else {
			// if tenant parameter is expected and not specified
			// it should work as non public resource
			super.filter(requestContext);
		}
	}

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		securityContextManager.endContextExecution();
	}

}
