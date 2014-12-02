package com.sirma.itt.emf.web.component;

import java.io.IOException;
import java.util.Map;

import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sun.faces.renderkit.html_basic.ScriptRenderer;

/**
 * Overrides the default renderer for h:outputScript render urls that fetch scripts directly, not
 * through the faces servlet.
 * 
 * @author Adrian Mitev
 */
public class ExtendedScriptRenderer extends ScriptRenderer {

	// JSF uses a singleton instance of the renderer, so this will be instantiated only once
	private long startupTime = System.currentTimeMillis();

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
		Map<String, Object> attributes = component.getAttributes();
		Map<Object, Object> contextMap = context.getAttributes();

		String name = (String) attributes.get("name");
		String library = (String) attributes.get("library");

		String key = name + library;

		if (null == name) {
			return;
		}

		// Ensure this script is not rendered more than once per request
		if (contextMap.containsKey(key)) {
			return;
		}
		contextMap.put(key, Boolean.TRUE);

		// Special case of scripts that have query strings
		// These scripts actually use their query strings internally, not externally
		// so we don't need the resource to know about them
		int queryPos = name.indexOf("?");
		String query = null;
		if (queryPos > -1 && name.length() > queryPos) {
			query = name.substring(queryPos + 1);
			name = name.substring(0, queryPos);
		}

		ResponseWriter writer = context.getResponseWriter();
		this.startElement(writer, component);

		if (name.equals("jsf.js")) {
			// do the complex logic only for jsf.js
			Resource resource = context.getApplication().getResourceHandler()
					.createResource(name, library);

			String resourceSrc;
			if (resource == null) {
				resourceSrc = "RES_NOT_FOUND";
			} else {
				resourceSrc = resource.getRequestPath();
				if (query != null) {
					resourceSrc = resourceSrc + ((resourceSrc.indexOf("?") > -1) ? "+" : "?")
							+ query;
				}
				resourceSrc = context.getExternalContext().encodeResourceURL(resourceSrc);
			}

			writer.writeURIAttribute("src", resourceSrc, "src");
		} else {
			String minified = (String) attributes.get("minified");

			// if in production and minified is true, add .min in the resource suffix
			if (minified != null
					&& Boolean.FALSE.equals(context.getExternalContext().getApplicationMap()
							.get(EmfConfigurationProperties.APPLICATION_MODE_DEVELOPEMENT))) {

				if (Boolean.valueOf(minified)) {
					name = new StringBuilder(name).insert(name.lastIndexOf("."), ".min").toString();
				} else {
					// if minified is not true, but still not null use it's value as resource name
					name = minified;
				}
			}

			writer.writeURIAttribute("src", context.getExternalContext().getRequestContextPath()
					+ "/" + ((library != null) ? library + "/" : "") + name + "?time="
					+ startupTime, "src");
		}
		
		this.endElement(writer);
		super.encodeEnd(context, component);
	}
}