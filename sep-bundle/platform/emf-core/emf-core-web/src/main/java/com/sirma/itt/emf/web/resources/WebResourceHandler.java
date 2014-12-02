package com.sirma.itt.emf.web.resources;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.sirma.itt.emf.plugin.Plugin;

/**
 * Provides an interface for handling resources.
 * 
 * @author Adrian Mitev
 */
public interface WebResourceHandler extends Plugin {

	String TARGET_NAME = "ResourceHandler";

	/**
	 * Handles resolving of a specific resource by its path. Construct the resource that should be
	 * returned to the client.
	 * 
	 * @param path
	 *            resource path
	 * @param request
	 *            http request
	 * @param servletContext
	 *            servlet context.
	 * @return constructed resource.
	 */
	WebResource handle(String path, HttpServletRequest request, ServletContext servletContext);

	/**
	 * Checks if the current handler is able to handle the specific resource.
	 * 
	 * @param path
	 *            resource path.
	 * @param request
	 *            http request
	 * @param servletContext
	 *            servlet context.
	 * @return true if the current handler can handle the specified resource.
	 */
	boolean canHandle(String path, HttpServletRequest request, ServletContext servletContext);

}
