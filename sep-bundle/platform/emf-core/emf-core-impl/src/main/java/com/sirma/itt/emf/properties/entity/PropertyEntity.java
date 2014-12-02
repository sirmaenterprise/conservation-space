package com.sirma.itt.emf.properties.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.sirma.itt.emf.entity.BaseEntity;

/**
 * Bean to convey <b>emf_properties</b> data.
 * 
 * @author BBonev
 */
@Entity
@Table(name = "emf_properties")
@org.hibernate.annotations.Table(appliesTo = "emf_properties", indexes = {
		@Index(name = "idx_prE_propertyId", columnNames = "propertyId"),
		@Index(name = "idx_prE_propertyId_index", columnNames = { "propertyId", "listIndex" }),
		@Index(name = "idx_prE_entityId_index", columnNames = { "bean_id", "bean_type" })})
public class PropertyEntity extends BaseEntity implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4516465742517264790L;

	/** The key. */
	@Embedded
	private PropertyKey key;

	/** The value. */
	@OneToOne(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true)
	@OnDelete(action = OnDeleteAction.CASCADE) // when the entity is deleted we remove also the value
	private PropertyValue value;

	/** The target entity. */
	@Embedded
	private EntityId entityId;

	/**
	 * Required default constructor.
	 */
	public PropertyEntity() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "PropertyEntity [id=" + getId() + ", entityId=" + entityId + ", key=" + key
				+ ", value=" + value + "]";
	}

	/**
	 * Gets the key.
	 *
	 * @return the key
	 */
	public PropertyKey getKey() {
		return key;
	}

	/**
	 * Sets the key.
	 *
	 * @param key
	 *            the new key
	 */
	public void setKey(PropertyKey key) {
		this.key = key;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public PropertyValue getValue() {
		return value;
	}

	/**
	 * Sets the value.
	 *
	 * @param value
	 *            the new value
	 */
	public void setValue(PropertyValue value) {
		this.value = value;
	}

	/**
	 * Getter method for entityId.
	 *
	 * @return the entityId
	 */
	public EntityId getEntityId() {
		return entityId;
	}

	/**
	 * Setter method for entityId.
	 *
	 * @param entityId the entityId to set
	 */
	public void setEntityId(EntityId entityId) {
		this.entityId = entityId;
	}

}