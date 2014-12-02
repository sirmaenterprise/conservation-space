package com.sirma.itt.emf.web.event;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * AbstractWebEvent can be used in the web implementation when navigation may be changed in the
 * event handler.
 * 
 * @param <I>
 *            instance for which this event is fired
 * @author svelikov
 */
public abstract class AbstractWebEvent<I extends Instance> implements EmfEvent {

	/** The instance object for which this event was fired. */
	private I instance;

	/** Navigation string to be used after event. */
	private String navigation;

	/**
	 * Instantiates a new abstract instance event.
	 * 
	 * @param instance
	 *            the instance
	 * @param navigation
	 *            the navigation
	 */
	public AbstractWebEvent(I instance, String navigation) {
		this.setInstance(instance);
		this.setNavigation(navigation);
	}

	/**
	 * Getter method for instance.
	 * 
	 * @return the instance
	 */
	public I getInstance() {
		return instance;
	}

	/**
	 * Setter method for instance.
	 * 
	 * @param instance
	 *            the instance to set
	 */
	public void setInstance(I instance) {
		this.instance = instance;
	}

	/**
	 * Getter method for navigation.
	 * 
	 * @return the navigation
	 */
	public String getNavigation() {
		return navigation;
	}

	/**
	 * Setter method for navigation.
	 * 
	 * @param navigation
	 *            the navigation to set
	 */
	public void setNavigation(String navigation) {
		this.navigation = navigation;
	}

}
