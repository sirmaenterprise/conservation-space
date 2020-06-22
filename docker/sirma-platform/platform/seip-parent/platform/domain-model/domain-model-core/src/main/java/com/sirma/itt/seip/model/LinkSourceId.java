/*
 *
 */
package com.sirma.itt.seip.model;

import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import org.json.JSONObject;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoCopyable;
import com.sirma.itt.seip.Resettable;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceReferenceImpl;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * {@link LinkSourceId} extends {@link InstanceReferenceImpl} to support kryo copying and json representation
 *
 * @author BBonev
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@Embeddable
@javax.persistence.Access(AccessType.PROPERTY)
public class LinkSourceId extends InstanceReferenceImpl
		implements KryoCopyable<LinkSourceId>, Resettable, JsonRepresentable {
	private static final long serialVersionUID = 5126865626500416947L;

	/**
	 * Instantiates a new ldink source id with default null parameters
	 */
	public LinkSourceId() {
		super(null, null, null, null);
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
		this(sourceId, sourceType, null, null);
	}

	/**
	 * Instantiates a new link source id from the given reference by coping the input data.
	 *
	 * @param copyFrom
	 *            the copy from
	 */
	public LinkSourceId(InstanceReference copyFrom) {
		this(copyFrom.getId(), copyFrom.getReferenceType(), copyFrom.getType(), null);
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
		this(sourceId, sourceType, null, instance);
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
		super(sourceId, sourceType, type, instance);
	}

	/** The source id. */
	@Column(name = "id", length = 100, nullable = false)
	@Override
	public String getId() {
		return super.getId();
	}

	/** The source type. */
	@OneToOne(cascade = { CascadeType.REFRESH }, targetEntity = DataType.class)
	@JoinColumn(name = "referenceType")
	@Override
	public DataTypeDefinition getReferenceType() {
		return super.getReferenceType();
	}

	@Override
	public LinkSourceId copy(Kryo kryo) {
		// copy the class but keep the original data type definition instance - no need to copy
		// immutable object
		return new LinkSourceId(getId(), getReferenceType(), getType(), null);
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
		JsonUtil.addToJson(jsonObject, "instanceId", getId());
		if (getReferenceType() != null) {
			JsonUtil.addToJson(jsonObject, "instanceType", getReferenceType().getName());
		}
		return jsonObject;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		String instanceType = JsonUtil.getStringValue(jsonObject, "instanceType");
		setId(JsonUtil.getStringValue(jsonObject, "instanceId"));
		if (instanceType != null) {
			setReferenceType(TypeConverterUtil.getConverter().convert(DataTypeDefinition.class, instanceType));
		}
	}

}
