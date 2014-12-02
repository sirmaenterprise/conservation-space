package com.sirma.itt.emf.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Entity that represents a database table with a single blob column. The table is used to store any
 * big serializable objects.
 * 
 * @author BBonev
 */
@Entity
@Table(name = "emf_serializableValue")
@org.hibernate.annotations.Table(appliesTo = "emf_serializableValue")
public class SerializableValue implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -2601337475345740924L;

	/** The id. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** The serializable. */
	@Column(name = "serializable", length = Integer.MAX_VALUE, nullable = false)
	private Serializable serializable;

	/**
	 * Instantiates a new serializable value.
	 */
	public SerializableValue() {
		// default constructor
	}

	/**
	 * Instantiates a new serializable value.
	 *
	 * @param value the value
	 */
	public SerializableValue(Serializable value) {
		serializable = value;
	}

	/**
	 * Instantiates a new serializable value.
	 *
	 * @param id the id
	 * @param serializable the serializable
	 */
	public SerializableValue(Long id, Serializable serializable) {
		this.id = id;
		this.serializable = serializable;
	}

	/**
	 * Getter method for id.
	 *
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * Setter method for id.
	 *
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
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
	 * @param serializable the serializable to set
	 */
	public void setSerializable(Serializable serializable) {
		this.serializable = serializable;
	}
}
