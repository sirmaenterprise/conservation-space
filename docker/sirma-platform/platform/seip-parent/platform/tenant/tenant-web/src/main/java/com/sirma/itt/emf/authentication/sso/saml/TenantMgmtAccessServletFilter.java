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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.Activator;
import com.sirma.itt.seip.security.configuration.SecurityExclusion;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Simple filter to redirect system user to tenant mgmt api
 *
 * @author bbanchev
 */
@Singleton
@WebFilter(filterName = "Tenant Management Filter", urlPatterns = "/*")
public class TenantMgmtAccessServletFilter implements Filter {

	@Inject
	private SecurityContext securityContext;

	@Inject
	private SecurityExclusion exclusions;

	private String contextPath;
	private String mgmtApiPath;
	private String mgmtServicePath;
	private String restServicePath;

	@Override
	public void init(FilterConfig config) throws ServletException {
		contextPath = config.getServletContext().getContextPath();
		mgmtApiPath = contextPath + "/tenant-mgmt";
		mgmtServicePath = contextPath + "/service/tenant";
		// allow all rest services to pass via this filter
		// also only allows the new rest service path /api and not the old /service
		restServicePath = contextPath + Activator.ROOT_PATH;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (!securityContext.isActive() || !securityContext.isSystemTenant()) {
			chain.doFilter(request, response);
			return;
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String requestURI = httpRequest.getRequestURI();
		if (isNotExcluded(requestURI) && isNotManagementPath(requestURI)) {
			((HttpServletResponse) response).sendRedirect(contextPath + "/tenant-mgmt/index.html");
			return;
		}
		chain.doFilter(request, response);
	}

	private boolean isNotManagementPath(String requestURI) {
		return !(requestURI.startsWith(mgmtApiPath) || requestURI.startsWith(restServicePath)) || requestURI
				.startsWith(mgmtServicePath);
	}

	private boolean isNotExcluded(String requestURI) {
		return !exclusions.isForExclusion(requestURI.substring(contextPath.length()));
	}

	@Override
	public void destroy() {
		// nothing to destroy
	}

	/**
	 * Exclusion for all tenant management resources. It's needed otherwise all resources are redirected for login
	 *
	 * @author BBonev
	 */
	@Extension(target = SecurityExclusion.TARGET_NAME, order = 50)
	static class TenantManagementResourcesExclusion implements SecurityExclusion {

		@Override
		public boolean isForExclusion(String path) {
			return path.startsWith("/tenant-mgmt/");
		}
	}
}
