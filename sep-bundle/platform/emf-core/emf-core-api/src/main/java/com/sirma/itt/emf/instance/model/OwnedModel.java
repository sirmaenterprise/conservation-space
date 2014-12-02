package com.sirma.itt.emf.instance.model;

/**
 * Model interface to mark a node that it has a parent that owns him.
 * 
 * @author BBonev
 */
public interface OwnedModel {

	/**
	 * Gets the owning/parent reference.
	 * 
	 * @return the owning reference
	 */
	InstanceReference getOwningReference();

	/**
	 * Sets the owning reference.
	 * 
	 * @param reference
	 *            the new owning reference
	 */
	void setOwningReference(InstanceReference reference);

	/**
	 * Gets the owning instance. The method should return cached fully loaded instance that is
	 * represented by the {@link InstanceReference} returned by {@link #getOwningReference()}
	 * 
	 * @return the owning instance or <code>null</code> if the {@link #getOwningReference()} returns
	 *         <code>null</code>.
	 */
	Instance getOwningInstance();

	/**
	 * Sets the owning instance for the current model.
	 * 
	 * @param instance
	 *            the new owning instance
	 */
	void setOwningInstance(Instance instance);
}
