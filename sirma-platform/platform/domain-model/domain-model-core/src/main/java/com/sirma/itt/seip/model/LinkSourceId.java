/*
 *
 */
package com.sirma.itt.seip.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.json.JSONObject;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoCopyable;
import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.Resettable;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.InitializedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Mapping class for single source element for links
 *
 * @author BBonev
 */
@Embeddable
public class LinkSourceId implements InstanceReference, KryoCopyable<LinkSourceId>, Resettable, JsonRepresentable {
	private static final long serialVersionUID = 5126865626500416947L;

	/** The source id. */
	@Column(name = "sourceId", length = 100, nullable = false)
	@Tag(1)
	private String sourceId;

	/** The source type. */
	@OneToOne(cascade = { CascadeType.REFRESH }, targetEntity = DataType.class)
	@JoinColumn(name = "sourceType")
	@Tag(2)
	private DataTypeDefinition sourceType;

	/** The semantic instance type if resolved. */
	@Transient
	private InstanceType type;

	@Transient
	private InstanceReference parent;

	/** The instance. */
	@Transient
	// the instance should not be serialized and is lazy loaded
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
	 * Instantiates a new link source id from the given reference by coping the input data.
	 *
	 * @param copyFrom
	 *            the copy from
	 */
	public LinkSourceId(InstanceReference copyFrom) {
		this(copyFrom.getIdentifier(), copyFrom.getReferenceType());
		setType(copyFrom.getType());
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
	 * Instantiates a new link source id.
	 *
	 * @param sourceId
	 *            the source id
	 * @param sourceType
	 *            the source type
	 * @param type
	 *            the instance type
	 * @param instance
	 *            the instance that represents the current reference if fetched.
	 */
	public LinkSourceId(String sourceId, DataTypeDefinition sourceType, InstanceType type, Instance instance) {
		this.sourceId = sourceId;
		this.sourceType = sourceType;
		this.instance = instance;
		this.type = type;
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
	 * Sets the instance type.
	 *
	 * @param type
	 *            the new type
	 */
	@Override
	public void setType(InstanceType type) {
		this.type = type;
	}

	@Override
	public InstanceType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (sourceId == null ? 0 : sourceId.hashCode());
		result = prime * result + (sourceType == null ? 0 : sourceType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof InstanceReference)) {
			return false;
		}
		InstanceReference other = (InstanceReference) obj;
		if (!EqualsHelper.nullSafeEquals(sourceId, other.getIdentifier())) {
			return false;
		}
		return EqualsHelper.nullSafeEquals(sourceType, other.getReferenceType());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(128);
		builder.append("InstanceReference [sourceId=");
		builder.append(sourceId);
		builder.append(", sourceType=");
		builder.append(sourceType != null ? sourceType.getId() : "null");
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
		if (instance == null && StringUtils.isNotNullOrEmpty(getIdentifier()) && getReferenceType() != null) {
			// note if not found the first time will try to fetch it again every time when requested
			instance = TypeConverterUtil.getConverter().convert(InitializedInstance.class, this).getInstance();
		}
		return instance;
	}

	@Override
	public LinkSourceId copy(Kryo kryo) {
		// copy the class but keep the original data type definition instance - no need to copy
		// immutable object
		return new LinkSourceId(getIdentifier(), getReferenceType(), getType(), null);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * When called the method will clean any internal state if any. The method is intended to be called before cache
	 * store (if the cache is local).
	 */
	@Override
	public void reset() {
		instance = null;
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject jsonObject = new JSONObject();
		JsonUtil.addToJson(jsonObject, "instanceId", getIdentifier());
		if (getReferenceType() != null) {
			JsonUtil.addToJson(jsonObject, "instanceType", getReferenceType().getName());
		}
		return jsonObject;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		String id = JsonUtil.getStringValue(jsonObject, "instanceId");
		String instanceType = JsonUtil.getStringValue(jsonObject, "instanceType");
		setIdentifier(id);
		if (instanceType != null) {
			setReferenceType(TypeConverterUtil.getConverter().convert(DataTypeDefinition.class, instanceType));
		}
	}

	@Override
	public InstanceReference getParent() {
		return parent;
	}

	@Override
	public void setParent(InstanceReference parentReference) {
		parent = parentReference;
	}
}
