package com.sirma.itt.seip.domain.instance;

import java.util.Objects;

import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * {@link InstanceReferenceImpl} is base implementation for {@link InstanceReference} holding all common data and
 * method. Specific subclasses are encouraged to use this class as super method instead of implementing the interface
 *
 * @author bbanchev
 */
public class InstanceReferenceImpl implements InstanceReference {

	private static final long serialVersionUID = -3474242132603224977L;
	@Tag(1)
	protected String id;
	@Tag(2)
	protected DataTypeDefinition referenceType;
	@Transient
	protected transient InstanceType type;
	@Transient
	protected transient Instance instance;

	/**
	 * Constructs new reference based on id and data type
	 *
	 * @param identifier
	 *            the id of the reference
	 * @param referenceType
	 *            the data type for the reference
	 */
	public InstanceReferenceImpl(String identifier, DataTypeDefinition referenceType) {
		this(identifier, referenceType, null, null);
	}

	/**
	 * Constructs new reference based on id, data type and instance type
	 *
	 * @param identifier
	 *            the id of the reference
	 * @param referenceType
	 *            the data type for the reference
	 * @param type
	 *            the type of the reference
	 */
	public InstanceReferenceImpl(String identifier, DataTypeDefinition referenceType, InstanceType type) {
		this(identifier, referenceType, type, null);
	}

	/**
	 * Full constructor for {@link InstanceReferenceImpl}
	 *
	 * @param identifier
	 *            the id of the reference
	 * @param referenceType
	 *            the data type for the reference
	 * @param type
	 *            the type of the reference
	 * @param instance
	 *            the source instance of this reference
	 */
	public InstanceReferenceImpl(String identifier, DataTypeDefinition referenceType, InstanceType type,
			Instance instance) {
		this.id = identifier;
		this.referenceType = referenceType;
		this.type = type;
		this.instance = instance;
	}

	@Override
	public String getId() {
		if (id == null && instance != null) {
			id = Objects.toString(instance.getId(), null);
		}
		return id;
	}

	@Override
	public void setId(String id) {
		this.id = id;
		if (instance != null && !EqualsHelper.nullSafeEquals(id, instance.getId())) {
			instance.setId(id);
		}
	}

	@Override
	public DataTypeDefinition getReferenceType() {
		return referenceType;
	}

	@Override
	public void setReferenceType(DataTypeDefinition referenceType) {
		this.referenceType = referenceType;
	}

	@Override
	public InstanceType getType() {
		if (type == null && instance != null) {
			type = instance.type();
		}
		return type;
	}

	@Override
	public void setType(InstanceType type) {
		this.type = type;
		if (instance != null && !EqualsHelper.nullSafeEquals(type, instance.type())) {
			instance.setType(type);
		}
	}

	@Override
	public Instance toInstance() {
		if (instance == null && StringUtils.isNotBlank(getId())) {
			instance = TypeConverterUtil.getConverter().convert(InitializedInstance.class, this).getInstance();
		}
		return instance;
	}

	@Override
	public int hashCode() {
		return 31 + (id == null ? 0 : id.hashCode());
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
		return EqualsHelper.nullSafeEquals(id, other.getId());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(100);
		builder
				.append(this.getClass().getSimpleName())
					.append(" [id=")
					.append(id)
					.append(", referenceType=")
					.append(referenceType)
					.append(", type=")
					.append(type)
					.append(", instance=")
					.append(instance != null ? "set" : "unset")
					.append("]");
		return builder.toString();
	}

}
