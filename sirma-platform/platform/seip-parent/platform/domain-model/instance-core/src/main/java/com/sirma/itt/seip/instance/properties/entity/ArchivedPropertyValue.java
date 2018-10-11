package com.sirma.itt.seip.instance.properties.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

/**
 * Entity class that represents a property value stored for the deleted/archived instances
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "emf_archivedPropertyValue")
@org.hibernate.annotations.Table(appliesTo = "emf_archivedPropertyValue")
public class ArchivedPropertyValue extends BasePropertyValue {
	private static final long serialVersionUID = 7900685360619759167L;
	@Column(name = "serializable", length = Integer.MAX_VALUE, nullable = false)
	private Serializable serializable;

	/**
	 * Default constructor.
	 */
	public ArchivedPropertyValue() {
		super();
	}

	/**
	 * Construct a new property value.
	 *
	 * @param propertyName
	 *            the dictionary-defined property type to store the property as
	 * @param value
	 *            the value to store. This will be converted into a format compatible with the type given
	 */
	public ArchivedPropertyValue(String propertyName, Serializable value) {
		super(propertyName, value);
	}

	@Override
	protected void storeSerializableValue(Serializable value) {
		setSerializable(cloneSerializable(value));
	}

	@Override
	protected Serializable loadSerializableValue() {
		return serializable;
	}

	/**
	 * Getter method for serializable.
	 *
	 * @return the serializable
	 */
	public Serializable getSerializable() {
		return serializable;
	}

	/**
	 * Setter method for serializable.
	 *
	 * @param serializable
	 *            the serializable to set
	 */
	public void setSerializable(Serializable serializable) {
		this.serializable = serializable;
	}

}
