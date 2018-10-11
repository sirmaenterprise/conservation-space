package com.sirma.itt.seip.instance.properties.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseEntity;

/**
 * Entity that represents a database table with information about instanceId, propertyId and richtext value. The table
 * is used to store values containing html.
 *
 * @author S.Djulgerova
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "sep_richtextproperties")
@NamedQueries(value = {
		@NamedQuery(name = RichtextPropertyEntity.QUERY_RICHTEXT_PROPERTY_KEY, query = RichtextPropertyEntity.QUERY_RICHTEXT_PROPERTY),
		@NamedQuery(name = RichtextPropertyEntity.QUERY_RICHTEXT_PROPERTIES_KEY, query = RichtextPropertyEntity.QUERY_RICHTEXT_PROPERTIES),
		@NamedQuery(name = RichtextPropertyEntity.QUERY_RICHTEXT_PROPERTIES_BY_IDS_KEY, query = RichtextPropertyEntity.QUERY_RICHTEXT_PROPERTIES_BY_IDS),
		@NamedQuery(name = RichtextPropertyEntity.DELETE_RICHTEXT_PROPERTIES_KEY, query = RichtextPropertyEntity.DELETE_RICHTEXT_PROPERTIES) })
public class RichtextPropertyEntity extends BaseEntity {

	private static final long serialVersionUID = 106890152010872170L;

	/** Query {@link RichtextPropertyEntity} by given instance id and property id. Params: instanceId and propertyId */
	public static final String QUERY_RICHTEXT_PROPERTY_KEY = "QUERY_PROPERTY_RICHTEXT";
	static final String QUERY_RICHTEXT_PROPERTY = "select rp from RichtextPropertyEntity rp where rp.instanceId=:instanceId and rp.propertyId=:propertyId";

	/** Query from {@link RichtextPropertyEntity} by given instance id. Params: instanceId */
	public static final String QUERY_RICHTEXT_PROPERTIES_KEY = "QUERY_RICHTEXT_PROPERTIES";
	static final String QUERY_RICHTEXT_PROPERTIES = "select rp from RichtextPropertyEntity rp where rp.instanceId=:instanceId";

	/** Query from {@link RichtextPropertyEntity} by given instance ids. Params: ids */
	public static final String QUERY_RICHTEXT_PROPERTIES_BY_IDS_KEY = "QUERY_RICHTEXT_PROPERTIES_BY_IDS";
	static final String QUERY_RICHTEXT_PROPERTIES_BY_IDS = "select rp from RichtextPropertyEntity rp where rp.instanceId in (:ids)";

	/** Delete from {@link RichtextPropertyEntity} by given instance id. Param: instanceId */
	public static final String DELETE_RICHTEXT_PROPERTIES_KEY = "DELETE_PROPERTY_RICHTEXT";
	static final String DELETE_RICHTEXT_PROPERTIES = "delete from RichtextPropertyEntity where instanceId=:instanceId";

	@Column(name = "content")
	private String content;

	@Column(name = "instanceid", nullable = false)
	private String instanceId;

	@Column(name = "propertyid", nullable = false)
	private Long propertyId;

	/**
	 * Instantiates a new entity.
	 */
	public RichtextPropertyEntity() {
		// default constructor
	}

	/**
	 * Instantiates a new entity.
	 * 
	 * @param instanceId
	 *            instance id
	 * @param propertyId
	 *            property id
	 * @param content
	 *            richtext value
	 */
	public RichtextPropertyEntity(String instanceId, Long propertyId, String content) {
		setInstanceId(instanceId);
		setPropertyId(propertyId);
		setContent(content);
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 37;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RichtextPropertyEntity) {
			return super.equals(obj);
		}
		return false;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public Long getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(Long propertyId) {
		this.propertyId = propertyId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
