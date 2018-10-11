package com.sirma.itt.seip.instance.properties.entity;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.CommonInstance;

/**
 * Provides mapping to convert a real value to enum value of type {@link ValueType} that is used to represent the
 * supported data types in the database.
 *
 * @author BBonev
 */
public class ValueTypeConverter {
	/** used to provide empty collection values in and out. */
	public static final Serializable EMPTY_COLLECTION_VALUE = (Serializable) Collections.emptyList();

	/**
	 * Instantiates a new value type converter.
	 */
	private ValueTypeConverter() {
		// utility class
	}

	/**
	 * The possible value types.
	 */
	public static enum ValueType {

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
			 * @return the persisted type {@link BasePropertyValue#DEFAULT_MAX_STRING_LENGTH} characters will be
			 *         serialized.
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
			 * @return Returns and empty <tt>Collection</tt> if the value is null otherwise it just returns the original
			 *         value
			 */
			@Override
			Serializable convert(Serializable value) {
				if (value == null) {
					return EMPTY_COLLECTION_VALUE;
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
				return TypeConverterUtil.getConverter().convert(InstanceReference.class, value).toInstance();
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
		 * Converts a value to this type. The implementation must be able to cope with any legitimate source value.
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
	public static ValueType getActualType(Serializable value) {
		// if type is not recognized as belonging to any particular slot
		ValueType detectedType = ValueType.SERIALIZABLE;
		if (value == null) {
			detectedType = ValueType.NULL;
		} else if (value instanceof Boolean) {
			detectedType = ValueType.BOOLEAN;
		} else if (value instanceof Integer) {
			detectedType = ValueType.INTEGER;
		} else if (value instanceof Long) {
			detectedType = ValueType.LONG;
		} else if (value instanceof Float) {
			detectedType = ValueType.FLOAT;
		} else if (value instanceof Double) {
			detectedType = ValueType.DOUBLE;
		} else if (value instanceof String) {
			detectedType = ValueType.STRING;
		} else if (value instanceof Date) {
			detectedType = ValueType.DATE;
		} else if (value instanceof CommonInstance) {
			detectedType = ValueType.INSTANCE;
		} else if (value instanceof Instance) {
			detectedType = ValueType.ANY_INSTANCE;
		} else if (value instanceof InstanceReference) {
			detectedType = ValueType.ANY_INSTANCE;
		} else if (value instanceof Uri) {
			detectedType = ValueType.URI;
		}
		return detectedType;
	}

	/**
	 * a mapping from a property type name to the corresponding value type.
	 */
	private static Map<String, ValueType> valueTypesByPropertyType;
	/**
	 * a mapping of {@link ValueType} ordinal number to the enum. This is manually maintained and <b>MUST NOT BE CHANGED
	 * FOR EXISTING VALUES</b>.
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
				throw new EmfRuntimeException("ValueType has duplicate ordinal number: " + valueType);
			} else if (ordinalNumber.intValue() == -1) {
				throw new EmfRuntimeException("ValueType doesn't have an ordinal number: " + valueType);
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
	public static ValueType makeValueType(String typeQName) {
		ValueType valueType = valueTypesByPropertyType.get(typeQName);
		if (valueType == null) {
			throw new EmfRuntimeException("Property type not recognised: \n" + "   type: " + typeQName);
		}
		return valueType;
	}

	/**
	 * Gets the type by original number.
	 *
	 * @param id
	 *            the id
	 * @return the type by original number
	 */
	public static ValueType getTypeByOriginalNumber(Integer id) {
		return valueTypesByOrdinalNumber.get(id);
	}

	/**
	 * Given an actual type qualified name, returns the <tt>int</tt> ordinal number that represents it in the database.
	 *
	 * @param typeQName
	 *            the type qualified name
	 * @return Returns the <tt>int</tt> representation of the type, e.g. <b>CONTENT.getOrdinalNumber()</b> for type
	 *         <b>d:content</b>.
	 */
	public static int convertToTypeOrdinal(String typeQName) {
		ValueType valueType = makeValueType(typeQName);
		return valueType.getOrdinalNumber();
	}
}
