package com.sirma.itt.seip.mail;

import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;

/**
 * Context aware template loader.
 *
 * @see StringTemplateLoader
 * @author nvelkov
 */
public interface ContextualTemplateLoader extends TemplateLoader {

	/**
	 * Puts a template into the loader.
	 *
	 * @see StringTemplateLoader#putTemplate(String, String)
	 * @param name
	 *            the name of the template.
	 * @param templateSource
	 *            the source code of the template.
	 */
	public void putTemplate(String name, String templateSource);
}
