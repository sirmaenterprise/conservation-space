package com.sirma.itt.emf.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

/**
 * Character encoding filter.
 * 
 * @author svelikov
 */
@WebFilter(urlPatterns = "/*", initParams = { @WebInitParam(name = "encoding", value = "UTF-8") })
public class EncodingFilter implements Filter {

	/** The encoding. */
	private String encoding = "utf-8";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		request.setCharacterEncoding(encoding);
		filterChain.doFilter(request, response);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String encodingParam = filterConfig.getInitParameter("encoding");
		if (encodingParam != null) {
			encoding = encodingParam;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void destroy() {
		// nothing to do
	}

}