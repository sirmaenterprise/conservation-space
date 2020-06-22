package com.sirma.itt.seip.domain;

/**
 * Marks the implemented object as an instance that is container/tenant aware and provides tenant/container affinity.
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

	/**
	 * Set container for the given object if it implements the {@link TenantAware} interface
	 * 
	 * @param target
	 *            instance to update
	 * @param container
	 *            the container to set
	 */
	static void setContainer(Object target, String container) {
		if (target instanceof TenantAware) {
			((TenantAware) target).setContainer(container);
		}
	}
}
