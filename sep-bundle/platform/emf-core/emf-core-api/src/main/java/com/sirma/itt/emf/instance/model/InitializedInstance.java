package com.sirma.itt.emf.instance.model;

import java.io.Serializable;

/**
 * Object used in type converters to load initialized instance when converting from.
 * {@link InstanceReference} to {@link Instance}.
 * 
 * @author BBonev
 */
public class InitializedInstance implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2240305276412650928L;

	/** The instance. */
	private final Instance instance;

	/**
	 * Instantiates a new initialized instance.
	 * 
	 * @param instance
	 *            the instance
	 */
	public InitializedInstance(Instance instance) {
		this.instance = instance;
	}

	/**
	 * Getter method for instance.
	 * 
	 * @return the instance
	 */
	public Instance getInstance() {
		return instance;
	}

	@Override
	public String toString() {
		return "" + instance;
	}
}
