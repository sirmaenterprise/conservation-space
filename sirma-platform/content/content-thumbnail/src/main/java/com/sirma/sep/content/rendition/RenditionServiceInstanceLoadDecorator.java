/**
 *
 */
package com.sirma.sep.content.rendition;

import java.util.Collection;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.rendition.RenditionService;

/**
 * Concrete implementation of the InstanceLoadDecorator. The implementation loads thumbnails to the passed instances.
 * This extension is collected and called in the concrete implementation of InstanceLoadDecorator -
 * ChaningInstanceLoadDecorator.
 *
 * @author A. Kunchev
 */
@Extension(target = InstanceLoadDecorator.TARGET_NAME, enabled = true, order = 10)
public class RenditionServiceInstanceLoadDecorator implements InstanceLoadDecorator {

	@Inject
	private RenditionService renditionService;

	@Override
	public <I extends Instance> void decorateInstance(I instance) {
		renditionService.loadThumbnail(instance);
	}

	@Override
	public <I extends Instance> void decorateResult(Collection<I> collection) {
		renditionService.loadThumbnails(collection);
	}

}
