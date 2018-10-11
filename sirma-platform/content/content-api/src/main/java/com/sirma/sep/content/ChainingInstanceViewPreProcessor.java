/**
 *
 */
package com.sirma.sep.content;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Wrapping pre processor instance that calls all extension points.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ChainingInstanceViewPreProcessor implements InstanceViewPreProcessor {

	@Inject
	@ExtensionPoint(InstanceViewPreProcessor.TARGET_NAME)
	private Iterable<InstanceViewPreProcessor> preProcessors;

	@Override
	public void process(ViewPreProcessorContext context) {
		for (InstanceViewPreProcessor preProcessor : preProcessors) {
			preProcessor.process(context);
		}
	}

}
