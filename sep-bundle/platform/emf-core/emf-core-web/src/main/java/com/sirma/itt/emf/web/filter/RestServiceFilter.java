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
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet filter that will be used for applying additional data to <b>all REST services</b>.
 * 
 * @author yasko
 */
@WebFilter(urlPatterns = "/service/*", initParams = { @WebInitParam(name = "encoding", value = "utf-8") })
public class RestServiceFilter implements Filter {

	private String encoding;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {
		response.setCharacterEncoding(encoding);
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		httpResponse.setHeader("Pragma", "no-cache");
		httpResponse.setHeader("Cache-Control", "must-revalidate, max-age=0");
		httpResponse.setDateHeader("Expires", 0);
		filterChain.doFilter(request, httpResponse);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		encoding = filterConfig.getInitParameter("encoding");
	}

	@Override
	public void destroy() {

	}

}
