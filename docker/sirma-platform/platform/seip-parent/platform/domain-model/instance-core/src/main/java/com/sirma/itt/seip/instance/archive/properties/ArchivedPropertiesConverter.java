package com.sirma.itt.seip.instance.archive.properties;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PrototypeDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.archive.properties.entity.ArchivedJsonPropertiesEntity;
import com.sirma.itt.seip.instance.properties.entity.ValueTypeConverter;
import com.sirma.itt.seip.instance.properties.entity.ValueTypeConverter.ValueType;

/**
 * Converter used to transform {@link ArchivedJsonPropertiesEntity} properties to instance properties and vice versa.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
class ArchivedPropertiesConverter {
	private static final String VALUE_KEY = "value";
	private static final String TYPE_KEY = "type";

	@Inject
	private DefinitionService definitionService;
	
	@Inject
	private TypeConverter typeConverter;

	/**
	 * Converts instance properties to {@link ArchivedJsonPropertiesEntity} properties. The produced map values will
	 * contain the information for the original type of the properties values, which is need for backward conversion.
	 *
	 * @param version instance which properties should be persisted
	 * @return properties {@link Map} suitable for storing into DB, containing information for restoring the original
	 *         type of the properties
	 */
	Map<String, Serializable> toPersistent(Instance version) {
		DefinitionModel definition = definitionService.getInstanceDefinition(version);
		return version.getProperties().entrySet().stream()
					.map(Pair.from(Entry::getKey, toArchivedProperty(definition)))
					.filter(Pair.nonNullSecond())
					.collect(Pair.toMap());
	}

	private Function<Entry<String, Serializable>, Serializable> toArchivedProperty(DefinitionModel definition) {
		return entry -> {
			PrototypeDefinition prototype = getPropertyPrototype(entry.getKey(), definition, entry.getValue());
			if (prototype == null) {
				return null;
			}
			
			return new ArchivedProperty(typeConverter.tryConvert(String.class, entry.getValue()), prototype.getDataType().getName());
		};
	}

	/**
	 * Retrieves the prototype of properties by name using the current instance definition. <br>
	 * If instance definition is not available, the logic will try to extract the prototype from the value itself. <br>
	 * If the specific property is missing details in the passed definition, the logic will try to extract the prototype
	 * from the value itself.
	 */
	private PrototypeDefinition getPropertyPrototype(String name, DefinitionModel definition, Serializable value) {
		if (definition == null) {
			return definitionService.getDefinitionByValue(name, value);
		}

		PrototypeDefinition prototype = definitionService.getPrototype(name, PathElement.class.cast(definition));
		return prototype != null ? prototype : definitionService.getDefinitionByValue(name, value);
	}

	/**
	 * Converts the {@link ArchivedJsonPropertiesEntity} properties to instance properties. <br>
	 * This conversion is required, because archived properties are stored in specific format, where the values are
	 * holding the information about their original type and they should be processed, before setting them as instance
	 * properties.<br>
	 * The backward conversion will be done via {@link ValueTypeConverter#convertValue(ValueType, Serializable)} where
	 * the stored information about the value type will be used.
	 *
	 * @param toConvert {@link ArchivedJsonPropertiesEntity} properties which should be converted to instance properties
	 * @return new {@link Map} with properties in format suitable for usage in instances
	 */
	@SuppressWarnings("static-method")
	Map<String, Serializable> toInstanceProperties(Map<String, Serializable> toConvert) {
		return toArchivedPropertiesMap(toConvert).entrySet().stream().collect(LinkedHashMap::new,
				(m, e) -> m.put(e.getKey(), unwrapArchivedProperty(e.getValue())), Map::putAll);
	}

	// transforms the object retrieved from the database to ArchivedProperty
	private static Map<String, ArchivedProperty> toArchivedPropertiesMap(Map<String, Serializable> toConvert) {
		return toConvert.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> {
			Map<String, Serializable> map = Map.class.cast(entry.getValue());
			return new ArchivedProperty(map.get(VALUE_KEY), map.get(TYPE_KEY).toString());
		}));
	}

	// unwraps the ArchivedProperty to actual property of specific type, suitable for usage in the instances
	private static Serializable unwrapArchivedProperty(ArchivedProperty property) {
		ValueType valueType = ValueTypeConverter.makeValueType(property.getType());
		return ValueTypeConverter.convertValue(valueType, property.getValue());
	}

	/**
	 * Simple object containing the value and the original type of the properties. Used in conversion between DB entity
	 * data and instance properties.
	 *
	 * @author A. Kunchev
	 */
	static class ArchivedProperty implements Serializable {

		private static final long serialVersionUID = -2267749697350349609L;

		private Serializable value;
		private String type;

		ArchivedProperty(Serializable value, String type) {
			this.value = value;
			this.type = type;
		}

		public Serializable getValue() {
			return value;
		}

		public void setValue(Serializable value) {
			this.value = value;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return new StringBuilder()
					.append("ArchivedProperty [value=").append(value)
						.append(", type=").append(type)
						.append("]").toString();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			if (!super.equals(obj)) {
				return false;
			}
			ArchivedProperty that = ArchivedProperty.class.cast(obj);
			return Objects.equals(value, that.value) && Objects.equals(type, that.type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(value, type);
		}
	}
}