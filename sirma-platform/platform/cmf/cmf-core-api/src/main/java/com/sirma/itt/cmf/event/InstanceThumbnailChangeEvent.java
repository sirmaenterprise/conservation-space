package com.sirma.itt.cmf.event;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * The Class DocumentThumbnailChangeEvent used for notify for thumbnail change.
 *
 * @author Hristo Lungov
 */
public class InstanceThumbnailChangeEvent extends AbstractInstanceEvent<Instance> {

	/**
	 * Instantiates a new document change event.
	 *
	 * @param instance
	 *            the instance
	 */
	public InstanceThumbnailChangeEvent(Instance instance) {
		super(instance);
	}

}
