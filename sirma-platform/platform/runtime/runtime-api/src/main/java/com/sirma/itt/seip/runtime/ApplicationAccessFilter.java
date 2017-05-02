package com.sirma.itt.seip.runtime;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.runtime.boot.StartupPhase;

/**
 * Restricts the application access before it is fully initialized. Until {@link RuntimeInfo} is updated with
 * {@link StartupPhase#STARTUP_COMPLETE}, the filter ignores all request send to the application.
 * 
 * After the application has been initialized, the filter no longer restricts the access and passes the remote request
 * down the filter chain.
 * 
 * This restriction is designed to allow the application to startup undisturbed from remote calls.
 * 
 * @author Mihail Radkov
 * @see RuntimeInfo
 */
@WebFilter(filterName = "Application Access Filter", urlPatterns = "/*")
public class ApplicationAccessFilter implements Filter {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// Nothing to initialize.
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (RuntimeInfo.isStarted()) {
			chain.doFilter(request, response);
		} else {
			if (response instanceof HttpServletResponse) {
				((HttpServletResponse) response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			}
			LOGGER.warn("Application is still initializing and therefore not accessible.");
		}
	}

	@Override
	public void destroy() {
		// Nothing to destroy.
	}
}
