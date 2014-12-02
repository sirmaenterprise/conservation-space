/**
 * Copyright (c) 2013 22.11.2013 , Sirma ITT. /* /**
 */
package com.sirma.itt.emf.web.component;

import java.io.IOException;
import java.util.Map;

import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sun.faces.renderkit.html_basic.StylesheetRenderer;

/**
 * Overrides the default renderer for h:outputStylesheet render urls that fetch css files directly,
 * not through the faces servlet.
 * 
 * @author Adrian Mitev
 */
public class ExtendedStylesheetRenderer extends StylesheetRenderer {

	// JSF uses a singleton instance of the renderer, so this will be instantiated only once
	private long startupTime = System.currentTimeMillis();

	@Override
	public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

		Map<String, Object> attributes = component.getAttributes();
		Map<Object, Object> contextMap = context.getAttributes();

		String name = (String) attributes.get("name");
		String library = (String) attributes.get("library");
		String key = name + library;

		String media = (String) attributes.get("media");

		if (null == name) {
			return;
		}

		// Ensure this stylesheet is not rendered more than once per request
		if (contextMap.containsKey(key)) {
			return;
		}
		contextMap.put(key, Boolean.TRUE);

		Resource resource = context.getApplication().getResourceHandler()
				.createResource(name, library);

		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("link", component);
		writer.writeAttribute("type", "text/css", "type");
		writer.writeAttribute("rel", "stylesheet", "rel");
		String resourceUrl = "RES_NOT_FOUND";
		if (resource != null) {
			resourceUrl = context.getExternalContext().encodeResourceURL(resource.getRequestPath());
			if (resourceUrl.contains("javax.faces.resource")) {
				String minified = (String) attributes.get("minified");

				// if in production and minified is true, add .min in the resource suffix
				if (minified != null
						&& Boolean.FALSE.equals(context.getExternalContext().getApplicationMap()
								.get(EmfConfigurationProperties.APPLICATION_MODE_DEVELOPEMENT))) {

					if (Boolean.valueOf(minified)) {
						name = new StringBuilder(name).insert(name.lastIndexOf("."), ".min")
								.toString();
					} else {
						// if minified is not true, but still not null use it's value as resource
						// name
						name = minified;
					}
				}

				resourceUrl = context.getExternalContext().getRequestContextPath() + "/"
						+ (library != null ? library + "/" : "") + name + "?time=" + startupTime;
			}
		}
		writer.writeURIAttribute("href", resourceUrl, "href");
		if (media != null) {
			writer.writeAttribute("media", media, "media");
		}
		writer.endElement("link");
		super.encodeEnd(context, component);
	}
}
