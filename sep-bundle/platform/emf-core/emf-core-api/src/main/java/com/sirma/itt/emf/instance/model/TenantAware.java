package com.sirma.itt.emf.instance.model;

/**
 * Marks the implemented object as an instance that is container/tenant aware and provides
 * tenant/container affinity.
 * 
 * @author BBonev
 */
public interface TenantAware {

	/**
	 * Gets the container.
	 * 
	 * @return the container
	 */
	String getContainer();

	/**
	 * Sets the container.
	 * 
	 * @param container
	 *            the new container
	 */
	void setContainer(String container);
}
