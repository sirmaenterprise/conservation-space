package com.sirma.sep.content.rendition;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.model.BaseEntity;
import com.sirma.itt.seip.model.DataType;

/**
 * Base Instance to thumbnail mapping.
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "emf_thumbnailmappingentity",
		indexes = { @Index(name = "idx_tme_inst_thmb", columnList = "instanceid,thumbnailid"),
				@Index(name = "idx_tme_instid_prps", columnList = "instanceid,purpose"),
				@Index(name = "idx_tme_inst_p_thmb", columnList = "instanceid,purpose,thumbnailid") })
@NamedQueries({
		@NamedQuery(name = ThumbnailMappingEntity.QUERY_THUMBNAILS_BY_IDS_KEY, query = ThumbnailMappingEntity.QUERY_THUMBNAILS_BY_IDS),
		@NamedQuery(name = ThumbnailMappingEntity.QUERY_THUMBNAIL_BY_INSTANCE_ID_AND_PURPOSE_KEY, query = ThumbnailMappingEntity.QUERY_THUMBNAIL_BY_INSTANCE_ID_AND_PURPOSE),
		@NamedQuery(name = ThumbnailMappingEntity.QUERY_THUMBNAIL_MAPPING_BY_ID_AND_PURPOSE_KEY, query = ThumbnailMappingEntity.QUERY_THUMBNAIL_MAPPING_BY_ID_AND_PURPOSE),
		@NamedQuery(name = ThumbnailMappingEntity.DELETE_THUMBNAIL_MAPPINGS_BY_SOURCE_ID_KEY, query = ThumbnailMappingEntity.DELETE_THUMBNAIL_MAPPINGS_BY_SOURCE_ID),
		@NamedQuery(name = ThumbnailMappingEntity.DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_AND_PURPOSE_KEY, query = ThumbnailMappingEntity.DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_AND_PURPOSE),
		@NamedQuery(name = ThumbnailMappingEntity.DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_KEY, query = ThumbnailMappingEntity.DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID),
		@NamedQuery(name = ThumbnailMappingEntity.QEURY_THUMBNAIL_ID_FOR_INSTANCE_KEY, query = ThumbnailMappingEntity.QEURY_THUMBNAIL_ID_FOR_INSTANCE) })
public class ThumbnailMappingEntity extends BaseEntity {

	private static final long serialVersionUID = 7609829314483547927L;

	public static final String QUERY_THUMBNAILS_BY_IDS_KEY = "QUERY_THUMBNAILS_BY_IDS";
	static final String QUERY_THUMBNAILS_BY_IDS = "select tm.instanceId, t.thumbnail from ThumbnailMappingEntity tm, ThumbnailEntity t where tm.instanceId in (:ids) AND tm.purpose = :purpose AND tm.thumbnailId = t.id and t.thumbnail is not null and t.retries is null";

	public static final String QUERY_THUMBNAIL_MAPPING_BY_ID_AND_PURPOSE_KEY = "QUERY_THUMBNAIL_MAPPING_BY_ID_AND_PURPOSE";
	static final String QUERY_THUMBNAIL_MAPPING_BY_ID_AND_PURPOSE = "select tm from ThumbnailMappingEntity tm left join fetch tm.instanceType where tm.instanceId = :id AND tm.purpose = :purpose";

	public static final String QUERY_THUMBNAIL_BY_INSTANCE_ID_AND_PURPOSE_KEY = "QUERY_THUMBNAIL_BY_INSTANCE_ID_AND_PURPOSE";
	static final String QUERY_THUMBNAIL_BY_INSTANCE_ID_AND_PURPOSE = "select t.thumbnail from ThumbnailMappingEntity tm, ThumbnailEntity t where tm.instanceId = :id AND tm.purpose = :purpose AND tm.thumbnailId = t.id";

	public static final String QEURY_THUMBNAIL_ID_FOR_INSTANCE_KEY = "QEURY_THUMBNAIL_ID_FOR_INSTANCE";
	static final String QEURY_THUMBNAIL_ID_FOR_INSTANCE = "select tm.thumbnailId from ThumbnailMappingEntity tm where tm.instanceId in (:instanceId) AND purpose = :purpose";

	public static final String DELETE_THUMBNAIL_MAPPINGS_BY_SOURCE_ID_KEY = "DELETE_THUMBNAIL_MAPPINGS_BY_SOURCE_ID";
	static final String DELETE_THUMBNAIL_MAPPINGS_BY_SOURCE_ID = "delete from ThumbnailMappingEntity where thumbnailId=:id";

	public static final String DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_AND_PURPOSE_KEY = "DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_AND_PURPOSE";
	static final String DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_AND_PURPOSE = "delete from ThumbnailMappingEntity where instanceId=:id AND purpose=:purpose";

	public static final String DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID_KEY = "DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID";
	static final String DELETE_THUMBNAIL_MAPPINGS_BY_INSTANCE_ID = "delete from ThumbnailMappingEntity where instanceId=:id";

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

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = super.hashCode();
		result = PRIME * result + (instanceId == null ? 0 : instanceId.hashCode());
		result = PRIME * result + (purpose == null ? 0 : purpose.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof ThumbnailMappingEntity)) {
			return false;
		}
		ThumbnailMappingEntity other = (ThumbnailMappingEntity) obj;
		return nullSafeEquals(instanceId, other.instanceId) && nullSafeEquals(purpose, other.purpose);
	}

}
