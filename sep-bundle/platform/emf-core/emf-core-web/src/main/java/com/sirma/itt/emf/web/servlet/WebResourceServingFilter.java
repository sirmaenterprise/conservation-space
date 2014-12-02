package com.sirma.itt.emf.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;

/**
 * Listens for all requests. Checks if the current request is a request for a static file located in
 * the root web folder and serves it directly. This is performed because if a file with a specific
 * path is initially located in META-INF/resources of a jar it is cached by the container and
 * servlet directly without checking if a file with such path is added later in the root web folder
 * of the app and that breaks the hot deploy.<br/>
 * WARNING: Should be used for development purposes only!!<br/>
 * 
 * @author Adrian Mitev
 */
public class WebResourceServingFilter implements Filter {

	private ServletContext servletContext;

	private String realPath;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		servletContext = filterConfig.getServletContext();
		realPath = servletContext.getRealPath("/");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String resourcePath = httpRequest.getRequestURI().substring(
				servletContext.getContextPath().length());

		File file = new File(realPath + "/" + resourcePath);
		// read the file if exists and can be read
		if (file.isFile() && file.exists() && file.canRead()) {
			try (FileInputStream fileStream = new FileInputStream(file)) {
				// set proper content type
				if (resourcePath.endsWith(".css")) {
					response.setContentType("text/css");
				} else if (resourcePath.endsWith(".js")) {
					response.setContentType("text/js");
				} else if (resourcePath.endsWith(".png")) {
					response.setContentType("image/png");
				} else if (resourcePath.endsWith(".jpg")) {
					response.setContentType("image/jpg");
				} else if (resourcePath.endsWith(".gif")) {
					response.setContentType("image/gif");
				} else if (resourcePath.endsWith(".bmp")) {
					response.setContentType("image/bmp");
				}

				IOUtils.copy(fileStream, response.getOutputStream());
				response.getOutputStream().flush();
				response.getOutputStream().close();
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {

	}

}
