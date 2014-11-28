package com.sirma.itt.faces.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.faces.application.ResourceHandler;
import javax.faces.component.UIComponent;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;

/**
 * Serves consolidated JSF resources added in the sirma-faces HeadRenderer.
 * 
 * @author Adrian Mitev
 */
@WebServlet(urlPatterns = ResourceServlet.SF_SERVLET_PATH)
public class ResourceServlet extends HttpServlet {

	private static final long serialVersionUID = -8694088296816266266L;

	public static final String COMBINE_RESOURCES_INIT_PARAM = "com.sirma.itt.faces.combineResources";

	public static final String COMPRESS_RESOURCES_INIT_PARAM = "com.sirma.itt.faces.compressResources";

	public static final String DUMMY_SF_FILE = "sf-dummy.css";

	public static final String JSF_RESOURCE_PATH = "jsf-res-path";

	private static final String CACHE_NAME = "sf-resource-cache";

	private static final String RICHFACES_RESOURCE = "/rfRes";

	public static final String RICHFACES_CSS = ".ecss";

	public static final String RICHFACES_RESLIB = ".reslib";

	public static final String SF_SERVLET_PATH = "/sf-resource";

	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ServletContext servletContext = request.getServletContext();

		// if there is an If-None-Match, the resource was previously requested,
		// tell the browser it's not modified
		String header = request.getHeader("If-None-Match");
		if (!Strings.isNullOrEmpty(header)) {
			addCacheHeaders(response);
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}

		String resourceHash = request.getParameter("resource");
		String type = request.getParameter("type");

		// extract the resource cache
		Map<String, String> cache = (Map<String, String>) servletContext.getAttribute(CACHE_NAME);
		if (cache == null) {
			cache = new HashMap<String, String>();
			servletContext.setAttribute(CACHE_NAME, cache);
		}

		String result = cache.get(resourceHash);
		// if the cache for the current cache is not initialized, initialize it
		if (result == null) {
			List<ResourceId> resourcePaths = (List<ResourceId>) servletContext
					.getAttribute(resourceHash);
			String jsfResourcePath = (String) servletContext.getAttribute(JSF_RESOURCE_PATH);

			StringBuilder resultBuilder = new StringBuilder();
			resultBuilder.append("/* Merged resources").append(System.lineSeparator());

			Set<ResourceId> paths = new LinkedHashSet<ResourceId>(resourcePaths);

			for (ResourceId resource : paths) {
				resultBuilder.append((resource.getLibrary() != null) ? resource.getLibrary() : "")
						.append("/").append(resource.getName()).append(System.lineSeparator());
			}

			resultBuilder.append("*/").append(System.lineSeparator());

			for (ResourceId resource : paths) {
				InputStream inputStream = null;

				// ecss should be evaluated using a connection to the same
				// server (small impact for localhost connections)
				if (resource.getName().endsWith(RICHFACES_CSS)) {
					inputStream = handleRichfacesCSSResource(request, resource, jsfResourcePath);
				} else {
					inputStream = getClassPathResource(resource, servletContext);
				}

				// throw exception if the resource is not found
				if (inputStream == null) {
					throw new IllegalStateException("Resource '" + resource + "' cannot be found");
				}

				// a nice way to read string from InputStream
				Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
				if (scanner.hasNext()) {
					String content = scanner.next();
					if ("css".equals(type)) {
						content = fixRelativePaths(resource.toString(), content);
					}
					resultBuilder.append(content);

					if ("js".equals(type)) {
						resultBuilder.append(";");
					}

					resultBuilder.append("\r\n");
				}
			}

			boolean compressionEnabled = Boolean.valueOf(servletContext
					.getInitParameter(COMPRESS_RESOURCES_INIT_PARAM));

			result = resultBuilder.toString();

			if ("css".equals(type)) {
				if (compressionEnabled) {
					result = new ResourceOptimizer().compressCSS(result);
				}
			} else if ("js".equals(type)) {
				if (compressionEnabled) {
					result = new ResourceOptimizer().compressJavascript(result);
				}
			}

			cache.put(resourceHash, result);
		}

		if ("css".equals(type)) {
			response.addHeader("Content-Type", "text/css");
			response.setContentType("text/css");
		} else if ("js".equals(type)) {
			response.addHeader("Content-Type", "text/javascript");
			response.setContentType("text/javascript");
		}

		addCacheHeaders(response);
		response.addHeader("ETag", resourceHash);
		response.addHeader("Content-Length", "" + result.length());

		response.getWriter().write(result);
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

	/**
	 * Locates a resource in the classpath using various mechanisms.
	 * 
	 * @param resource
	 *            the resource to find.
	 * @return {@link InputStream} of the resource or null if the resource is not found.
	 */
	private InputStream getClassPathResource(ResourceId resource, ServletContext servletContext) {
		// first look if the resource is in META-INF dir (using
		// context class loader)
		InputStream inputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("META-INF/resources/" + resource);

		// if the resource is not found in META-INF, look in pure
		// "resources" dir using the servlet context
		if (inputStream == null) {
			inputStream = servletContext.getResourceAsStream("/resources/" + resource);
		}

		// else look at the server's classloader (if the libraries
		// are in the server's classloader)
		if (inputStream == null) {
			inputStream = UIComponent.class.getClassLoader().getResourceAsStream(
					"META-INF/resources/" + resource);
		}

		if (inputStream == null) {
			Class<?> sunFacesImplClass = null;
			try {
				sunFacesImplClass = Class.forName("com.sun.faces.application.ApplicationImpl");
			} catch (ClassNotFoundException e) {
			}
			if (sunFacesImplClass != null) {
				inputStream = sunFacesImplClass.getClassLoader().getResourceAsStream(
						"META-INF/resources/" + resource);
			}
		}

		return inputStream;
	}

	/**
	 * Transforms the relative path in the CSS files to absolute path to fix issues with flattening
	 * of the CSS file structure when the resources are merged.
	 * 
	 * @param content
	 *            content of the CSS file.
	 * @return content with transformed paths.
	 */
	private String fixRelativePaths(String resourcePath, String content) {
		String result = resourcePath;

		int resourceNameIndex = resourcePath.lastIndexOf("/");
		if (resourceNameIndex != -1) {
			String resourcePathWithoutName = resourcePath.substring(0, resourceNameIndex) + "/";

			result = content.replaceAll("url\\(([\"\'])?", "url($1" + resourcePathWithoutName);
		}

		return result;
	}

	/**
	 * ECSS (css + el expressions) are evaluated by requesting them directly via http request to the
	 * resource. As the connection is performed to localhost there is not much performance impact.
	 * 
	 * @param request
	 *            http request.
	 * @param resourceId
	 *            id of the resource to request
	 * @param jsfResourcePath
	 *            path for requesting jsf resources (
	 * @return resource input stream.
	 */
	private InputStream handleRichfacesCSSResource(HttpServletRequest request,
			ResourceId resourceId, String jsfResourcePath) {
		try {
			String richfacesResourceMapping = jsfResourcePath.replace(
					ResourceHandler.RESOURCE_IDENTIFIER, RICHFACES_RESOURCE);
			URL url = new URL(request.getRequestURL().toString()
					.replace(request.getRequestURI(), "")
					.concat(String.format(richfacesResourceMapping, resourceId.toString())));
			return url.openConnection().getInputStream();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
