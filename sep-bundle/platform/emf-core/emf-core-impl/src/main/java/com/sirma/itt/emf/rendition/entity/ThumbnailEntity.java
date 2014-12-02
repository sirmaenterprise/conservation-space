package com.sirma.itt.emf.rendition.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.entity.BaseStringIdEntity;

/**
 * Entity class that represent a single thumbnail. The id of the entity is a checksum of the
 * thumbnail value.
 * 
 * @author BBonev
 */
@Entity
@Table(name = "emf_thumbnailEntity")
@org.hibernate.annotations.Table(appliesTo = "emf_thumbnailEntity")
public class ThumbnailEntity extends BaseStringIdEntity {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -8428914204001776850L;

	/** The thumbnail. */
	@Column(name = "thumbnail", length = Integer.MAX_VALUE, columnDefinition = "text", nullable = true)
	private String thumbnail;

	/** The end point. */
	@Column(name = "endPoint", length = Integer.MAX_VALUE, nullable = true)
	private String endPoint;

	/** The provider name. */
	@Column(name = "providerName", length = 100, nullable = true)
	private String providerName;

	/** The retries. */
	@Column(name = "retries", nullable = true)
	private Integer retries;

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
	 * @param thumbnail the thumbnail to set
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
	 * {@inheritDoc}
	 */
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
		builder.append(", thumbnail=");
		builder.append(StringUtils.isNotNullOrEmpty(thumbnail));
		builder.append("]");
		return builder.toString();
	}
}
