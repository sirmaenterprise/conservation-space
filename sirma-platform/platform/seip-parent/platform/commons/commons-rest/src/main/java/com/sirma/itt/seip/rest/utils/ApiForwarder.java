package com.sirma.itt.seip.rest.utils;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.rest.Activator;

/**
 * Forwarder for old /service endpoints to the new /api endponts.
 *
 * @author yasko
 */
// TODO: we should replace this crap with proxy server (nginx)
@WebFilter(ApiForwarder.FORWARD_PATH + "/*")
public class ApiForwarder implements Filter {

	public static final String FORWARD_PATH = "/service";

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;

		String queryString = req.getQueryString();
		StringBuilder builder = new StringBuilder(
				req.getRequestURI().replace(req.getContextPath().concat(FORWARD_PATH), Activator.ROOT_PATH));
		if (StringUtils.isNotBlank(queryString)) {
			builder.append('?').append(queryString);
		}

		request.getRequestDispatcher(builder.toString()).forward(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// not needed
	}

	@Override
	public void destroy() {
		// not needed
	}
}
