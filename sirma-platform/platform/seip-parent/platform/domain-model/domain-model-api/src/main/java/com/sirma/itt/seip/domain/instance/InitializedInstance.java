package com.sirma.itt.seip.domain.instance;

import java.io.Serializable;
import java.util.Objects;

/**
 * Object used in type converters to load initialized instance when converting from {@link InstanceReference} to
 * {@link Instance}.
 *
 * @author BBonev
 */
public class InitializedInstance implements Serializable {

	private static final long serialVersionUID = -2240305276412650928L;

	/** The instance. */
	private final Instance instance;

	/**
	 * Instantiates a new initialized instance.
	 * 
	 * @param instance
	 *            the instance. Requires non null
	 */
	public InitializedInstance(Instance instance) {
		Objects.requireNonNull(instance, "Invalid initialzied instance provided. Check client code!");
		this.instance = instance;
	}

	/**
	 * Gets the initialized instance
	 * 
	 * @return the {@link Instance} specific implementation. Not null
	 */
	public Instance getInstance() {
		return instance;
	}

	@Override
	public String toString() {
		return Objects.toString(instance, "");
	}
}
