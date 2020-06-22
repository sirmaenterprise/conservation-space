package com.sirma.itt.seip.definition.model;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PrototypeDefinition;
import com.sirma.itt.seip.model.BaseEntity;
import com.sirma.itt.seip.model.DataType;

/**
 * Implementation for the {@link PrototypeDefinition}
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "emf_prototype", indexes = {
		@Index(name = "idx_ptd_name", columnList = "name"),
		@Index(name = "idx_ptd_n_c_m", columnList =  "name,container,multiValued") })
@NamedQuery(name = PrototypeDefinitionImpl.QUERY_PROTO_TYPE_DEFINITION_KEY, query = PrototypeDefinitionImpl.QUERY_PROTO_TYPE_DEFINITION)
public class PrototypeDefinitionImpl extends BaseEntity implements PrototypeDefinition {
	private static final long serialVersionUID = -4863460171690379568L;

	/** Query {@link PrototypeDefinitionImpl} by name, multiValue and data type id */
	public static final String QUERY_PROTO_TYPE_DEFINITION_KEY = "QUERY_PROTO_TYPE_DEFINITION";
	static final String QUERY_PROTO_TYPE_DEFINITION = "select p from PrototypeDefinitionImpl p where p.identifier=:name and p.multiValued=:multiValued and p.dataType.id=:type";

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
	@Type(type = "com.sirma.itt.seip.db.customtype.BooleanCustomType")
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
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + (container == null ? 0 : container.hashCode());
		result = PRIME * result + (dataType == null ? 0 : dataType.hashCode());
		result = PRIME * result + (identifier == null ? 0 : identifier.hashCode());
		result = PRIME * result + (multiValued == null ? 0 : multiValued.hashCode());
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
		if (!nullSafeEquals(container, other.container)
				|| !nullSafeEquals(dataType, other.dataType)
				|| !nullSafeEquals(identifier, other.identifier)) {
			return false;
		}
		return nullSafeEquals(multiValued, other.multiValued);
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
