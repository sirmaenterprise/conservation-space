/**
 *
 */
package com.sirma.sep.content;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

/**
 * Internal boundary object that carry information about persisted content. These objects are produced by the
 * {@link ContentStore} and carry information about the uploaded content.
 *
 * @author BBonev
 */
public class StoreItemInfo implements Serializable {

	private static final long serialVersionUID = 6788801638889717383L;

	/** The remote id. This id is specific for the content store that produced it. */
	@Tag(1)
	private String remoteId;

	/** The provider type. */
	@Tag(2)
	private String providerType;

	/** The additional data. */
	@Tag(3)
	private Serializable additionalData;

	/** The content length. */
	@Tag(4)
	private long contentLength = -1L;

	/**
	 * The content type of the stored content. This field is not required to be filled by the store but could be used to
	 * provide information to the store
	 */
	@Tag(5)
	private String contentType;

	private transient boolean isModified = false;

	/**
	 * Instantiates a new store item info.
	 */
	public StoreItemInfo() {
		// default constructor
	}

	/**
	 * Instantiates a new store item info.
	 *
	 * @param remoteId
	 *            the remote id
	 * @param providerType
	 *            the provider type
	 * @param contentLength
	 *            the content length
	 * @param contentType
	 *            the content type
	 * @param additionalData
	 *            the additional data
	 */
	public StoreItemInfo(String remoteId, String providerType, long contentLength, String contentType,
			Serializable additionalData) {
		this.remoteId = remoteId;
		this.providerType = providerType;
		this.contentLength = contentLength;
		this.contentType = contentType;
		this.additionalData = additionalData;
	}

	/**
	 * Gets the remote id. This id is specific for the content store that produced it.
	 *
	 * @return the remoteId
	 */
	public String getRemoteId() {
		return remoteId;
	}

	/**
	 * Sets the remote the id.
	 *
	 * @param remoteId
	 *            the remote id
	 * @return the current instance
	 */
	public StoreItemInfo setRemoteId(String remoteId) {
		isModified |= !nullSafeEquals(this.remoteId, remoteId);
		this.remoteId = remoteId;
		return this;
	}

	/**
	 * Gets the provider type.
	 *
	 * @return the providerType
	 */
	public String getProviderType() {
		return providerType;
	}

	/**
	 * Sets the provider type.
	 *
	 * @param providerType
	 *            the providerType to set
	 * @return the current instance
	 */
	public StoreItemInfo setProviderType(String providerType) {
		isModified |= !nullSafeEquals(this.providerType, providerType);
		this.providerType = providerType;
		return this;
	}

	/**
	 * Gets the additional data.
	 *
	 * @return the additionalData
	 */
	public Serializable getAdditionalData() {
		return additionalData;
	}

	/**
	 * Sets the additional data.
	 *
	 * @param additionalData
	 *            the additionalData to set
	 * @return the current instance
	 */
	public StoreItemInfo setAdditionalData(Serializable additionalData) {
		isModified = true;
		this.additionalData = additionalData;
		return this;
	}

	/**
	 * Gets the content length.
	 *
	 * @return the contentLength
	 */
	public long getContentLength() {
		return contentLength;
	}

	/**
	 * Sets the content length.
	 *
	 * @param contentLength
	 *            the contentLength to set
	 * @return the current instance
	 */
	public StoreItemInfo setContentLength(long contentLength) {
		isModified |= this.contentLength != contentLength;
		this.contentLength = contentLength;
		return this;
	}

	/**
	 * Gets the content type of the content represented by the current info. This may be provided to the content store
	 * and is not required from the store to return any values.
	 *
	 * @return the contentLength
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Sets the content type.
	 *
	 * @param contentType
	 *            the content type to set
	 * @return the current instance
	 */
	public StoreItemInfo setContentType(String contentType) {
		isModified |= !nullSafeEquals(this.contentType, contentType);
		this.contentType = contentType;
		return this;
	}

	/**
	 * Checks if the current store item info was modified during the last check.
	 *
	 * @return true, if is modified
	 */
	protected boolean isModified() {
		return isModified;
	}
}
