package com.sirma.itt.emf.authentication.sso.saml;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Filters out all requests to the /tenant-mgmt/ resources and if the user is not a user from the
 * system tenant, redirects him to ui2 or if ui2 url is not set, to the main page.
 * 
 * @author nvelkov
 */
@Singleton
@WebFilter(filterName = "Tenant Management Permissions Filter", urlPatterns = "/tenant-mgmt/*")
public class TenantMgmtPermissionsFilter implements Filter {

	@Inject
	private SecurityContext securityContext;

	@Inject
	private SystemConfiguration systemConfiguration;

	private String contextPath;

	@Override
	public void init(FilterConfig config) throws ServletException {
		contextPath = config.getServletContext().getContextPath();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (!securityContext.isSystemTenant()) {
			if (systemConfiguration.getUi2Url().isSet()) {
				((HttpServletResponse) response).sendRedirect(systemConfiguration.getUi2Url().get());
			} else {
				((HttpServletResponse) response).sendRedirect(contextPath);
			}
			return;
		}
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// nothing to destroy
	}

}
