package com.sirma.itt.seip.instance.properties.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.db.PersistenceUnits;
import com.sirma.itt.seip.db.discovery.PersistenceUnitBinding;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.properties.PropertyModelValue;
import com.sirma.itt.seip.instance.properties.entity.ValueTypeConverter.ValueType;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Immutable property value storage class.
 *
 * @author Derek Hulley
 * @since 3.4
 */
@PersistenceUnitBinding(PersistenceUnits.PRIMARY)
@MappedSuperclass
public abstract class BasePropertyValue implements PropertyModelValue {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -5165363819138715938L;

	private static final String NULL_FOR_PERSISTED_TYPE = "Value must be null for persisted type: ";

	/**
	 * used to take care of empty strings being converted to nulls by the database.
	 */
	private static final String STRING_EMPTY = "";

	private static final Logger LOGGER = LoggerFactory.getLogger(BasePropertyValue.class);

	/** The id. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** the type of the property, prior to serialization persistence. */
	private ValueType actualType;

	/** the type of persistence used. */
	private ValueType persistedType;

	/** The boolean value. */
	@Type(type = "com.sirma.itt.seip.db.customtype.BooleanCustomType")
	private Boolean booleanValue;

	/** The long value. */
	private Long longValue;

	/** The float value. */
	private Float floatValue;

	/** The double value. */
	private Double doubleValue;

	/** The string value. */
	@Column(name = "stringValue", length = 2048, nullable = true)
	private String stringValue;

	/**
	 * Default constructor.
	 */
	public BasePropertyValue() {
	}

