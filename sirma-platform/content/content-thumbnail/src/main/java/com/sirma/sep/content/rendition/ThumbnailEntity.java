package com.sirma.sep.content.rendition;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.model.BaseStringIdEntity;

/**
 * Entity class that represent a single thumbnail. The id of the entity is a checksum of the thumbnail value.
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Entity
@Table(name = "emf_thumbnailEntity")
@org.hibernate.annotations.Table(appliesTo = "emf_thumbnailEntity")
@NamedQueries({
		@NamedQuery(name = ThumbnailEntity.QUERY_THUMBNAILS_FOR_SYNC_KEY, query = ThumbnailEntity.QUERY_THUMBNAILS_FOR_SYNC),
		@NamedQuery(name = ThumbnailEntity.UPDATE_THUMBNAIL_DATA_KEY, query = ThumbnailEntity.UPDATE_THUMBNAIL_DATA),
		@NamedQuery(name = ThumbnailEntity.QUERY_THUMBNAIL_ENTITY_BY_ID_KEY, query = ThumbnailEntity.QUERY_THUMBNAIL_ENTITY_BY_ID),
		@NamedQuery(name = ThumbnailEntity.DELETE_THUMBNAIL_BY_SOURCE_ID_KEY, query = ThumbnailEntity.DELETE_THUMBNAIL_BY_SOURCE_ID),
		@NamedQuery(name = ThumbnailEntity.UPDATE_RESCHEDULE_THUMBNAIL_CHECK_FOR_INSTANCES_KEY, query = ThumbnailEntity.UPDATE_RESCHEDULE_THUMBNAIL_CHECK_FOR_INSTANCES) })
public class ThumbnailEntity extends BaseStringIdEntity {

	private static final long serialVersionUID = -5213071621934869089L;

	public static final String QUERY_THUMBNAIL_ENTITY_BY_ID_KEY = "QUERY_THUMBNAIL_ENTITY_BY_ID";
	static final String QUERY_THUMBNAIL_ENTITY_BY_ID = "select t from ThumbnailEntity t where t.id=:id";

	public static final String DELETE_THUMBNAIL_BY_SOURCE_ID_KEY = "DELETE_THUMBNAIL_BY_SOURCE_ID";
	static final String DELETE_THUMBNAIL_BY_SOURCE_ID = "delete from ThumbnailEntity where id=:id";

	public static final String UPDATE_THUMBNAIL_DATA_KEY = "UPDATE_THUMBNAIL_DATA";
	static final String UPDATE_THUMBNAIL_DATA = "update ThumbnailEntity set thumbnail=:thumbnail, retries = :retries, lastFailTime = :lastFailTime where id = :id OR endPoint in (select t.endPoint from ThumbnailEntity t where t.id = :id)";

	public static final String UPDATE_RESCHEDULE_THUMBNAIL_CHECK_FOR_INSTANCES_KEY = "UPDATE_RESCHEDULE_THUMBNAIL_CHECK_FOR_INSTANCES";
	static final String UPDATE_RESCHEDULE_THUMBNAIL_CHECK_FOR_INSTANCES = "update ThumbnailEntity set thumbnail= null, retries = null where id in (select tm.thumbnailId from ThumbnailMappingEntity tm where tm.instanceId in (:instanceId) AND tm.purpose = :purpose) AND (thumbnail is null or thumbnail = :thumbnail) AND lastFailTime < :lastFailTimeThreshold and endPoint is not null";

	public static final String QUERY_THUMBNAILS_FOR_SYNC_KEY = "QUERY_THUMBNAILS_FOR_SYNC";
	static final String QUERY_THUMBNAILS_FOR_SYNC = "select t.id, t.endPoint, t.providerName, t.retries from ThumbnailEntity t where t.thumbnail is null and (t.retries is null or t.retries < :retries) and (t.lastFailTime is null or t.lastFailTime < :lastFailTimeThreshold)";

	/** The thumbnail. */
	@Column(name = "thumbnail", length = Integer.MAX_VALUE, columnDefinition = "text", nullable = true)
	private String thumbnail;

	/** The end point. */
	@Column(name = "endPoint", length = 2048, nullable = true)
	private String endPoint;

	/** The provider name. */
	@Column(name = "providerName", length = 100, nullable = true)
	private String providerName;

	/** The retries. */
	@Column(name = "retries", nullable = true)
	private Integer retries;

	/** The last date when the retries reached max value */
	@Column(name = "lastFailTime", nullable = true)
	private Date lastFailTime;

	/**
	 * Getter method for thumbnail.
	 *
	 * @return the thumbnail
	 */
	public String getThumbnail() {
		return thumbnail;
	}

	/**
	 * Setter method for thumbnail.
	 *
	 * @param thumbnail
	 *            the thumbnail to set
	 */
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	/**
	 * Getter method for endPoint.
	 *
	 * @return the endPoint
	 */
	public String getEndPoint() {
		return endPoint;
	}

	/**
	 * Setter method for endPoint.
	 *
	 * @param endPoint
	 *            the endPoint to set
	 */
	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}

	/**
	 * Getter method for providerName.
	 *
	 * @return the providerName
	 */
	public String getProviderName() {
		return providerName;
	}

	/**
	 * Setter method for providerName.
	 *
	 * @param providerName
	 *            the providerName to set
	 */
	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	/**
	 * Getter method for retries.
	 *
	 * @return the retries
	 */
	public Integer getRetries() {
		return retries;
	}

	/**
	 * Setter method for retries.
	 *
	 * @param retries
	 *            the retries to set
	 */
	public void setRetries(Integer retries) {
		this.retries = retries;
	}

	/**
	 * Gets the last fail time.
	 *
	 * @return the lastFailTime
	 */
	public Date getLastFailTime() {
		return lastFailTime;
	}

	/**
	 * Sets the last fail time.
	 *
	 * @param lastFailTime
	 *            the lastFailTime to set
	 */
	public void setLastFailTime(Date lastFailTime) {
		this.lastFailTime = lastFailTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (endPoint == null ? 0 : endPoint.hashCode());
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
		if (!(obj instanceof ThumbnailEntity)) {
			return false;
		}
		ThumbnailEntity other = (ThumbnailEntity) obj;
		return nullSafeEquals(endPoint, other.endPoint);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Thumbnail [id=");
		builder.append(getId());
		builder.append(", providerName=");
		builder.append(providerName);
		builder.append(", endPoint=");
		builder.append(endPoint);
		builder.append(", retries=");
		builder.append(retries);
		builder.append(", lastFailDate=");
		builder.append(lastFailTime);
		builder.append(", thumbnail=");
		builder.append(StringUtils.isNotBlank(thumbnail));
		builder.append("]");
		return builder.toString();
	}
}
