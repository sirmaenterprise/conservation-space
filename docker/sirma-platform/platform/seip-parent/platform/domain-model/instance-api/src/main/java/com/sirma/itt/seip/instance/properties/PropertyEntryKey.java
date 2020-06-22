package com.sirma.itt.seip.instance.properties;

/**
 * Defines a compound key for single property entry.
 *
 * @author BBonev
 */
public interface PropertyEntryKey {

	/**
	 * Getter method for propertyId.
	 *
	 * @return the propertyId
	 */
	Long getPropertyId();

	/**
	 * Setter method for propertyId.
	 *
	 * @param propertyId
	 *            the propertyId to set
	 */
	void setPropertyId(Long propertyId);

	/**
	 * Gets the list index.
	 *
	 * @return the list index
	 */
	Integer getListIndex();

	/**
	 * Sets the list index.
	 *
	 * @param listIndex
	 *            the new list index
	 */
	void setListIndex(Integer listIndex);

}