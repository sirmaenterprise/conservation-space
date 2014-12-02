package com.sirma.itt.emf.web.tab;

import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * TabSelectedEvent is fired when a tab inside case is selected. Current instance object can be
 * found in the event.
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
