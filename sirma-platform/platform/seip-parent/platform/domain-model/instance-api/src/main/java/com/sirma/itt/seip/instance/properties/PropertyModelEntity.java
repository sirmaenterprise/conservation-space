package com.sirma.itt.seip.instance.properties;

import java.io.Serializable;

import com.sirma.itt.seip.Entity;

/**
 * Defines minimum properties needed for storing a single property for an instance. The class defines a keys for:
 * <ul>
 * <li>the property identification
 * <li>instance identifier that owns the property
 * <li>the actual value object
 * </ul>
 * 
 * @author BBonev
 */
public interface PropertyModelEntity extends Entity<Long>, Serializable {

	/**
	 * Gets the key that identifies the property
	 *
	 * @return the key
	 */
	PropertyEntryKey getKey();

	/**
	 * Sets the key to use
	 *
	 * @param key
	 *            the new key
	 */
	void setKey(PropertyEntryKey key);

	/**
	 * Gets the entity id that identifies the instance that this property belongs to.
	 *
	 * @return the entity id
	 */
	PropertyModelKey getEntityId();

	/**
	 * Sets the entity id to use
	 *
	 * @param entityId
	 *            the new entity id
	 */
	void setEntityId(PropertyModelKey entityId);

	/**
	 * Gets the object that stores the property value.
	 *
	 * @return the value
	 */
	PropertyModelValue getValue();

	/**
	 * Sets the value.
	 *
	 * @param value
	 *            the new value
	 */
	void setValue(PropertyModelValue value);

}
