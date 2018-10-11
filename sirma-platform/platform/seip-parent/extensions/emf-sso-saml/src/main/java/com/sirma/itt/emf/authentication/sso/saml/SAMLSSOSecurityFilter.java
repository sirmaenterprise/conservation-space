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

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.security.configuration.SecurityExclusion;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Redirects the user to a SSO login page if not already authenticated or uses the current or provided authentication
 * info to authenticate the user.
 *
 * @author BBonev
 * @author Adrian Mitev
 */
@Singleton
@WebFilter(filterName = "Authentication Filter", servletNames = { "Faces Servlet" }, urlPatterns = "/*")
public class SAMLSSOSecurityFilter implements Filter {

	@Inject
	private SecurityContextManager securityContextManager;

	@Inject
	private SAMLRequestBuilder requestBuilder;

	@Inject
	private SecurityExclusion exclusions;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String requestURI = httpRequest.getRequestURI();
		String contextPath = httpRequest.getContextPath();

		// requested path = requested URI without the application context path
		String requestedPath = requestURI.substring(contextPath.length());

		boolean requiresAuthentication = !securityContextManager.getCurrentContext().isActive()
				&& !exclusions.isForExclusion(requestedPath);

		if (requiresAuthentication) {
			redirectToLogin((HttpServletRequest) request, (HttpServletResponse) response);
			return;
		}

		chain.doFilter(request, response);
	}

	private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String fullPath = request.getRequestURI();
		// append query string (request parameters) if available
		String queryString = request.getQueryString();
		if (StringUtils.isNotBlank(queryString)) {
			fullPath = fullPath + "?" + queryString;
		}

		response.sendRedirect(requestBuilder.build(request, fullPath));
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		// nothing to init
	}

	@Override
	public void destroy() {
		// nothing to destroy
	}

}