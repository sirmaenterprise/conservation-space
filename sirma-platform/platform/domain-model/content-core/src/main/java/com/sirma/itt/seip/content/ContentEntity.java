package com.sirma.itt.seip.content;

import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.db.customtype.BooleanCustomType;
import com.sirma.itt.seip.model.BaseStringIdEntity;
import com.sirma.itt.seip.model.SerializableValue;

/**
 * Entity class that maps the internal instances and their associated content locations. The entity uses a string id so
 * that we can refer globally to the concrete content without the need to know the instance id and the content purpose.
 *
 * @author BBonev
 */
@Entity
@Table(name = "seip_content", indexes = {
		@Index(name = "idx_con_ins_t", columnList = "instance_id,purpose,version", unique = true),
		@Index(name = "idx_con_rem_id", columnList = "remote_id") })
@NamedQueries({
		@NamedQuery(name = ContentEntity.QUERY_CONTENT_BY_INSTANCE_AND_PURPOSE_KEY, query = ContentEntity.QUERY_CONTENT_BY_INSTANCE_AND_PURPOSE),
		@NamedQuery(name = ContentEntity.QUERY_CONTENTS_BY_INSTANCE_AND_PURPOSE_KEY, query = ContentEntity.QUERY_CONTENTS_BY_INSTANCE_AND_PURPOSE),
		@NamedQuery(name = ContentEntity.QUERY_LATEST_CONTENT_BY_INSTANCE_KEY, query = ContentEntity.QUERY_LATEST_CONTENT_BY_INSTANCE),
		@NamedQuery(name = ContentEntity.ASSIGN_CONTENT_TO_INSTANCE_KEY, query = ContentEntity.ASSIGN_CONTENT_TO_INSTANCE),
		@NamedQuery(name = ContentEntity.QUERY_CONTENT_BY_CHECKSUM_KEY, query = ContentEntity.QUERY_CONTENT_BY_CHECKSUM) })
public class ContentEntity extends BaseStringIdEntity implements Named {

	private static final Long DEFAULT_LENGTH = Long.valueOf(-1L);

	private static final long serialVersionUID = 1660169916637524974L;

	/**
	 * Fetch content entity instance that match given content id or (instance id and content type).
	 */
	public static final String QUERY_CONTENT_BY_INSTANCE_AND_PURPOSE_KEY = "QUERY_CONTENT_BY_INSTANCE_AND_PURPOSE";
	static final String QUERY_CONTENT_BY_INSTANCE_AND_PURPOSE = "select c from ContentEntity c where c.id=:id or (c.instanceId = :id and c.purpose = :purpose) order by c.version desc";

	/**
	 * Fetch content entity instances that match given collection of content ids or (instance ids and content type).
	 */
	public static final String QUERY_CONTENTS_BY_INSTANCE_AND_PURPOSE_KEY = "QUERY_CONTENTS_BY_INSTANCE_AND_PURPOSE";
	static final String QUERY_CONTENTS_BY_INSTANCE_AND_PURPOSE = "select distinct c from ContentEntity c where c.id in (:id) or (c.instanceId in (:id) and c.purpose = :purpose) and version = (select max(cmax.version) from ContentEntity cmax where c.instanceId = cmax.instanceId and c.purpose = cmax.purpose)";

	/** Fetch all content entities for the given instance id. */
	public static final String QUERY_LATEST_CONTENT_BY_INSTANCE_KEY = "QUERY_LATEST_CONTENT_BY_INSTANCE";
	static final String QUERY_LATEST_CONTENT_BY_INSTANCE = "select c from ContentEntity c where c.instanceId = :instanceId and version = (select max(cmax.version) from ContentEntity cmax where c.instanceId = cmax.instanceId and c.purpose = cmax.purpose)";

	/**
	 * Sets the given {@code instanceId} to the content identified by the given {@code id} if it's not already assigned.
	 */
	public static final String ASSIGN_CONTENT_TO_INSTANCE_KEY = "ASSIGN_CONTENT_TO_INSTANCE";
	public static final String ASSIGN_CONTENT_TO_INSTANCE = "update ContentEntity set instanceId = :instanceId where id = :id and instanceId is null";

