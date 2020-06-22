/**
 *
 */
package com.sirma.itt.seip.instance.content.view;

import javax.inject.Inject;

import org.jsoup.nodes.Document;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.InstanceViewPreProcessor;
import com.sirma.sep.content.ViewPreProcessorContext;
import com.sirma.sep.content.idoc.sanitizer.IdocSanitizer;

/**
 * Pre processor that sanitize and update the content of saved views. This should be one of the first pre processors
 * that run.
 *
 * @author BBonev
 */
@Extension(target = InstanceViewPreProcessor.TARGET_NAME, order = 1)
public class InstanceViewSanitazingPreProcessor implements InstanceViewPreProcessor {

	@Inject
	private IdocSanitizer sanitizer;

	@Override
	public void process(ViewPreProcessorContext context) {
		if (context.isViewPresent()) {
			Document sanitized = sanitizer.sanitize(context.getParsedView(), null);
			context.updateView(sanitized);
		}
	}
}
