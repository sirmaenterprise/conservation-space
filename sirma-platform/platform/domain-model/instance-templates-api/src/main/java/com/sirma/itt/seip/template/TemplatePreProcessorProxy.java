package com.sirma.itt.seip.template;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * Proxy for the {@link TemplatePreProcessor} that calls all other pre processors
 *
 * @author BBonev
 */
@Singleton
public class TemplatePreProcessorProxy implements TemplatePreProcessor {

	@Inject
	@ExtensionPoint(TemplatePreProcessor.PLUGIN_NAME)
	private Plugins<TemplatePreProcessor> plugins;

	@Override
	public void process(TemplateContext context) {
		for (TemplatePreProcessor processor : plugins) {
			processor.process(context);
		}
	}
}
