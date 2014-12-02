package com.sirma.itt.emf.instance.model;

/**
 * The Interface DmsAware marks an entity that has a DMS representation. The method
 * {@link #getDmsId()} should return the id in DMS. The interface does not limit the type of the
 * object/entity supported.
 * 
 * @author BBonev
 */
public interface DmsAware {
	/**
	 * Gets the DMS id.
	 * 
	 * @return the DMS id
	 */
	String getDmsId();

	/**
	 * Sets the DMS id.
	 * 
	 * @param dmsId
	 *            the new DMS id
	 */
	void setDmsId(String dmsId);
}
