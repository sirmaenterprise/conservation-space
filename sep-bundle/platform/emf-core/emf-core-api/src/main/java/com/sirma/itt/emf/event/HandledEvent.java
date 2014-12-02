package com.sirma.itt.emf.event;

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
	public boolean isHandled();

	/**
	 * Setter method for handled.
	 * 
	 * @param handled
	 *            the handled to set
	 */
	public void setHandled(boolean handled);
}
