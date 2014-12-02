package com.sirma.itt.emf.rendition.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.entity.BaseEntity;

/**
 * Base Instance to thumbnail mapping.
 * 
 * @author BBonev
 */
@Entity
@Table(name = "emf_thumbnailmappingentity")
@org.hibernate.annotations.Table(appliesTo = "emf_thumbnailmappingentity", indexes = {
		@Index(name = "idx_tme_inst_thmb", columnNames = { "instanceid", "thumbnailid" }),
		@Index(name = "idx_tme_instid_prps", columnNames = { "instanceId", "purpose" }),
		@Index(name = "idx_tme_inst_p_thmb", columnNames = { "instanceid", "purpose", "thumbnailid" }) })
public class ThumbnailMappingEntity extends BaseEntity {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 7609829314483547927L;

	/** The instance id. */
	@Column(name = "instanceId", length = 100, nullable = false)
	private String instanceId;

	/** The instance type. */
	@OneToOne(cascade = { CascadeType.REFRESH }, targetEntity = DataType.class)
	@JoinColumn(name = "instanceType")
	private DataTypeDefinition instanceType;

	/** The thumbnail id. */
	@Column(name = "thumbnailId", length = 100, nullable = true)
	private String thumbnailId;

	/** The purpose. */
	@Column(name = "purpose", length = 50, nullable = true)
	private String purpose;

	/**
	 * Getter method for instanceId.
	 * 
	 * @return the instanceId
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * Setter method for instanceId.
	 * 
	 * @param instanceId
	 *            the instanceId to set
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * Getter method for instanceType.
	 * 
	 * @return the instanceType
	 */
	public DataTypeDefinition getInstanceType() {
		return instanceType;
	}

	/**
	 * Setter method for instanceType.
	 * 
	 * @param instanceType
	 *            the instanceType to set
	 */
	public void setInstanceType(DataTypeDefinition instanceType) {
		this.instanceType = instanceType;
	}

	/**
	 * Getter method for thumbnailId.
	 * 
	 * @return the thumbnailId
	 */
	public String getThumbnailId() {
		return thumbnailId;
	}

	/**
	 * Setter method for thumbnailId.
	 * 
	 * @param thumbnailId
	 *            the thumbnailId to set
	 */
	public void setThumbnailId(String thumbnailId) {
		this.thumbnailId = thumbnailId;
	}

	/**
	 * Getter method for purpose.
	 * 
	 * @return the purpose
	 */
	public String getPurpose() {
		return purpose;
	}

	/**
	 * Setter method for purpose.
	 * 
	 * @param purpose
	 *            the purpose to set
	 */
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

}
