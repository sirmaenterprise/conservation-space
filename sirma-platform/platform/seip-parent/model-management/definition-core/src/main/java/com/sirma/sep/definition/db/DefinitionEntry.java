package com.sirma.sep.definition.db;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

import com.sirma.itt.seip.GenericProxy;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.definition.TopLevelDefinition;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.model.BaseEntity;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.model.SerializableValue;
import com.sirma.itt.seip.serialization.kryo.KryoConvertableWrapper;

/**
 * Definition holder class. The object from the class is used for persisting
 * single concrete definition. The target definition is serialized to the
 * database as it is.
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "emf_definitionentry", indexes = { @Index(name = "idx_de_id_r", columnList = "identifier,revision"),
		@Index(name = "idx_de_hash", columnList = "hash"),
		@Index(name = "idx_de_id_r_c", columnList = "identifier,revision,container"),
		@Index(name = "idx_de_id_t_c", columnList = "identifier,targetType,container"),
		@Index(name = "idx_de_id_t_c_r", columnList = "identifier,targetType,container,revision"),
		@Index(name = "idx_de_a_c_t", columnList = "isAbstract,container,targetType") })
@NamedQueries({
		@NamedQuery(name = DefinitionEntry.QUERY_MAX_REVISION_OF_DEFINITIONS_BY_FILTER_KEY, query = DefinitionEntry.QUERY_MAX_REVISION_OF_DEFINITIONS_BY_FILTER),
		@NamedQuery(name = DefinitionEntry.QUERY_MAX_DEFINITION_BY_ID_AND_TYPE_KEY, query = DefinitionEntry.QUERY_MAX_DEFINITION_BY_ID),
		@NamedQuery(name = DefinitionEntry.QUERY_DEFINITION_BY_ID_REVISION_KEY, query = DefinitionEntry.QUERY_DEFINITION_BY_ID_REVISION),
		@NamedQuery(name = DefinitionEntry.DELETE_DEFINITION_BY_ID_REVISION_KEY, query = DefinitionEntry.DELETE_DEFINITION_BY_ID_REVISION),
		@NamedQuery(name = DefinitionEntry.QUERY_DEFINITION_BY_ID_EXCLUDE_REVISION_KEY, query = DefinitionEntry.QUERY_DEFINITION_BY_ID_EXCLUDE_REVISION),
		@NamedQuery(name = DefinitionEntry.QUERY_DEFINITION_BY_ID_TYPE_KEY, query = DefinitionEntry.QUERY_DEFINITION_BY_ID_TYPE),
		@NamedQuery(name = DefinitionEntry.QUERY_MAX_REVISION_OF_DEFINITIONS_FOR_MIGRATION_KEY, query = DefinitionEntry.QUERY_MAX_REVISION_OF_DEFINITIONS_FOR_MIGRATION),
		@NamedQuery(name = DefinitionEntry.QUERY_NON_INSTANTIATED_DEFINITIONS_KEY, query = DefinitionEntry.QUERY_NON_INSTANTIATED_DEFINITIONS), })
@NamedNativeQueries({
		@NamedNativeQuery(name = DefinitionEntry.QUERY_FETCH_IMPORTED_DEFINITIONS_KEY, query = DefinitionEntry.QUERY_FETCH_IMPORTED_DEFINITIONS)
})
public class DefinitionEntry extends BaseEntity implements TopLevelDefinition, GenericProxy<DefinitionModel> {

	private static final long serialVersionUID = 8839988787537117368L;

	public static final String QUERY_MAX_REVISION_OF_DEFINITIONS_BY_FILTER_KEY = "QUERY_MAX_REVISION_OF_DEFINITIONS_BY_FILTER";
	static final String QUERY_MAX_REVISION_OF_DEFINITIONS_BY_FILTER = "select c FROM DefinitionEntry c inner join fetch c.targetType where c.Abstract=:Abstract AND c.targetType.id=:type AND c.revision=(select max(c2.revision) from DefinitionEntry c2 where c2.identifier=c.identifier AND c2.targetType.id=:type)";

	public static final String QUERY_NON_INSTANTIATED_DEFINITIONS_KEY = "QUERY_NON_INSTANTIATED_DEFINITIONS";
	static final String QUERY_NON_INSTANTIATED_DEFINITIONS = "select d from DefinitionEntry d where d.identifier not in (:definitions)";

	public static final String QUERY_MAX_DEFINITION_BY_ID_AND_TYPE_KEY = "QUERY_MAX_DEFINITION_BY_ID";
	static final String QUERY_MAX_DEFINITION_BY_ID = "select c FROM DefinitionEntry c inner join fetch c.targetType where c.identifier = :identifier AND c.targetType.id=:type AND c.revision=(select max(revision) from DefinitionEntry where identifier=:identifier AND targetType.id=:type)";

	public static final String QUERY_DEFINITION_BY_ID_REVISION_KEY = "QUERY_DEFINITION_BY_ID_REVISION";
	static final String QUERY_DEFINITION_BY_ID_REVISION = "select d from DefinitionEntry d inner join fetch d.targetType where d.identifier = :identifier AND d.targetType.id=:type and d.revision=:revision order by d.id desc";

	public static final String DELETE_DEFINITION_BY_ID_REVISION_KEY = "DELETE_DEFINITION_BY_ID_REVISION";
	static final String DELETE_DEFINITION_BY_ID_REVISION = "update DefinitionEntry d set d.Abstract = 1 where d.identifier = :identifier AND d.targetType.id=:type and d.revision=:revision";

	public static final String QUERY_DEFINITION_BY_ID_EXCLUDE_REVISION_KEY = "QUERY_DEFINITION_BY_ID_EXCLUDE_REVISION";
	static final String QUERY_DEFINITION_BY_ID_EXCLUDE_REVISION = "select d.revision from DefinitionEntry d where d.identifier = :identifier AND d.targetType.id=:type and d.revision<>:revision";

	public static final String QUERY_DEFINITION_BY_ID_TYPE_KEY = "QUERY_DEFINITION_BY_ID_TYPE";
	static final String QUERY_DEFINITION_BY_ID_TYPE = "select d from DefinitionEntry d inner join fetch d.targetType where d.targetType.id=:type order by d.id desc";

	public static final String QUERY_MAX_REVISION_OF_DEFINITIONS_FOR_MIGRATION_KEY = "QUERY_MAX_REVISION_OF_DEFINITIONS_FOR_MIGRATION";
	static final String QUERY_MAX_REVISION_OF_DEFINITIONS_FOR_MIGRATION = "select distinct d.identifier, d.revision from DefinitionEntry d where d.revision = (select max(t.revision) from DefinitionEntry t where t.identifier=d.identifier and t.targetType.id=d.targetType.id) AND d.identifier in (:definitions) and d.targetType.id=:type";

	// revision 0 is a system revision and should not be fetched
	public static final String QUERY_FETCH_IMPORTED_DEFINITIONS_KEY = "QUERY_FETCH_IMPORTED_DEFINITIONS";
	static final String QUERY_FETCH_IMPORTED_DEFINITIONS = "select identifier, file_name, modified_by, modified_on, isabstract from emf_definitionentry def left join sep_definition_content on def.identifier = sep_definition_content.definition_id where id = (select max(id) from emf_definitionentry where identifier = def.identifier) and revision <> 0";

	@Column(name = "hash", nullable = false)
	private Integer hash;

	@Column(name = "identifier", length = 100, nullable = false)
	private String identifier;

	@Column(name = "parentIdentifierId", length = 100, nullable = true)
	private String parentIdentifierId;

	@Column(name = "dmsId", length = 100, nullable = true)
	private String dmsId;

	@Column(name = "container", length = 100, nullable = true)
	private String container;

	@Column(name = "isAbstract", nullable = true)
	@Type(type = "com.sirma.itt.seip.db.customtype.BooleanCustomType")
	private Boolean Abstract = Boolean.FALSE;

	@Column(name = "revision", nullable = false)
	private Long revision;

	@JoinColumn(name = "targetType")
	@OneToOne(cascade = { CascadeType.REFRESH }, fetch = FetchType.EAGER, targetEntity = DataType.class)
	private DataTypeDefinition targetType;

	@JoinColumn
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private SerializableValue targetDefinition;

	@Column(name = "modified_on", nullable = true)
	private Date modifiedOn;

	@Column(name = "modified_by", nullable = true)
	private String modifiedBy;

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public List<PropertyDefinition> getFields() {
		return Collections.emptyList();
	}

	@Override
	public Integer getHash() {
		return hash;
	}

	@Override
	public void setHash(Integer hash) {
		this.hash = hash;
	}

	@Override
	public String getParentDefinitionId() {
		return parentIdentifierId;
	}

	@Override
	public String getDmsId() {
		return dmsId;
	}

	@Override
	public void setDmsId(String dmsId) {
		this.dmsId = dmsId;
	}

	@Override
	public void setContainer(String container) {
		this.container = container;
	}

	@Override
	public String getContainer() {
		return container;
	}

	@Override
	public boolean isAbstract() {
		if (getAbstract() == null) {
			return false;
		}
		return getAbstract().booleanValue();
	}

	@Override
	public Long getRevision() {
		return revision;
	}

	@Override
	public void setRevision(Long revision) {
		this.revision = revision;
	}

	public DataTypeDefinition getTargetType() {
		return targetType;
	}

	public void setTargetType(DataTypeDefinition targetType) {
		this.targetType = targetType;
	}

	public Boolean getAbstract() {
		return Abstract;
	}

	public void setAbstract(Boolean anAbstract) {
		Abstract = anAbstract;
	}

	public SerializableValue getTargetDefinition() {
		return targetDefinition;
	}

	public void setTargetDefinition(SerializableValue targetDefinition) {
		this.targetDefinition = targetDefinition;
	}

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
				return (DefinitionModel) TypeConverterUtil.getConverter()
						.convert(KryoConvertableWrapper.class, serializable).getTarget();
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
	public DefinitionModel createCopy() {
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

	public Date getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(Date modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
}
