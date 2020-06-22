package com.sirma.itt.seip.model;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.sirma.itt.seip.domain.instance.OwnedModel;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;

/**
 * Generic instance entity. It defines the following database fields.
 * <ul>
 * <li>id character varying(128) NOT NULL,
 * <li>instancetype bigint, - the instance type
 * <li>owninginstanceid character varying(50),
 * <li>owninginstancetype bigint,
 * <li>definitionid character varying(128),
 * <li>definitionrevision bigint,
 * <li>definitionpath character varying(512), - definition path in complex definitions (documents/sections/workflow
 * tasks)
 * <li>dmsid character varying(128), - general dms id or workflow/task instance id in dms
 * <li>cmid character varying(128),
 * <li>treepath character varying(2048), // for tasks/subtasks
 * <li>tenatid character varying(128)
 * </ul>
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "seip_instanceentity")
@org.hibernate.annotations.Table(appliesTo = "seip_instanceentity", indexes = {})
@AssociationOverrides(value = {
		@AssociationOverride(name = "owning.referenceType", joinColumns = @JoinColumn(name = "owninginstancetype", nullable = true)) })
@NamedQueries({
		@NamedQuery(name = InstanceEntity.QUERY_ALL_INSTANCE_IDS_BY_TYPE_KEY, query = InstanceEntity.QUERY_ALL_INSTANCE_IDS_BY_TYPE),
		@NamedQuery(name = InstanceEntity.QUERY_INSTANCE_ENTITIES_BY_ID_KEY, query = InstanceEntity.QUERY_INSTANCE_ENTITIES_BY_ID),
		@NamedQuery(name = InstanceEntity.QUERY_INSTANCE_BY_DMS_KEY, query = InstanceEntity.QUERY_INSTANCE_BY_DMS),
		@NamedQuery(name = InstanceEntity.QUERY_INSTANCE_ENTITIES_BY_CM_ID_KEY, query = InstanceEntity.QUERY_INSTANCE_ENTITIES_BY_CM_ID),
		@NamedQuery(name = InstanceEntity.QUERY_INSTANCE_ENTITIES_BY_DMS_ID_KEY, query = InstanceEntity.QUERY_INSTANCE_ENTITIES_BY_DMS_ID) })
public class InstanceEntity extends BaseStringIdEntity implements OwnedModel<LinkSourceId> {// NOSONAR

	/** Query all instance ids for given instance type. */
	public static final String QUERY_ALL_INSTANCE_IDS_BY_TYPE_KEY = "QUERY_ALL_INSTANCE_IDS_BY_TYPE";
	static final String QUERY_ALL_INSTANCE_IDS_BY_TYPE = "select id from InstanceEntity where instanceType=:instanceType";

	/** Query {@link InstanceEntity} by given collection if ids. */
	public static final String QUERY_INSTANCE_ENTITIES_BY_ID_KEY = "QUERY_INSTANCE_ENTITIES_BY_ID";
	static final String QUERY_INSTANCE_ENTITIES_BY_ID = "select c from InstanceEntity c left join fetch c.owning where c.id in (:id)";

	/** Query {@link InstanceEntity} by dms id. */
	public static final String QUERY_INSTANCE_BY_DMS_KEY = "QUERY_INSTANCE_BY_DMS";
	static final String QUERY_INSTANCE_BY_DMS = "select t from InstanceEntity t left join fetch t.owning where t.dmsId=:dmsId";

	/** Query {@link InstanceEntity}s by dms ids. */
	public static final String QUERY_INSTANCE_ENTITIES_BY_DMS_ID_KEY = "QUERY_INSTANCE_ENTITIES_BY_DMS_ID";
	static final String QUERY_INSTANCE_ENTITIES_BY_DMS_ID = "select c from InstanceEntity c left join fetch c.owning where c.dmsId in (:dmsId)";

	/** The query instance entities by cm id key. */
	public static final String QUERY_INSTANCE_ENTITIES_BY_CM_ID_KEY = "QUERY_INSTANCE_ENTITIES_BY_CM_ID";
	static final String QUERY_INSTANCE_ENTITIES_BY_CM_ID = "select c from InstanceEntity c left join fetch c.owning where c.cmId in (:cmId)";

	/**
	 * Query instance ids by instance types and owning instance id. Effectively fetches the children of particular type
	 * for given instance
	 */
	public static final String QUERY_INSTANCE_ID_BY_INSTANCE_TYPES_AND_OWNING_INSTANCE_KEY = "QUERY_INSTANCE_ID_BY_INSTANCE_TYPES_AND_OWNING_INSTANCE";
	static final String QUERY_INSTANCE_ID_BY_INSTANCE_TYPES_AND_OWNING_INSTANCE = "select ie.id from InstanceEntity ie left join fetch ie.owning where ie.instanceType in (:instanceType) and ie.owning.id =:owningInstanceId";

	private static final long serialVersionUID = 4913538268620205107L;
	@Column(name = "index", nullable = true, insertable = false, updatable = false)
	private Long index;
	@Column(name = "instancetype")
	private Long instanceType;
	@Column(name = "definitionid", length = 128)
	private String definitionId;
	@Column(name = "definitionrevision")
	private Long definitionRevision;
	@Column(name = "definitionpath", length = 512)
	private String definitionPath;
	@Column(name = "dmsid", length = 128)
	private String dmsId;
	@Column(name = "cmid", length = 128)
	private String cmId;
	@Column(name = "contextpath", length = 2048)
	private String treePath;
	@Column(name = "tenantid", length = 128)
	private String tenantId;
	@AttributeOverrides(value = {
			@AttributeOverride(name = "id", column = @Column(name = "owninginstanceid", length = 128, nullable = true)) })
	private LinkSourceId owning;

	/**
	 * Getter method for instanceType.
	 *
	 * @return the instanceType
	 */
	public Long getInstanceType() {
		return instanceType;
	}

	/**
	 * Setter method for instanceType.
	 *
	 * @param instanceType
	 *            the instanceType to set
	 */
	public void setInstanceType(Long instanceType) {
		this.instanceType = instanceType;
	}

	/**
	 * Getter method for definitionId.
	 *
	 * @return the definitionId
	 */
	public String getDefinitionId() {
		return definitionId;
	}

	/**
	 * Setter method for definitionId.
	 *
	 * @param definitionId
	 *            the definitionId to set
	 */
	public void setDefinitionId(String definitionId) {
		this.definitionId = definitionId;
	}

	/**
	 * Getter method for definitionRevision.
	 *
	 * @return the definitionRevision
	 */
	public Long getDefinitionRevision() {
		return definitionRevision;
	}

	/**
	 * Setter method for definitionRevision.
	 *
	 * @param definitionRevision
	 *            the definitionRevision to set
	 */
	public void setDefinitionRevision(Long definitionRevision) {
		this.definitionRevision = definitionRevision;
	}

	/**
	 * Getter method for definitionPath.
	 *
	 * @return the definitionPath
	 */
	public String getDefinitionPath() {
		return definitionPath;
	}

	/**
	 * Setter method for definitionPath.
	 *
	 * @param definitionPath
	 *            the definitionPath to set
	 */
	public void setDefinitionPath(String definitionPath) {
		this.definitionPath = definitionPath;
	}

	/**
	 * Getter method for dmsId.
	 *
	 * @return the dmsId
	 */
	public String getDmsId() {
		return dmsId;
	}

	/**
	 * Setter method for dmsId.
	 *
	 * @param dmsId
	 *            the dmsId to set
	 */
	public void setDmsId(String dmsId) {
		this.dmsId = dmsId;
	}

	/**
	 * Getter method for cmid.
	 *
	 * @return the cmid
	 */
	public String getCmId() {
		return cmId;
	}

	/**
	 * Setter method for cmid.
	 *
	 * @param cmid
	 *            the cmid to set
	 */
	public void setCmId(String cmid) {
		cmId = cmid;
	}

	/**
	 * Getter method for treePath.
	 *
	 * @return the treePath
	 */
	public String getTreePath() {
		return treePath;
	}

	/**
	 * Setter method for treePath.
	 *
	 * @param treePath
	 *            the treePath to set
	 */
	public void setTreePath(String treePath) {
		this.treePath = treePath;
	}

	/**
	 * Getter method for tenantId.
	 *
	 * @return the tenantId
	 */
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * Setter method for tenantId.
	 *
	 * @param tenantId
	 *            the tenantId to set
	 */
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * Getter method for owning.
	 *
	 * @return the owning
	 */
	@Override
	public LinkSourceId getOwning() {
		return owning;
	}

	/**
	 * Setter method for owning.
	 *
	 * @param owning
	 *            the owning to set
	 */
	@Override
	public void setOwning(LinkSourceId owning) {
		this.owning = owning;
	}

	/**
	 * Getter method for index.
	 *
	 * @return the index
	 */
	public Long getIndex() {
		return index;
	}

	/**
	 * Setter method for index.
	 *
	 * @param index
	 *            the index to set
	 */
	public void setIndex(Long index) {
		this.index = index;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(512);
		builder
				.append("InstanceEntity [id=")
					.append(getId())
					.append(", instanceType=")
					.append(instanceType)
					.append(", definitionId=")
					.append(definitionId)
					.append(", definitionRevision=")
					.append(definitionRevision)
					.append(", definitionPath=")
					.append(definitionPath)
					.append(", dmsId=")
					.append(dmsId)
					.append(", cmId=")
					.append(cmId)
					.append(", treePath=")
					.append(treePath)
					.append(", tenantId=")
					.append(tenantId)
					.append(", owning=")
					.append(owning)
					.append("]");
		return builder.toString();
	}

}
