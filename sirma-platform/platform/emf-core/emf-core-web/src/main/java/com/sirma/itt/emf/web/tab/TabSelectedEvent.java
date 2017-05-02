package com.sirma.itt.emf.web.tab;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * TabSelectedEvent is fired when a tab inside case is selected. Current instance object can be found in the event.
 *
 * @author svelikov
 */
@Documentation("TabSelectedEvent is fired when a tab inside case is selected. Current instance object can be found in the event.")
public class TabSelectedEvent extends AbstractInstanceEvent<Instance> {

	/**
	 * Instantiates a new tab selected event.
	 *
	 * @param instance
	 *            the instance
	 */
	public TabSelectedEvent(Instance instance) {
		super(instance);
	}

}
