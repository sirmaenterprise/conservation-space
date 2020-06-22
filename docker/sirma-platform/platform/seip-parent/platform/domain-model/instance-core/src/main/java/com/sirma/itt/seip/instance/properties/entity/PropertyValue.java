package com.sirma.itt.seip.instance.properties.entity;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.SerializableValue;

/**
 * Entity class that represents a property value stored for the active instances
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "emf_propertyValue")
@org.hibernate.annotations.Table(appliesTo = "emf_propertyValue")
public class PropertyValue extends BasePropertyValue {

	private static final long serialVersionUID = 706855803484421261L;

	/** The serializable value stored in a separate table */
	@OneToOne(cascade = { CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private SerializableValue serializableValue;

	/**
	 * Default constructor.
	 */
	public PropertyValue() {
		super();
	}

	/**
	 * Construct a new property value.
	 *
	 * @param typeName
	 *            the dictionary-defined property type to store the property as
	 * @param value
	 *            the value to store. This will be converted into a format compatible with the type given
	 */
	public PropertyValue(String typeName, Serializable value) {
		super(typeName, value);
	}

	/**
	 * Gets the serializable value.
	 *
	 * @return the serializable value
	 */
	public SerializableValue getSerializableValue() {
		return serializableValue;
	}

	/**
	 * Sets the serializable value.
	 *
	 * @param value
	 *            the new serializable value
	 */
	public void setSerializableValue(SerializableValue value) {
		serializableValue = value;
	}

	@Override
	protected void storeSerializableValue(Serializable value) {
		setSerializableValue(cloneSerializable(new SerializableValue(value)));
	}

	@Override
	protected Serializable loadSerializableValue() {
		return getSerializableValue().getSerializable();
	}
}
