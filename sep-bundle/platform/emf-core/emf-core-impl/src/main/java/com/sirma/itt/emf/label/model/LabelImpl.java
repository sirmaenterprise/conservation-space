package com.sirma.itt.emf.label.model;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Index;

import com.sirma.itt.emf.entity.BaseEntity;
import com.sirma.itt.emf.entity.SerializableValue;
import com.sirma.itt.emf.label.LabelDefinition;

/**
 * Implementation for label definition.
 *
 * @author BBonev
 */
@Entity
@Table(name = "emf_labels")
@org.hibernate.annotations.Table(appliesTo = "emf_labels", indexes = { @Index(name = "idx_l_labelid", columnNames = "labelId") })
public class LabelImpl extends BaseEntity implements LabelDefinition, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 1907538917957126670L;

	/** The label id. */
	@Column(name = "labelId", length = 100, nullable = false)
	private String identifier;

	/** The labels. */
	@Transient
	private Map<String, String> labels;

	/** The value. */
	@JoinColumn
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private SerializableValue value;

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getLabels() {
		return labels;
	}

	/**
	 * Getter method for value.
	 *
	 * @return the value
	 */
	public SerializableValue getValue() {
		return value;
	}

	/**
	 * Setter method for value.
	 *
	 * @param value
	 *            the value to set
	 */
	public void setValue(SerializableValue value) {
		this.value = value;
	}

	/**
	 * Setter method for labels.
	 *
	 * @param labels the labels to set
	 */
	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((identifier == null) ? 0 : identifier.hashCode());
		return result;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof LabelImpl)) {
			return false;
		}
		LabelImpl other = (LabelImpl) obj;
		if (identifier == null) {
			if (other.identifier != null) {
				return false;
			}
		} else if (!identifier.equals(other.identifier)) {
			return false;
		}
		return true;
	}

	/**
	* {@inheritDoc}
	*/
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LabelImpl [id=");
		builder.append(getId());
		builder.append(", identifier=");
		builder.append(identifier);
		builder.append(", labels=");
		builder.append(labels);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}

}
