package com.sirma.itt.emf.domain.model;

/**
 * Defines a definition that can be identified by hash. The hash should be calculated for the whole
 * hierarchy of of the given definition and should be consistent. If any of the fields changes the
 * cache should change also.
 * 
 * @author BBonev
 */
public interface HashableDefinition {

	/**
	 * Gets the hash of the given definition if calculated.
	 * 
	 * @return the hash
	 */
	public Integer getHash();

	/**
	 * Sets the externally calculated hash for the current definition.
	 * 
	 * @param hash
	 *            the hash to set
	 */
	public void setHash(Integer hash);
}