	/**
	 * Fetch content entity instance that match given content id or (instance id and content type).
	 */
	public static final String QUERY_CONTENT_BY_CHECKSUM_KEY = "QUERY_CONTENT_BY_CHECKSUM";
	static final String QUERY_CONTENT_BY_CHECKSUM = "select c from ContentEntity c where c.checksum=:checksum order by c.version desc";

	@Column(name = "instance_id", length = 128, nullable = true)
	private String instanceId;

	/** The content purpose related to the instance id. */
	@Column(name = "purpose", length = 64, nullable = true)
	private String purpose;

	@Column(name = "mimetype", length = 128)
	private String mimeType;

	/** The original file name. */
	@Column(name = "file_name", length = 512, nullable = false)
	private String name;

	@Column(name = "content_size", nullable = true)
	private Long contentSize;

	@Column(name = "remote_id", length = 1024, nullable = false)
	private String remoteId;

	@Column(name = "remote_source_name", length = 64, nullable = false)
	private String remoteSourceName;

	@JoinColumn(name = "additional_info_id", nullable = true)
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private SerializableValue additionalInfo;

	@Type(type = BooleanCustomType.TYPE_NAME)
	@Column(name = "is_view")
	private Boolean view;

	@Column(name = "charset", length = 16, nullable = true)
	private String charset;

	@Column(name = "version", nullable = false)
	private int version;

	@Type(type = BooleanCustomType.TYPE_NAME)
	@Column(name = "indexable")
	private Boolean indexable;

	@Column(name = "checksum", length = 50, nullable = true)
	private String checksum;

	/**
	 * Gets the instance id.
	 *
	 * @return the instanceId
	 */
	public String getInstanceId() {
		return instanceId;
	}

	/**
	 * Sets the instance id.
	 *
	 * @param instanceId
	 *            the instanceId to set
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * Gets the content purpose related to the instance
	 *
	 * @return the purpose
	 */
	public String getPurpose() {
		return purpose;
	}

	/**
	 * Sets the content purpose related to the instance.
	 *
	 * @param purpose
	 *            the new purpose
	 */
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	/**
	 * Gets the mime type.
	 *
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Sets the mime type.
	 *
	 * @param mimeType
	 *            the mimeType to set
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Gets the remote id.
	 *
	 * @return the remoteId
	 */
	public String getRemoteId() {
		return remoteId;
	}

	/**
	 * Sets the remote id.
	 *
	 * @param remoteId
	 *            the remoteId to set
	 */
	public void setRemoteId(String remoteId) {
		this.remoteId = remoteId;
	}

	/**
	 * Gets the remote source name.
	 *
	 * @return the remoteSourceName
	 */
	public String getRemoteSourceName() {
		return remoteSourceName;
	}

	/**
	 * Sets the remote source name.
	 *
	 * @param remoteSourceName
	 *            the remoteSourceName to set
	 */
	public void setRemoteSourceName(String remoteSourceName) {
		this.remoteSourceName = remoteSourceName;
	}

	/**
	 * Gets the additional info.
	 *
	 * @return the additionalInfo
	 */
	public SerializableValue getAdditionalInfo() {
		return additionalInfo;
	}

