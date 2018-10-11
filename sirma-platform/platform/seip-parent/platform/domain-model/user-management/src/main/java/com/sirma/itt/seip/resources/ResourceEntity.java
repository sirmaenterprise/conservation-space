package com.sirma.itt.seip.resources;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.customtype.BooleanCustomType;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.model.BaseStringIdEntity;

/**
 * Object that represents a single project resource in DB.
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "emf_resource", indexes = { @Index(name = "idx_p_res_name", columnList = "identifier"),
		@Index(name = "idx_p_res_name_ac", columnList = "identifier,is_active") })
@NamedQueries({
		@NamedQuery(name = ResourceEntity.QUERY_ALL_RESOURCES_BY_TYPE_KEY, query = ResourceEntity.QUERY_ALL_RESOURCES_BY_TYPE),
		@NamedQuery(name = ResourceEntity.QUERY_ALL_RESOURCES_KEY, query = ResourceEntity.QUERY_ALL_RESOURCES),
		@NamedQuery(name = ResourceEntity.CHECK_IF_RESOURCE_EXISTS_KEY, query = ResourceEntity.CHECK_IF_RESOURCE_EXISTS),
		@NamedQuery(name = ResourceEntity.QUERY_RESOURCES_BY_NAMES_AND_TYPE_KEY, query = ResourceEntity.QUERY_RESOURCES_BY_NAMES_AND_TYPE),
		@NamedQuery(name = ResourceEntity.QUERY_ALL_RESOURCE_IDS_BY_TYPE_KEY, query = ResourceEntity.QUERY_ALL_RESOURCE_IDS_BY_TYPE),
		@NamedQuery(name = ResourceEntity.QUERY_RESOURCES_BY_IDS_KEY, query = ResourceEntity.QUERY_RESOURCES_BY_IDS) })
public class ResourceEntity extends BaseStringIdEntity implements Identity {

	/** Query all {@link ResourceEntity}s */
	public static final String QUERY_ALL_RESOURCES_KEY = "QUERY_ALL_RESOURCES_KEY";
	static final String QUERY_ALL_RESOURCES = "from ResourceEntity";

	/** Query all {@link ResourceEntity}s filtered by {@code type} */
	public static final String QUERY_ALL_RESOURCES_BY_TYPE_KEY = "QUERY_ALL_RESOURCES_BY_TYPE";
	static final String QUERY_ALL_RESOURCES_BY_TYPE = "select r from ResourceEntity r where r.type = :type";

	/** Query the system ids of the resource entities that match the given {@code type} */
	public static final String QUERY_ALL_RESOURCE_IDS_BY_TYPE_KEY = "QUERY_ALL_RESOURCE_IDS_BY_TYPE";
	static final String QUERY_ALL_RESOURCE_IDS_BY_TYPE = "select id from ResourceEntity where type = :type";

	/** Checks if resource with the given id exists in the system. */
	public static final String CHECK_IF_RESOURCE_EXISTS_KEY = "CHECK_IF_RESOURCE_EXISTS";
	static final String CHECK_IF_RESOURCE_EXISTS = "select count(p.id) from ResourceEntity p where lower(p.identifier) = lower(:identifier) or lower(p.id) = lower(:identifier)";

	/** Query {@link ResourceEntity} that has name in one of the given {@code identifier} and {@code type} */
	public static final String QUERY_RESOURCES_BY_NAMES_AND_TYPE_KEY = "QUERY_RESOURCES_BY_NAMES_AND_TYPE";
	static final String QUERY_RESOURCES_BY_NAMES_AND_TYPE = "select p from ResourceEntity p where lower(p.identifier) in (:identifier) and p.type=:type";

	/** Query {@link ResourceEntity}s that have the given system {@code ids} */
	public static final String QUERY_RESOURCES_BY_IDS_KEY = "QUERY_RESOURCES_BY_IDS";
	static final String QUERY_RESOURCES_BY_IDS = "select p from ResourceEntity p where p.id in (:ids)";

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7820910159110494389L;

	/** The identifier. */
	@Column(name = "identifier", length = 100, nullable = false)
	private String identifier;

	/** The display name. */
	@Column(name = "displayName", length = 200, nullable = true)
	private String displayName;

	@Column(name = "type", nullable = true)
	private Integer type;

	@Column(name = "is_active")
	@Type(type = BooleanCustomType.TYPE_NAME)
	private Boolean active;

	@Column(name = "source", length = 100, nullable = true)
	private String source;

	@Column(name = "definition_id", length = 100, nullable = true)
	private String definitionId;

	/**
	 * Gets the display name.
	 *
	 * @return the display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the display name.
	 *
	 * @param displayName
	 *            the new display name
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

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
	 * @return the type
	 */
	public Integer getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(Integer type) {
		this.type = type;
	}

	/**
	 * @return the active
	 */
	public Boolean getActive() {
		if (active == null) {
			return Boolean.TRUE;
		}
		return active;
	}

	/**
	 * @param active
	 *            the active to set
	 */
	public void setActive(Boolean active) {
		this.active = active;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the definitionId
	 */
	public String getDefinitionId() {
		return definitionId;
	}

	/**
	 * @param definitionId
	 *            the definitionId to set
	 */
	public void setDefinitionId(String definitionId) {
		this.definitionId = definitionId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ResourceEntity [identifier=");
		builder.append(identifier);
		builder.append(", displayName=");
		builder.append(displayName);
		builder.append(", type=");
		builder.append(type);
		builder.append(", source=");
		builder.append(source);
		builder.append(", isActive=");
		builder.append(active);
		builder.append("]");
		return builder.toString();
	}

}
