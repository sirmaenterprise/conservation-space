package com.sirma.itt.seip.definition.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

import com.sirma.itt.seip.GenericProxy;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.TopLevelDefinition;
import com.sirma.itt.seip.model.BaseEntity;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.model.SerializableValue;
import com.sirma.itt.seip.serialization.kryo.KryoConvertableWrapper;

/**
 * Definition holder class. The object from the class is used for persisting single concrete definition. The target
 * definition is serialized to the database as it is.
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
@NamedQueries({
		@NamedQuery(name = DefinitionEntry.QUERY_ALL_DEFINITIONS_FILTERED_KEY, query = DefinitionEntry.QUERY_ALL_DEFINITIONS_FILTERED),
		@NamedQuery(name = DefinitionEntry.QUERY_MAX_DEFINITION_BY_ID_KEY, query = DefinitionEntry.QUERY_MAX_DEFINITION_BY_ID),
		@NamedQuery(name = DefinitionEntry.QUERY_DEFINITION_BY_ID_REVISION_KEY, query = DefinitionEntry.QUERY_DEFINITION_BY_ID_REVISION),
		@NamedQuery(name = DefinitionEntry.DELETE_DEFINITION_BY_ID_REVISION_KEY, query = DefinitionEntry.DELETE_DEFINITION_BY_ID_REVISION),
		@NamedQuery(name = DefinitionEntry.QUERY_DEFINITION_BY_ID_EXCLUDE_REVISION_KEY, query = DefinitionEntry.QUERY_DEFINITION_BY_ID_EXCLUDE_REVISION),
		@NamedQuery(name = DefinitionEntry.QUERY_DEFINITION_BY_ID_TYPE_KEY, query = DefinitionEntry.QUERY_DEFINITION_BY_ID_TYPE),
		@NamedQuery(name = DefinitionEntry.QUERY_MAX_REVISION_OF_DEFINITIONS_FOR_MIGRATION_KEY, query = DefinitionEntry.QUERY_MAX_REVISION_OF_DEFINITIONS_FOR_MIGRATION),
		@NamedQuery(name = DefinitionEntry.QUERY_NON_INSTANTIATED_DEFINITIONS_KEY, query = DefinitionEntry.QUERY_NON_INSTANTIATED_DEFINITIONS), })
public class DefinitionEntry extends BaseEntity implements TopLevelDefinition, GenericProxy<DefinitionModel> {

	private static final long serialVersionUID = 8839988787537117368L;

	/**
	 * The Constant QUERY_ALL_DEFINITIONS_FILTERED_KEY. Fetches max revision definitions by Abstract, container and type
	 */
	public static final String QUERY_ALL_DEFINITIONS_FILTERED_KEY = "QUERY_ALL_DEFINITIONS_FILTERED";
	static final String QUERY_ALL_DEFINITIONS_FILTERED = "select c FROM DefinitionEntry c inner join fetch c.targetType where c.Abstract=:Abstract AND c.targetType.id=:type AND c.revision=(select max(c2.revision) from DefinitionEntry c2 where c2.identifier=c.identifier AND c2.targetType.id=:type)";

	/** The Constant QUERY_NON_INSTANTIATED_DEFINITIONS_KEY. */
	public static final String QUERY_NON_INSTANTIATED_DEFINITIONS_KEY = "QUERY_NON_INSTANTIATED_DEFINITIONS";
	static final String QUERY_NON_INSTANTIATED_DEFINITIONS = "select d from DefinitionEntry d where d.identifier not in (:definitions)";

	/**
	 * The Constant QUERY_MAX_DEFINITION_BY_ID_KEY. Fetches max definition by id, container and type
	 */
	public static final String QUERY_MAX_DEFINITION_BY_ID_KEY = "QUERY_MAX_DEFINITION_BY_ID";
	static final String QUERY_MAX_DEFINITION_BY_ID = "select c FROM DefinitionEntry c inner join fetch c.targetType where c.identifier = :identifier AND c.targetType.id=:type AND c.revision=(select max(revision) from DefinitionEntry where identifier=:identifier AND targetType.id=:type)";

	/**
	 * The Constant QUERY_DEFINITION_BY_ID_AND_CONTAINER_KEY. Fetches a definition entry by id, container and type
	 */
	public static final String QUERY_DEFINITION_BY_ID_REVISION_KEY = "QUERY_DEFINITION_BY_ID_REVISION";
	static final String QUERY_DEFINITION_BY_ID_REVISION = "select d from DefinitionEntry d inner join fetch d.targetType where d.identifier = :identifier AND d.targetType.id=:type and d.revision=:revision order by d.id desc";
	/**
	 * The Constant QUERY_DEFINITION_BY_ID_AND_CONTAINER_KEY. Fetches a definition entry by id, container and type
	 */
	public static final String DELETE_DEFINITION_BY_ID_REVISION_KEY = "DELETE_DEFINITION_BY_ID_REVISION";
	static final String DELETE_DEFINITION_BY_ID_REVISION = "delete from DefinitionEntry d where d.identifier = :identifier AND d.targetType.id=:type and d.revision=:revision";

	public static final String QUERY_DEFINITION_BY_ID_EXCLUDE_REVISION_KEY = "QUERY_DEFINITION_BY_ID_EXCLUDE_REVISION";
	static final String QUERY_DEFINITION_BY_ID_EXCLUDE_REVISION = "select d.revision from DefinitionEntry d where d.identifier = :identifier AND d.targetType.id=:type and d.revision<>:revision";
	/**
	 * The Constant QUERY_DEFINITION_BY_ID_AND_CONTAINER_KEY. Fetches a definition entry by id, container and type
	 */
	public static final String QUERY_DEFINITION_BY_ID_TYPE_KEY = "QUERY_DEFINITION_BY_ID_TYPE";
	static final String QUERY_DEFINITION_BY_ID_TYPE = "select d from DefinitionEntry d inner join fetch d.targetType where d.targetType.id=:type order by d.id desc";

	public static final String QUERY_MAX_REVISION_OF_DEFINITIONS_FOR_MIGRATION_KEY = "QUERY_MAX_REVISION_OF_DEFINITIONS_FOR_MIGRATION";
	static final String QUERY_MAX_REVISION_OF_DEFINITIONS_FOR_MIGRATION = "select distinct d.identifier, d.revision from DefinitionEntry d where d.revision = (select max(t.revision) from DefinitionEntry t where t.identifier=d.identifier and t.targetType.id=d.targetType.id) AND d.identifier in (:definitions) and d.targetType.id=:type";

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
	@Type(type = "com.sirma.itt.seip.db.customtype.BooleanCustomType")
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
		return getAbstract().booleanValue();
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
	 * @param targetType
	 *            the targetType to set
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
	 * @param _abstract
	 *            the abstract to set
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
				if (serializable instanceof byte[] && ((byte[]) serializable).length == 0) {
					return null;
				}
				return (DefinitionModel) TypeConverterUtil
						.getConverter()
							.convert(KryoConvertableWrapper.class, serializable)
							.getTarget();
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

	@Override
	public String getType() {
		return null;
	}

}
