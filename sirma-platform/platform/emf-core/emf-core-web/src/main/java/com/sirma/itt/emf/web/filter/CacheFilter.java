/**
 * Copyright (c) 2014 19.06.2014 , Sirma ITT. /* /**
 */
package com.sirma.itt.emf.web.filter;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sirma.itt.emf.web.resources.WebResourceServlet;

/**
 * Adds headers to enable browsers to cache resources. For more info look here -
 * http://yuiblog.com/blog/2007/01/04/performance-research-part-2/.
 *
 * @author Adrian Mitev
 */
@WebFilter(urlPatterns = { "*.png", "*.jpg", "*.gif", "*.jpeg", "*.js", "*.css", "*.woff", "*.eot" })
public class CacheFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		if (!httpRequest.getRequestURI().contains(WebResourceServlet.SERVLET_PATH)
				&& httpResponse.getHeader("Expires") == null) {
			// one month in the future
			httpResponse.addHeader("Expires", new Date(System.currentTimeMillis() - 2592000 * 1000).toString());
			// one month in the past
			httpResponse.addHeader("Last-Modified", new Date(System.currentTimeMillis() + 2592000 * 1000).toString());

			httpResponse.addHeader("Cache-Control", "public, max-age=2592000");
		}

		chain.doFilter(request, httpResponse);
	}

	@Override
	public void destroy() {
		//
	}

}