	/**
	 * Construct a new property value.
	 *
	 * @param typeQName
	 *            the dictionary-defined property type to store the property as
	 * @param value
	 *            the value to store. This will be converted into a format compatible with the type given
	 */
	public BasePropertyValue(String typeQName, Serializable value) {

		if (value == null) {
			actualType = ValueTypeConverter.getActualType(null);
			setPersistedValue(ValueType.NULL, null);
		} else {
			// Convert the value to the type required. This ensures that any
			// type conversion issues
			// are caught early and prevent the scenario where the data in the
			// DB cannot be given
			// back out because it is unconvertable.
			ValueType valueType = ValueTypeConverter.makeValueType(typeQName);
			Serializable localValue = value;
			localValue = valueType.convert(localValue);

			actualType = ValueTypeConverter.getActualType(localValue);
			// get the persisted type
			ValueType persistedValueType = actualType.getPersistedType(localValue);
			// convert to the persistent type
			localValue = persistedValueType.convert(localValue);
			setPersistedValue(persistedValueType, localValue);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (obj instanceof BasePropertyValue) {
			BasePropertyValue that = (BasePropertyValue) obj;
			return actualType == that.actualType
					&& EqualsHelper.nullSafeEquals(getPersistedValue(), that.getPersistedValue());
		}
		return false;
	}

	@Override
	public int hashCode() {
		int h = 0;
		if (actualType != null) {
			h = actualType.hashCode();
		}
		Serializable persistedValue = getPersistedValue();
		if (persistedValue != null) {
			h += 17 * persistedValue.hashCode();
		}
		return h;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(128);
		sb
				.append("PropertyValue")
					.append("[actual-type=")
					.append(actualType)
					.append(", value-type=")
					.append(persistedType)
					.append(", value=")
					.append(getPersistedValue())
					.append("]");
		return sb.toString();
	}

	/**
	 * Gets the actual type.
	 *
	 * @return the actual type
	 */
	public Integer getActualType() {
		return actualType == null ? null : actualType.getOrdinalNumber();
	}

	/**
	 * Gets the actual type string.
	 *
	 * @return Returns the actual type's String representation
	 */
	public String getActualTypeString() {
		return actualType == null ? null : actualType.toString();
	}

	/**
	 * Sets the actual type.
	 *
	 * @param actualType
	 *            the new actual type
	 */
	public void setActualType(Integer actualType) {
		ValueType type = ValueTypeConverter.getTypeByOriginalNumber(actualType);
		if (type == null) {
			LOGGER.error("Unknown property actual type ordinal number: {}", actualType);
		}
		this.actualType = type;
	}

	/**
	 * Gets the persisted type.
	 *
	 * @return the persisted type
	 */
	public Integer getPersistedType() {
		return persistedType == null ? null : persistedType.getOrdinalNumber();
	}

	/**
	 * Sets the persisted type.
	 *
	 * @param persistedType
	 *            the new persisted type
	 */
	public void setPersistedType(Integer persistedType) {
		ValueType type = ValueTypeConverter.getTypeByOriginalNumber(persistedType);
		if (type == null) {
			LOGGER.error("Unknown property persisted type ordinal number: {}", persistedType);
		}
		this.persistedType = type;
	}

	/**
	 * Stores the value in the correct slot based on the type of persistence requested. No conversion is done.
	 *
	 * @param persistedType
	 *            the value type
	 * @param value
	 *            the value - it may only be null if the persisted type is {@link ValueType#NULL}
	 */
	protected void setPersistedValue(ValueType persistedType, Serializable value) {
		switch (persistedType) {
			case NULL:
				handleNullValue(persistedType, value);
				break;
			case BOOLEAN:
				booleanValue = (Boolean) value;
				break;
			case LONG:
				longValue = (Long) value;
				break;
			case FLOAT:
				floatValue = (Float) value;
				break;
			case DOUBLE:
				doubleValue = (Double) value;
				break;
			case STRING:
				stringValue = (String) value;
				break;
			case SERIALIZABLE:
				storeSerializableValue(value);
				break;
			default:
				throw new EmfRuntimeException("Unrecognised value type: " + persistedType);
		}
		// we store the type that we persisted as
		this.persistedType = persistedType;
	}

	/**
	 * Handle null value.
	 *
	 * @param persistedType
	 *            the persisted type
	 * @param value
	 *            the value
	 */
	protected void handleNullValue(ValueType persistedType, Serializable value) {
		if (value != null) {
			throw new EmfRuntimeException(NULL_FOR_PERSISTED_TYPE + persistedType);
		}
	}

	/**
	 * Store serializable value. The implementation should ensure the separation of the given value from the persistent
	 * model
	 *
	 * @param value
	 *            the value
	 */
	protected abstract void storeSerializableValue(Serializable value);

	/**
	 * Clones a serializable object to disconnect the original instance from the persisted instance.
	 *
	 * @param <E>
	 *            the element type
	 * @param original
	 *            the original object
	 * @return the new cloned object
	 */
	@SuppressWarnings("unchecked")
	protected <E extends Serializable> E cloneSerializable(E original) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutputStream objectOut = new ObjectOutputStream(byteOut)) {
			objectOut.writeObject(original);
			objectOut.flush();

			try (ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream(byteOut.toByteArray()))) {
				return (E) objectIn.readObject();
			}
		} catch (Exception e) {
			throw new EmfRuntimeException("Failed to clone serializable object: " + original, e);
		}
	}

	/**
	 * Gets the persisted value.
	 *
	 * @return Returns the persisted value, keying off the persisted value type
	 */
	private Serializable getPersistedValue() {
		switch (persistedType) {
			case NULL:
				return null;
			case BOOLEAN:
				return booleanValue;
			case LONG:
				return longValue;
			case FLOAT:
				return floatValue;
			case DOUBLE:
				return doubleValue;
			case STRING:
				// Oracle stores empty strings as 'null'...
				return stringValue == null ? STRING_EMPTY : stringValue;
			case SERIALIZABLE:
				return loadSerializableValue();
			default:
				throw new EmfRuntimeException("Unrecognised value type: " + persistedType);
		}
	}

	/**
	 * Load serializable value.
	 *
	 * @return the serializable
	 */
	protected abstract Serializable loadSerializableValue();

	/**
	 * Fetches the value as a desired type. Collections (i.e. multi-valued properties) will be converted as a whole to
	 * ensure that all the values returned within the collection match the given type.
	 *
	 * @param typeQName
	 *            the type required for the return value
	 * @return Returns the value of this property as the desired type, or a <code>Collection</code> of values of the
	 *         required type
	 * @see DataTypeDefinition#ANY The static qualified names for the types
	 */
	@Override
	public Serializable getValue(String typeQName) {
		// first check for null
		ValueType requiredType = ValueTypeConverter.makeValueType(typeQName);
		if (requiredType == ValueType.SERIALIZABLE) {
			// the required type must be the actual type
			requiredType = actualType;
		}

		// we need to convert
		Serializable ret = null;
		if (actualType == ValueType.COLLECTION && persistedType == ValueType.NULL) {
			// This is a special case of an empty collection
			ret = (Serializable) Collections.emptyList();
		} else {
			Serializable persistedValue = getPersistedValue();
			// convert the type
			// In order to cope with historical data, where collections were
			// serialized
			// regardless of type.
			if (persistedValue instanceof Collection<?>) {
				// We assume that the collection contained the correct type
				// values. They would
				// have been converted on the way in.
				ret = persistedValue;
			} else {
				ret = requiredType.convert(persistedValue);
			}
		}
		// done
		LOGGER.trace("Fetched value: \n   property value: {}\n   requested type: {}\n   result: {}", this, requiredType,
				ret);
		return ret;
	}

	/**
	 * Gets the value or values as a guaranteed collection.
	 *
	 * @param typeQName
	 *            the type q name
	 * @return the collection
	 * @see #getValue(String)
	 */
	@SuppressWarnings("unchecked")
	public Collection<Serializable> getCollection(String typeQName) {
		Serializable value = getValue(typeQName);
		if (value instanceof Collection) {
			return (Collection<Serializable>) value;
		}
		return Collections.singletonList(value);
	}

	/**
	 * Gets the boolean value.
	 *
	 * @return the boolean value
	 */
	public boolean getBooleanValue() {
		if (booleanValue == null) {
			return false;
		}
		return booleanValue.booleanValue();
	}

	/**
	 * Sets the boolean value.
	 *
	 * @param value
	 *            the new boolean value
	 */
	public void setBooleanValue(boolean value) {
		booleanValue = Boolean.valueOf(value);
	}

	/**
	 * Gets the long value.
	 *
	 * @return the long value
	 */
	public long getLongValue() {
		if (longValue == null) {
			return 0;
		}
		return longValue.longValue();
	}

	/**
	 * Sets the long value.
	 *
	 * @param value
	 *            the new long value
	 */
	public void setLongValue(long value) {
		longValue = Long.valueOf(value);
	}

	/**
	 * Gets the float value.
	 *
	 * @return the float value
	 */
	public float getFloatValue() {
		if (floatValue == null) {
			return 0.0F;
		}
		return floatValue.floatValue();
	}

	/**
	 * Sets the float value.
	 *
	 * @param value
	 *            the new float value
	 */
	public void setFloatValue(float value) {
		floatValue = Float.valueOf(value);
	}

	/**
	 * Gets the double value.
	 *
	 * @return the double value
	 */
	public double getDoubleValue() {
		if (doubleValue == null) {
			return 0.0;
		}
		return doubleValue.doubleValue();
	}

	/**
	 * Sets the double value.
	 *
	 * @param value
	 *            the new double value
	 */
	public void setDoubleValue(double value) {
		doubleValue = Double.valueOf(value);
	}

	/**
	 * Gets the string value.
	 *
	 * @return the string value
	 */
	public String getStringValue() {
		return stringValue;
	}

	/**
	 * Sets the string value.
	 *
	 * @param value
	 *            the new string value
	 */
	public void setStringValue(String value) {
		stringValue = value;
	}

	/**
	 * Getter method for id.
	 *
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * Setter method for id.
	 *
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
}
