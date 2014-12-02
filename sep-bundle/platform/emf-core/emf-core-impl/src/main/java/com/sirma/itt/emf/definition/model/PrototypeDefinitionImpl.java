package com.sirma.itt.emf.definition.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.sirma.itt.emf.entity.BaseEntity;

/**
 * Implementation for the {@link PrototypeDefinition}
 *
 * @author BBonev
 */
@Entity
@Table(name = "emf_prototype")
@org.hibernate.annotations.Table(appliesTo = "emf_prototype", indexes = {
		@Index(name = "idx_ptd_name", columnNames = "name"),
		@Index(name = "idx_ptd_n_c_m", columnNames = { "name", "container", "multiValued" }) })
public class PrototypeDefinitionImpl extends BaseEntity implements PrototypeDefinition {
	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -4863460171690379568L;
	/** The identifier. */
	@Column(name = "name", length = 100, nullable = false)
	private String identifier;
	/** The container. */
	@Column(name = "container", length = 100, nullable = false)
	private String container;
	/** The data type. */
	@OneToOne(cascade = { CascadeType.REFRESH }, fetch = FetchType.EAGER, targetEntity = DataType.class)
	@JoinColumn
	private DataTypeDefinition dataType;

	/** The multi valued. */
	@Column(name = "multiValued", nullable = false)
	@Type(type = "com.sirma.itt.emf.entity.customType.BooleanCustomType")
	private Boolean multiValued;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContainer() {
		return container;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataTypeDefinition getDataType() {
		return dataType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setContainer(String container) {
		this.container = container;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDataType(DataTypeDefinition typeDefinition) {
		dataType = typeDefinition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PrototypeDefinitionImpl [identifier=");
		builder.append(identifier);
		builder.append(", container=");
		builder.append(container);
		builder.append(", multiValued=");
		builder.append(multiValued);
		builder.append(", dataType=");
		builder.append(dataType);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = (prime * result) + ((container == null) ? 0 : container.hashCode());
		result = (prime * result) + ((dataType == null) ? 0 : dataType.hashCode());
		result = (prime * result) + ((identifier == null) ? 0 : identifier.hashCode());
		result = (prime * result) + ((multiValued == null) ? 0 : multiValued.hashCode());
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
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof PrototypeDefinitionImpl)) {
			return false;
		}
		PrototypeDefinitionImpl other = (PrototypeDefinitionImpl) obj;
		if (container == null) {
			if (other.container != null) {
				return false;
			}
		} else if (!container.equals(other.container)) {
			return false;
		}
		if (dataType == null) {
			if (other.dataType != null) {
				return false;
			}
		} else if (!dataType.equals(other.dataType)) {
			return false;
		}
		if (identifier == null) {
			if (other.identifier != null) {
				return false;
			}
		} else if (!identifier.equals(other.identifier)) {
			return false;
		}
		if (multiValued == null) {
			if (other.multiValued != null) {
				return false;
			}
		} else if (!multiValued.equals(other.multiValued)) {
			return false;
		}
		return true;
	}

	@Override
	public Boolean isMultiValued() {
		if (multiValued == null) {
			return Boolean.FALSE;
		}
		return multiValued;
	}

	/**
	 * Getter method for multiValued.
	 *
	 * @return the multiValued
	 */
	public Boolean getMultiValued() {
		return multiValued;
	}

	@Override
	public void setMultiValued(Boolean multiValued) {
		this.multiValued = multiValued;
	}

}
