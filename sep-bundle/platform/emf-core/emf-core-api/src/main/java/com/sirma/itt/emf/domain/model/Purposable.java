package com.sirma.itt.emf.domain.model;

/**
 * Interface that defines a means to filer objects/definitions by purpose.
 * 
 * @author BBonev
 */
public interface Purposable {

	/**
	 * Gets the purpose.
	 * 
	 * @return the purpose
	 */
	String getPurpose();

	/**
	 * Sets the purpose.
	 * 
	 * @param purpose
	 *            the new purpose
	 */
	void setPurpose(String purpose);

}
