package com.sirma.itt.seip.template;

import com.sirma.itt.seip.plugin.Plugin;

/**
 * Extension point for {@link TemplateService}. The plugin will be called before template save. The extensions
 * could modify the template content before it's persisted.
 * <p>
 * If the plugin wants to modify the template instance it should be done synchronously. For asynchronous processing
 * listen for {@link BeforeTemplatePersistEvent}. The pre processing will be don before calling the before event
 *
 * @author BBonev
 */
public interface TemplatePreProcessor extends Plugin {

	/**
	 * Plugin extension point
	 */
	static String PLUGIN_NAME = "templatePreProcessor";

	/**
	 * Process a template context.
	 *
	 * @param context
	 *            the context to process
	 */
	void process(TemplateContext context);
}
