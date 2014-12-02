package com.sirma.itt.emf.definition.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.GenericProxy;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.entity.BaseEntity;
import com.sirma.itt.emf.entity.SerializableValue;
import com.sirma.itt.emf.serialization.KryoConvertableWrapper;

/**
 * Definition holder class. The object from the class is used for persisting single concrete
 * definition. The target definition is serialized to the database as it is.
 *
 * @author BBonev
 */
@Entity
@Table(name = "emf_definitionentry")
@org.hibernate.annotations.Table(appliesTo = "emf_definitionentry", indexes = {
		@Index(name = "idx_de_id_r", columnNames = { "identifier", "revision" }),
		@Index(name = "idx_de_hash", columnNames = { "hash" }),
		@Index(name = "idx_de_id_r_c", columnNames = { "identifier", "revision", "container" }),
		@Index(name = "idx_de_id_t_c", columnNames = { "identifier", "targetType", "container" }),
		@Index(name = "idx_de_id_t_c_r", columnNames = { "identifier", "targetType", "container", "revision" }),
		@Index(name = "idx_de_a_c_t", columnNames = { "isAbstract", "container", "targetType" }) })
public class DefinitionEntry extends BaseEntity implements TopLevelDefinition,
		GenericProxy<DefinitionModel> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 8839988787537117368L;

	/** The hash. */
	@Column(name = "hash", nullable = false)
	private Integer hash;

	/** The identifier. */
	@Column(name = "identifier", length = 100, nullable = false)
	private String identifier;

	/** The parent identifier id. */
	@Column(name = "parentIdentifierId", length = 100, nullable = true)
	private String parentIdentifierId;

	/** The dms id. */
	@Column(name = "dmsId", length = 100, nullable = true)
	private String dmsId;

	/** The container. */
	@Column(name = "container", length = 100, nullable = true)
	private String container;

	/** The Abstract. */
	@Column(name = "isAbstract", nullable = true)
	@Type(type = "com.sirma.itt.emf.entity.customType.BooleanCustomType")
	private Boolean Abstract = Boolean.FALSE;

	/** The revision. */
	@Column(name = "revision", nullable = false)
	private Long revision;

	/** The target type. */
	@JoinColumn(name = "targetType")
	@OneToOne(cascade = { CascadeType.REFRESH }, fetch = FetchType.EAGER, targetEntity = DataType.class)
	private DataTypeDefinition targetType;

	/** The target. */
	@JoinColumn
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private SerializableValue targetDefinition;

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
	public List<PropertyDefinition> getFields() {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer getHash() {
		return hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setHash(Integer hash) {
		this.hash = hash;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getParentDefinitionId() {
		return parentIdentifierId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getDmsId() {
		return dmsId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setDmsId(String dmsId) {
		this.dmsId = dmsId;
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
	public void setContainer(String container) {
		this.container = container;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isAbstract() {
		if (getAbstract() == null) {
			return false;
		}
		return getAbstract();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Long getRevision() {
		return revision;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setRevision(Long revision) {
		this.revision = revision;
	}

	/**
	 * Getter method for targetType.
	 *
	 * @return the targetType
	 */
	public DataTypeDefinition getTargetType() {
		return targetType;
	}

	/**
	 * Setter method for targetType.
	 *
	 * @param targetType the targetType to set
	 */
	public void setTargetType(DataTypeDefinition targetType) {
		this.targetType = targetType;
	}

	/**
	 * Getter method for abstract.
	 *
	 * @return the abstract
	 */
	public Boolean getAbstract() {
		return Abstract;
	}

	/**
	 * Setter method for abstract.
	 *
	 * @param _abstract the abstract to set
	 */
	public void setAbstract(Boolean _abstract) {
		Abstract = _abstract;
	}

	/**
	 * Getter method for targetDefinition.
	 *
	 * @return the targetDefinition
	 */
	public SerializableValue getTargetDefinition() {
		return targetDefinition;
	}

	/**
	 * Setter method for targetDefinition.
	 *
	 * @param targetDefinition
	 *            the targetDefinition to set
	 */
	public void setTargetDefinition(SerializableValue targetDefinition) {
		this.targetDefinition = targetDefinition;
	}

	/**
	 * Getter method for target.
	 *
	 * @return the target
	 */
	@Override
	public DefinitionModel getTarget() {
		SerializableValue definition = getTargetDefinition();
		if (definition != null) {
			Serializable serializable = definition.getSerializable();
			if (serializable != null) {
				// if the buffer is empty then we cannot restore anything from it
				if ((serializable instanceof byte[]) && (((byte[]) serializable).length == 0)) {
					return null;
				}
				return (DefinitionModel) TypeConverterUtil.getConverter()
						.convert(
						KryoConvertableWrapper.class, serializable).getTarget();
			}
		}
		return null;
	}

	@Override
	public void setTarget(DefinitionModel target) {
		SerializableValue definition = getTargetDefinition();
		if (definition == null) {
			definition = new SerializableValue();
		}
		Serializable serializable = null;
		if (target != null) {
			serializable = TypeConverterUtil.getConverter().convert(Serializable.class,
				new KryoConvertableWrapper(target));
		}
		definition.setSerializable(serializable);
		setTargetDefinition(definition);
	}

	@Override
	public DefinitionModel cloneProxy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public DefinitionModel clone() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasChildren() {
		return false;
	}

	@Override
	public Node getChild(String name) {
		return null;
	}

}
