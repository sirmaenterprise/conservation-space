package com.sirma.itt.seip.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.internal.util.SerializationHelper;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

/**
 * Entity that represents a database table with a single blob column. The table is used to store any big serializable
 * objects.
 *
 * @author BBonev
 */
@PersistenceUnitBinding({PersistenceUnits.PRIMARY, PersistenceUnits.CORE})
@Entity
@Table(name = "emf_serializableValue")
@org.hibernate.annotations.Table(appliesTo = "emf_serializableValue")
public class SerializableValue extends BaseEntity {

	private static final long serialVersionUID = 7163930199753747810L;
	/**
	 * Magic number that is written to the stream header.
	 */
	private static final short STREAM_MAGIC = (short) 0xaced;

	
	/** The serializable. */
	@Column(name = "serializable", nullable = false)
	private byte[] serializable;

	/**
	 * Instantiates a new serializable value.
	 */
	public SerializableValue() {
		// default constructor
	}

	/**
	 * Instantiates a new serializable value.
	 *
	 * @param value
	 *            the value
	 */
	public SerializableValue(Serializable value) {
		setSerializable(value);
	}

	/**
	 * Instantiates a new serializable value.
	 *
	 * @param id
	 *            the id
	 * @param serializable
	 *            the serializable
	 */
	public SerializableValue(Long id, Serializable serializable) {
		setId(id);
		setSerializable(serializable);
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 5039;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SerializableValue) {
			return super.equals(obj);
		}
		return false;
	}

	/**
	 * Getter method for serializable.
	 *
	 * @return the serializable
	 */
	public Serializable getSerializable() {
		// check what kind of stream is the serializable
		short header = getObjectStreamHeader();
		if (header != STREAM_MAGIC) {
			return serializable;
		}
		return (Serializable) SerializationHelper.deserialize(serializable);

	}

	/**
	 * Extracts the stream header (first two bytes)
	 *
	 * @return the stream header or null if stream is empty
	 */
	private short getObjectStreamHeader() {
		if (serializable == null || serializable.length < 2) {
			return -1;
		}
		return (short) ((serializable[1] & 0xFF) + (serializable[0] << 8));
	}

	/**
	 * Setter method for serializable.
	 *
	 * @param serializable
	 *            the serializable to set
	 */
	public void setSerializable(Serializable serializable) {
		this.serializable = SerializationHelper.serialize(serializable);
	}
}
