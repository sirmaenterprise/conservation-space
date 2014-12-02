package com.sirma.itt.emf.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.User;

/**
 * Clears the current security context from the current thread if left.
 * 
 * @author BBonev
 */
@WebFilter(urlPatterns = "/*")
public class ClearSecurityContextFilter implements Filter {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ClearSecurityContextFilter.class);
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {

		User authentication = SecurityContextManager.getFullAuthentication();
		if (authentication != null) {
			LOGGER.debug("Cleared stale authentication of user " + authentication);
		}
		SecurityContextManager.clearCurrentSecurityContext();

		filterChain.doFilter(request, response);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// nothing to do
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
		// nothing to do
	}

}