	/**
	 * Sets the additional info.
	 *
	 * @param additionalInfo
	 *            the additionalInfo to set
	 */
	public void setAdditionalInfo(SerializableValue additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	/**
	 * Checks if the current entity is new
	 *
	 * @return true, if is new
	 */
	public boolean isNew() {
		return getId() == null || getRemoteId() == null;
	}

	/**
	 * Converts the current entity to {@link StoreItemInfo}.
	 *
	 * @return the store item info
	 */
	public StoreItemInfo toStoreInfo() {
		long contentLength = 0L;
		if (getContentSize() != null) {
			contentLength = getContentSize().longValue();
		}
		SerializableValue serializableValue = getAdditionalInfo();
		Serializable additionalData = null;
		if (serializableValue != null) {
			additionalData = serializableValue.getSerializable();
		}
		return new StoreItemInfo(getRemoteId(), getRemoteSourceName(), contentLength, getMimeType(), additionalData);
	}

	/**
	 * Copy information from the given {@link StoreItemInfo}
	 *
	 * @param storeItemInfo
	 *            the store item info
	 */
	@SuppressWarnings("boxing")
	public void copyFrom(StoreItemInfo storeItemInfo) {
		if (storeItemInfo == null) {
			return;
		}
		setRemoteId(storeItemInfo.getRemoteId());
		setRemoteSourceName(storeItemInfo.getProviderType());
		if (getContentSize() == null || getContentSize().longValue() <= 0L || storeItemInfo.getContentLength() > 0) {
			setContentSize(storeItemInfo.getContentLength());
		}
		Serializable data = storeItemInfo.getAdditionalData();
		if (data != null) {
			if (getAdditionalInfo() == null) {
				setAdditionalInfo(new SerializableValue(data));
			} else {
				getAdditionalInfo().setSerializable(data);
			}
		}
	}

	/**
	 * Copy information from the given {@link Content} to the current entity instance.
	 *
	 * @param content
	 *            the content to copy from
	 */
	public void copyFrom(Content content) {
		if (content == null) {
			return;
		}
		setMimeType(content.getMimeType());
		setCharset(content.getCharset());
		setPurpose(content.getPurpose());
		setName(content.getName());
		setIndexable(Boolean.valueOf(content.isIndexable()));
		if (getName() == null) {
			setName(content.getContent().getId());
		}
		setView(Boolean.valueOf(content.isView()));
		if (content.getContentLength() != null) {
			setContentSize(content.getContentLength());
		} else if (getContentSize() == null) {
			setContentSize(DEFAULT_LENGTH);
		}
	}

	/**
	 * Merge the attributes from the given content instance by keeping the original values if the incomming value is
	 * <code>null</code>
	 *
	 * @param content
	 *            the content to copy from
	 */
	public void merge(Content content) {
		if (content == null) {
			return;
		}
		setMimeType(getOrDefault(content.getMimeType(), getMimeType()));
		setCharset(getOrDefault(content.getCharset(), getCharset()));
		setPurpose(getOrDefault(content.getPurpose(), getPurpose()));

		importFromContent(content);

		setName(getOrDefault(content.getName(), getName()));

		if (getName() == null && content.getContent() != null) {
			setName(content.getContent().getId());
		}
		setView(Boolean.valueOf(content.isView()));
		if (getContentSize() == null || getContentSize().longValue() == -1L || content.getContentLength() != null) {
			setContentSize(content.getContentLength());
		}
		if (getContentSize() == null) {
			setContentSize(DEFAULT_LENGTH);
		}
	}

	private void importFromContent(Content content) {
		if (content instanceof ContentImport) {
			ContentImport contentImport = (ContentImport) content;
			setInstanceId(getOrDefault(Objects.toString(contentImport.getInstanceId(), null), getInstanceId()));
			setRemoteId(getOrDefault(contentImport.getRemoteId(), getRemoteId()));
			setRemoteSourceName(getOrDefault(contentImport.getRemoteSourceName(), getRemoteSourceName()));
		}
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the content size.
	 *
	 * @return the contentSize
	 */
	public Long getContentSize() {
		return contentSize;
	}

	/**
	 * Sets the content size.
	 *
	 * @param contentSize
	 *            the contentSize to set
	 */
	public void setContentSize(Long contentSize) {
		this.contentSize = contentSize;
	}

	/**
	 * @return the view
	 */
	public Boolean getView() {
		return view;
	}

	/**
	 * @param view
	 *            the view to set
	 */
	public void setView(Boolean view) {
		this.view = view;
	}

	/**
	 * @return the charset
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * @param charset
	 *            the charset to set
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * Getter for content version.
	 *
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Setter for content version.
	 *
	 * @param version
	 *            the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	/**
	 * Gets the indexable.
	 *
	 * @return the indexable
	 */
	public Boolean getIndexable() {
		return indexable;
	}

	/**
	 * Sets the indexable.
	 *
	 * @param indexable
	 *            the new indexable
	 */
	public void setIndexable(Boolean indexable) {
		this.indexable = indexable;
	}

	/**
	 * Get content check sum if available
	 *
	 * @return the checksum
	 */
	public String getChecksum() {
		return checksum;
	}

	/**
	 * Set content check sum
	 *
	 * @param checksum
	 *            the checksum to set
	 */
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (purpose == null ? 0 : purpose.hashCode());
		result = prime * result + (instanceId == null ? 0 : instanceId.hashCode());
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
		if (!(obj instanceof ContentEntity)) {
			return false;
		}
		ContentEntity other = (ContentEntity) obj;
		return nullSafeEquals(instanceId, other.getInstanceId()) && nullSafeEquals(purpose, other.getPurpose());
	}
}
