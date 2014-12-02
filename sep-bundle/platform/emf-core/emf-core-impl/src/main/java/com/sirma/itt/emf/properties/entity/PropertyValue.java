/*
 * Copyright (C) 2005-2010 Alfresco Software Limited. This file is part of Alfresco Alfresco is free
 * software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details. You should have
 * received a copy of the GNU Lesser General Public License along with Alfresco. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.sirma.itt.emf.properties.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;

import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.model.Uri;
import com.sirma.itt.emf.entity.SerializableValue;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Immutable property value storage class.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
@Entity
@Table(name = "emf_propertyValue")
@org.hibernate.annotations.Table(appliesTo = "emf_propertyValue")
public class PropertyValue implements Cloneable, Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -3938814938067634683L;

	/**
	 * used to take care of empty strings being converted to nulls by the database.
	 */
	private static final String STRING_EMPTY = "";

	/** used to provide empty collection values in and out. */
	public static final Serializable EMPTY_COLLECTION_VALUE = (Serializable) Collections
			.emptyList();

	/** The logger. */
	private static final Log LOGGER = LogFactory.getLog(PropertyValue.class);

	/**
	 * potential value types.
	 */
	private static enum ValueType {

		/** The null. */
		NULL {
			@Override
			public Integer getOrdinalNumber() {
				return Integer.valueOf(0);
			}

			@Override
			Serializable convert(Serializable value) {
				return null;
			}
		},

		/** The boolean. */
		BOOLEAN {
			@Override
			public Integer getOrdinalNumber() {
				return Integer.valueOf(1);
			}

			@Override
			Serializable convert(Serializable value) {
				return TypeConverterUtil.getConverter().convert(Boolean.class, value);
			}
		},

		/** The integer. */
		INTEGER {
			@Override
			public Integer getOrdinalNumber() {
				return Integer.valueOf(2);
			}

			@Override
			protected ValueType getPersistedType(Serializable value) {
				return ValueType.LONG;
			}

			@Override
			Serializable convert(Serializable value) {
				return TypeConverterUtil.getConverter().convert(Integer.class, value);
			}
		},

		/** The long. */
		LONG {
			@Override
			public Integer getOrdinalNumber() {
				return Integer.valueOf(3);
			}

			@Override
			Serializable convert(Serializable value) {
				if (value == null) {
					return null;
				}
				return TypeConverterUtil.getConverter().convert(Long.class, value);
			}
		},

		/** The float. */
		FLOAT {
			@Override
			public Integer getOrdinalNumber() {
				return Integer.valueOf(4);
			}

			@Override
			Serializable convert(Serializable value) {
				return TypeConverterUtil.getConverter().convert(Float.class, value);
			}
		},

		/** The double. */
		DOUBLE {
			@Override
			public Integer getOrdinalNumber() {
				return Integer.valueOf(5);
			}

			@Override
			Serializable convert(Serializable value) {
				return TypeConverterUtil.getConverter().convert(Double.class, value);
			}
		},

		/** The string. */
		STRING {
			@Override
			public Integer getOrdinalNumber() {
				return Integer.valueOf(6);
			}

			/**
			 * Strings longer than the maximum of.
			 * 
			 * @param value
			 *            the value
			 * @return the persisted type {@link PropertyValue#DEFAULT_MAX_STRING_LENGTH} characters
			 *         will be serialized.
			 */
			@Override
			protected ValueType getPersistedType(Serializable value) {
				if (value instanceof String) {
					String valueStr = (String) value;
					// Check how long the String can be
					if (valueStr.length() > 1024) {
						return ValueType.SERIALIZABLE;
					}
				}
				return ValueType.STRING;
			}

			@Override
			Serializable convert(Serializable value) {
				return TypeConverterUtil.getConverter().convert(String.class, value);
			}
		},

		/** The date. */
		DATE {
			@Override
			public Integer getOrdinalNumber() {
				return Integer.valueOf(7);
			}

			@Override
			protected ValueType getPersistedType(Serializable value) {
				return ValueType.STRING;
			}

			@Override
			Serializable convert(Serializable value) {
				return TypeConverterUtil.getConverter().convert(Date.class, value);
			}
		},

		/** The serializable. */
		SERIALIZABLE {
			@Override
			public Integer getOrdinalNumber() {
				return Integer.valueOf(9);
			}

			@Override
			Serializable convert(Serializable value) {
				return value;
			}
		},
		INSTANCE {
			@Override
			public Integer getOrdinalNumber() {
				return Integer.valueOf(10);
			}

			@Override
			protected ValueType getPersistedType(Serializable value) {
				return ValueType.STRING;
			}

			@Override
			Serializable convert(Serializable value) {
				return TypeConverterUtil.getConverter().convert(CommonInstance.class, value);
			}
		},
		/** The collection. */
		COLLECTION {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public Integer getOrdinalNumber() {
				return Integer.valueOf(19);
			}

			/**
			 * @param value
			 *            is the value to convert
			 * @return Returns and empty <tt>Collection</tt> if the value is null otherwise it just
			 *         returns the original value
			 */
			@Override
			Serializable convert(Serializable value) {
				if (value == null) {
					return (Serializable) Collections.emptyList();
				}
				return value;
			}
		},
		ANY_INSTANCE {
			@Override
			public Integer getOrdinalNumber() {
				return Integer.valueOf(11);
			}

			@Override
			protected ValueType getPersistedType(Serializable value) {
				return ValueType.STRING;
			}

			@Override
			Serializable convert(Serializable value) {
				return TypeConverterUtil.getConverter().convert(InstanceReference.class, value)
						.toInstance();
			}
		},
		URI {
			@Override
			public Integer getOrdinalNumber() {
				return Integer.valueOf(12);
			}

			@Override
			protected ValueType getPersistedType(Serializable value) {
				return ValueType.STRING;
			}

			@Override
			Serializable convert(Serializable value) {
				return TypeConverterUtil.getConverter().convert(Uri.class, value);
			}
		};

		/**
		 * @return Returns the manually-maintained ordinal number for the value
		 */
		public abstract Integer getOrdinalNumber();

		/**
		 * Override if the type gets persisted in a different format.
		 * 
		 * @param value
		 *            the actual value that is to be persisted. May not be null.
		 * @return the persisted type
		 */
		protected ValueType getPersistedType(Serializable value) {
			return this;
		}

		/**
		 * Converts a value to this type. The implementation must be able to cope with any
		 * legitimate source value.
		 * 
		 * @param value
		 *            the value
		 * @return the serializable
		 * @see TypeConverter#getConverter()#convert(Class, Object)
		 */
		abstract Serializable convert(Serializable value);
	}

	/**
	 * Determine the actual value type to aid in more concise persistence.
	 * 
	 * @param value
	 *            the value that is to be persisted
	 * @return Returns the value type equivalent of the
	 */
	private static ValueType getActualType(Serializable value) {
		if (value == null) {
			return ValueType.NULL;
		} else if (value instanceof Boolean) {
			return ValueType.BOOLEAN;
		} else if (value instanceof Integer) {
			return ValueType.INTEGER;
		} else if (value instanceof Long) {
			return ValueType.LONG;
		} else if (value instanceof Float) {
			return ValueType.FLOAT;
		} else if (value instanceof Double) {
			return ValueType.DOUBLE;
		} else if (value instanceof String) {
			return ValueType.STRING;
		} else if (value instanceof Date) {
			return ValueType.DATE;
		} else if (value instanceof CommonInstance) {
			return ValueType.INSTANCE;
		} else if (value instanceof Instance) {
			return ValueType.ANY_INSTANCE;
		} else if (value instanceof InstanceReference) {
			return ValueType.ANY_INSTANCE;
		} else if (value instanceof Uri) {
			return ValueType.URI;
		} else {
			// type is not recognised as belonging to any particular slot
			return ValueType.SERIALIZABLE;
		}
	}

	/**
	 * a mapping from a property type name to the corresponding value type.
	 */
	private static Map<String, ValueType> valueTypesByPropertyType;
	/**
	 * a mapping of {@link ValueType} ordinal number to the enum. This is manually maintained and
	 * <b>MUST NOT BE CHANGED FOR EXISTING VALUES</b>.
	 */
	private static Map<Integer, ValueType> valueTypesByOrdinalNumber;
	static {
		valueTypesByPropertyType = new HashMap<>(37);
		valueTypesByPropertyType.put(DataTypeDefinition.BOOLEAN, ValueType.BOOLEAN);
		valueTypesByPropertyType.put(DataTypeDefinition.INT, ValueType.INTEGER);
		valueTypesByPropertyType.put(DataTypeDefinition.LONG, ValueType.LONG);
		valueTypesByPropertyType.put(DataTypeDefinition.DOUBLE, ValueType.DOUBLE);
		valueTypesByPropertyType.put(DataTypeDefinition.FLOAT, ValueType.FLOAT);
		valueTypesByPropertyType.put(DataTypeDefinition.DATE, ValueType.DATE);
		valueTypesByPropertyType.put(DataTypeDefinition.DATETIME, ValueType.DATE);
		valueTypesByPropertyType.put(DataTypeDefinition.TEXT, ValueType.STRING);
		valueTypesByPropertyType.put(DataTypeDefinition.ANY, ValueType.SERIALIZABLE);
		valueTypesByPropertyType.put(DataTypeDefinition.INSTANCE, ValueType.INSTANCE);
		valueTypesByPropertyType.put(DataTypeDefinition.URI, ValueType.URI);

		valueTypesByOrdinalNumber = new HashMap<>(37);
		for (ValueType valueType : ValueType.values()) {
			Integer ordinalNumber = valueType.getOrdinalNumber();
			if (valueTypesByOrdinalNumber.containsKey(ordinalNumber)) {
				throw new RuntimeException("ValueType has duplicate ordinal number: " + valueType);
			} else if (ordinalNumber.intValue() == -1) {
				throw new RuntimeException("ValueType doesn't have an ordinal number: " + valueType);
			}
			valueTypesByOrdinalNumber.put(ordinalNumber, valueType);
		}
	}

	/**
	 * Helper method to convert the type name into a <code>ValueType</code>.
	 * 
	 * @param typeQName
	 *            the type q name
	 * @return Returns the <code>ValueType</code> - never null
	 */
	private static ValueType makeValueType(String typeQName) {
		ValueType valueType = valueTypesByPropertyType.get(typeQName);
		if (valueType == null) {
			throw new EmfRuntimeException("Property type not recognised: \n" + "   type: "
					+ typeQName);
		}
		return valueType;
	}

	/**
	 * Given an actual type qualified name, returns the <tt>int</tt> ordinal number that represents
	 * it in the database.
	 * 
	 * @param typeQName
	 *            the type qualified name
	 * @return Returns the <tt>int</tt> representation of the type, e.g.
	 *         <b>CONTENT.getOrdinalNumber()</b> for type <b>d:content</b>.
	 */
	public static int convertToTypeOrdinal(String typeQName) {
		ValueType valueType = makeValueType(typeQName);
		return valueType.getOrdinalNumber();
	}

	/** The id. */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** the type of the property, prior to serialization persistence. */
	private ValueType actualType;

	/** the type of persistence used. */
	private ValueType persistedType;

	/** The boolean value. */
	@Type(type = "com.sirma.itt.emf.entity.customType.BooleanCustomType")
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

	/** The serializable value. */
	@OneToOne(cascade = { CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	private SerializableValue serializableValue;

	/**
	 * Default constructor.
	 */
	public PropertyValue() {
	}

	/**
	 * Construct a new property value.
	 * 
	 * @param typeQName
	 *            the dictionary-defined property type to store the property as
	 * @param value
	 *            the value to store. This will be converted into a format compatible with the type
	 *            given
	 */
	public PropertyValue(String typeQName, Serializable value) {

		if (value == null) {
			actualType = PropertyValue.getActualType(value);
			setPersistedValue(ValueType.NULL, null);
		} else {
			// Convert the value to the type required. This ensures that any
			// type conversion issues
			// are caught early and prevent the scenario where the data in the
			// DB cannot be given
			// back out because it is unconvertable.
			ValueType valueType = makeValueType(typeQName);
			value = valueType.convert(value);

			actualType = PropertyValue.getActualType(value);
			// get the persisted type
			ValueType persistedValueType = actualType.getPersistedType(value);
			// convert to the persistent type
			value = persistedValueType.convert(value);
			setPersistedValue(persistedValueType, value);
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
		if (obj instanceof PropertyValue) {
			PropertyValue that = (PropertyValue) obj;
			return (actualType.equals(that.actualType) && EqualsHelper.nullSafeEquals(
					getPersistedValue(), that.getPersistedValue()));
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
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("PropertyValue").append("[actual-type=").append(actualType)
				.append(", value-type=").append(persistedType).append(", value=")
				.append(getPersistedValue()).append("]");
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
		ValueType type = PropertyValue.valueTypesByOrdinalNumber.get(actualType);
		if (type == null) {
			LOGGER.error("Unknown property actual type ordinal number: " + actualType);
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
		ValueType type = PropertyValue.valueTypesByOrdinalNumber.get(persistedType);
		if (type == null) {
			LOGGER.error("Unknown property persisted type ordinal number: " + persistedType);
		}
		this.persistedType = type;
	}

	/**
	 * Stores the value in the correct slot based on the type of persistence requested. No
	 * conversion is done.
	 * 
	 * @param persistedType
	 *            the value type
	 * @param value
	 *            the value - it may only be null if the persisted type is {@link ValueType#NULL}
	 */
	public void setPersistedValue(ValueType persistedType, Serializable value) {
		switch (persistedType) {
			case NULL:
				if (value != null) {
					throw new EmfRuntimeException("Value must be null for persisted type: "
							+ persistedType);
				}
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
				serializableValue = cloneSerializable(new SerializableValue(value));
				break;
			default:
				throw new EmfRuntimeException("Unrecognised value type: " + persistedType);
		}
		// we store the type that we persisted as
		this.persistedType = persistedType;
	}

	/**
	 * Clones a serializable object to disconnect the original instance from the persisted instance.
	 * 
	 * @param original
	 *            the original object
	 * @return the new cloned object
	 */
	private SerializableValue cloneSerializable(SerializableValue original) {
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				ObjectOutputStream objectOut = new ObjectOutputStream(byteOut)) {
			objectOut.writeObject(original);
			objectOut.flush();

			try (ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream(
					byteOut.toByteArray()))) {
				Object target = objectIn.readObject();
				return (SerializableValue) target;
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
				if (stringValue == null) {
					// We know that we stored a non-null string, but now it is
					// null.
					// It can only mean one thing - Oracle
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("string_value is 'null'.  Forcing to empty String");
					}
					return PropertyValue.STRING_EMPTY;
				}
				return stringValue;
			case SERIALIZABLE:
				return serializableValue.getSerializable();
			default:
				throw new EmfRuntimeException("Unrecognised value type: " + persistedType);
		}
	}

	/**
	 * Fetches the value as a desired type. Collections (i.e. multi-valued properties) will be
	 * converted as a whole to ensure that all the values returned within the collection match the
	 * given type.
	 * 
	 * @param typeQName
	 *            the type required for the return value
	 * @return Returns the value of this property as the desired type, or a <code>Collection</code>
	 *         of values of the required type
	 * @see DataTypeDefinition#ANY The static qualified names for the types
	 */
	public Serializable getValue(String typeQName) {
		// first check for null
		ValueType requiredType = makeValueType(typeQName);
		if (requiredType == ValueType.SERIALIZABLE) {
			// the required type must be the actual type
			requiredType = actualType;
		}

		// we need to convert
		Serializable ret = null;
		if ((actualType == ValueType.COLLECTION) && (persistedType == ValueType.NULL)) {
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
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Fetched value: \n" + "   property value: " + this + "\n"
					+ "   requested type: " + requiredType + "\n" + "   result: " + ret);
		}
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
	 * Gets the serializable value.
	 * 
	 * @return the serializable value
	 */
	public SerializableValue getSerializableValue() {
		return serializableValue;
	}

	/**
	 * Sets the serializable value.
	 * 
	 * @param value
	 *            the new serializable value
	 */
	public void setSerializableValue(SerializableValue value) {
		serializableValue = value;
	}
}
