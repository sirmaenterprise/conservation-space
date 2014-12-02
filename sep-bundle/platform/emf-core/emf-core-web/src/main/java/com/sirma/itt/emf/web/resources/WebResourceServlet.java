package com.sirma.itt.emf.web.resources;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.plugin.ExtensionPoint;

/**
 * Provides access to static and dynamic resources like scripts, css and images by using a set of
 * plugged {@link WebResourceHandler} classes.
 * 
 * @author Adrian Mitev
 */
@WebServlet(urlPatterns = WebResourceServlet.SERVLET_PATH + "/*")
public class WebResourceServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public static final String SERVLET_PATH = "/emfRes";

	@Inject
	@ExtensionPoint(WebResourceHandler.TARGET_NAME)
	private Iterable<WebResourceHandler> handlers;

	private ServletContext servletContext;

	private String resourceServletPath;

	private Map<String, WebResource> resourceCache;

	@Override
	public void init(ServletConfig config) throws ServletException {
		resourceCache = new HashMap<>();
		servletContext = config.getServletContext();

		resourceServletPath = servletContext.getContextPath() + SERVLET_PATH;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setCharacterEncoding("UTF-8");

		String resourcePath = request.getRequestURI().substring(resourceServletPath.length() + 1);

		WebResource resource = resourceCache.get(resourcePath);
		if (resource == null) {
			for (WebResourceHandler handler : handlers) {
				if (handler.canHandle(resourcePath, request, servletContext)) {
					resource = handler.handle(resourcePath, request, servletContext);
					if (resource.isCachable()) {
						resourceCache.put(resourcePath, resource);
					}
				}
			}
		}

		if (resource != null) {
			// check if the resource is not changed
			String ifNoneMatchHeader = request.getHeader("If-None-Match");
			if (!StringUtils.isNullOrEmpty(ifNoneMatchHeader)
					&& ifNoneMatchHeader.equals(resource.getHash())) {
				addCacheHeaders(response);

				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}

			addCacheHeaders(response);

			response.addHeader("ETag", resource.getHash());
			addCacheHeaders(response);
			response.addHeader("Content-Length", "" + resource.getContent().length);
			response.setContentType(resource.getContentType());

			response.getOutputStream().write(resource.getContent());
		} else {
			response.sendError(404);
		}
	}

	/**
	 * Adds headers that instructs the browser to cache the resource reponse.
	 * 
	 * @param response
	 *            http response.
	 */
	private void addCacheHeaders(HttpServletResponse response) {
		// one month from now
		response.addHeader("Last-Modified",
				new Date(System.currentTimeMillis() + (2592000 * 1000)).toString());
		// one month in the past
		response.addHeader("Expires",
				new Date(System.currentTimeMillis() - (2592000 * 1000)).toString());
		response.addHeader("Cache-Control", "public, max-age=2592000");
	}
}
