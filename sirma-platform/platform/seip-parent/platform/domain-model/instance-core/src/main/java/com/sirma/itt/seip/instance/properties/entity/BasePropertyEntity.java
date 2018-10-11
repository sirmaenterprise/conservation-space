package com.sirma.itt.seip.instance.properties.entity;

import javax.persistence.Embedded;
import javax.persistence.MappedSuperclass;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.instance.properties.PropertyEntryKey;
import com.sirma.itt.seip.instance.properties.PropertyModelEntity;
import com.sirma.itt.seip.instance.properties.PropertyModelKey;
import com.sirma.itt.seip.model.BaseEntity;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Base class for implementing property saving. The class defines the instance key and the entity id
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@MappedSuperclass
public abstract class BasePropertyEntity extends BaseEntity implements PropertyModelEntity {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 564386242692672969L;
	/** The key. */
	@Embedded
	protected PropertyKey key;
	/** The target entity. */
	@Embedded
	protected EntityId entityId;

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	@Override
	public PropertyEntryKey getKey() {
		return key;
	}

	/**
	 * Sets the key.
	 *
	 * @param key
	 *            the new key
	 */
	@Override
	public void setKey(PropertyEntryKey key) {
		this.key = (PropertyKey) key;
	}

	/**
	 * Getter method for entityId.
	 *
	 * @return the entityId
	 */
	@Override
	public EntityId getEntityId() {
		return entityId;
	}

	/**
	 * Setter method for entityId.
	 *
	 * @param entityId
	 *            the entityId to set
	 */
	@Override
	public void setEntityId(PropertyModelKey entityId) {
		this.entityId = (EntityId) entityId;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + (entityId == null ? 0 : entityId.hashCode());
		result = PRIME * result + (key == null ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof BasePropertyEntity)) {
			return false;
		}
		BasePropertyEntity other = (BasePropertyEntity) obj;
		if (!EqualsHelper.nullSafeEquals(entityId, other.entityId)) {
			return false;
		}
		return EqualsHelper.nullSafeEquals(key, other.key);
	}

}
