/*
 *
 */
package com.sirma.itt.emf.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoCopyable;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.instance.model.InitializedInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;

/**
 * Mapping class for single source element for links
 *
 * @author BBonev
 */
@Embeddable
public class LinkSourceId implements InstanceReference, KryoCopyable<LinkSourceId> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -3667057426317805209L;

	/** The source id. */
	@Column(name = "sourceId", length = 50, nullable = false)
	@Tag(1)
	private String sourceId;

	/** The source type. */
	@OneToOne(cascade = { CascadeType.REFRESH }, targetEntity = DataType.class)
	@JoinColumn(name = "sourceType")
	@Tag(2)
	private DataTypeDefinition sourceType;

	/** The instance. */
	private transient Instance instance;

	/**
	 * Instantiates a new link source id.
	 */
	public LinkSourceId() {
		// default constructor
	}

	/**
	 * Instantiates a new link source id.
	 *
	 * @param sourceId
	 *            the source id
	 * @param sourceType
	 *            the source type
	 */
	public LinkSourceId(String sourceId, DataTypeDefinition sourceType) {
		this.sourceId = sourceId;
		this.sourceType = sourceType;
	}

	/**
	 * Instantiates a new link source id.
	 * 
	 * @param sourceId
	 *            the source id
	 * @param sourceType
	 *            the source type
	 * @param instance
	 *            the instance that represents the current reference if fetched.
	 */
	public LinkSourceId(String sourceId, DataTypeDefinition sourceType, Instance instance) {
		this.sourceId = sourceId;
		this.sourceType = sourceType;
		this.instance = instance;
	}

	/**
	 * Getter method for sourceId.
	 * 
	 * @return the sourceId
	 */
	public String getSourceId() {
		return sourceId;
	}

	/**
	 * Setter method for sourceId.
	 *
	 * @param sourceId
	 *            the sourceId to set
	 */
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	/**
	 * Getter method for sourceType.
	 *
	 * @return the sourceType
	 */
	public DataTypeDefinition getSourceType() {
		return sourceType;
	}

	/**
	 * Setter method for sourceType.
	 *
	 * @param sourceType
	 *            the sourceType to set
	 */
	public void setSourceType(DataTypeDefinition sourceType) {
		this.sourceType = sourceType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((sourceId == null) ? 0 : sourceId.hashCode());
		result = (prime * result) + ((sourceType == null) ? 0 : sourceType.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof LinkSourceId)) {
			return false;
		}
		LinkSourceId other = (LinkSourceId) obj;
		if (sourceId == null) {
			if (other.sourceId != null) {
				return false;
			}
		} else if (!sourceId.equals(other.sourceId)) {
			return false;
		}
		if (sourceType == null) {
			if (other.sourceType != null) {
				return false;
			}
		} else if (!sourceType.equals(other.sourceType)) {
			return false;
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InstanceReference [sourceId=");
		builder.append(sourceId);
		builder.append(", sourceType=");
		builder.append(sourceType);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public String getIdentifier() {
		return getSourceId();
	}

	@Override
	public void setIdentifier(String identifier) {
		setSourceId(identifier);
	}

	@Override
	public DataTypeDefinition getReferenceType() {
		return getSourceType();
	}

	@Override
	public void setReferenceType(DataTypeDefinition referenceType) {
		setSourceType(referenceType);
	}

	@Override
	public Instance toInstance() {
		if ((instance == null) && StringUtils.isNotNullOrEmpty(getIdentifier())
				&& (getReferenceType() != null)) {
			// note if not found the first time will try to fetch it again every time when requested
			instance = TypeConverterUtil.getConverter().convert(InitializedInstance.class, this)
					.getInstance();
		}
		return instance;
	}

	@Override
	public LinkSourceId copy(Kryo kryo) {
		// copy the class but keep the original data type definition instance - no need to copy
		// immutable object
		return new LinkSourceId(getIdentifier(), getReferenceType());
	}

}
