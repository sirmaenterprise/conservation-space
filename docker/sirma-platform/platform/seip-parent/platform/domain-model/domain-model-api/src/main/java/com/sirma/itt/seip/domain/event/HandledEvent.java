package com.sirma.itt.seip.domain.event;

import com.sirma.itt.seip.event.EmfEvent;

/**
 * Event that cares if has been handled or not
 *
 * @author BBonev
 */
public interface HandledEvent extends EmfEvent {

	/**
	 * Getter method for handled.
	 *
	 * @return the handled
	 */
	boolean isHandled();

	/**
	 * Setter method for handled.
	 *
	 * @param handled
	 *            the handled to set
	 */
	void setHandled(boolean handled);
}
