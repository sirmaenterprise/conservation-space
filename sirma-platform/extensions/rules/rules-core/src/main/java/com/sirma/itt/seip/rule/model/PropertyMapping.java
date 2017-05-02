/*
 *
 */
package com.sirma.itt.seip.rule.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * DTO for property mapping between two properties and on duplicate configuration.
 *
 * @author BBonev
 */
public class PropertyMapping {

	public static final String NAME = "propertyMapping";

	/** The from. */
	final String from;
	/** The to. */
	final String to;
	final Boolean required;
	/** The on duplicate. */
	OnDuplicateConfig onDuplicate;
	final String value;
	final PropertyValueChange valueChange;

	/**
	 * Instantiates a new property mapping.
	 *
	 * @param from
	 *            the from
	 * @param value
	 *            the value
	 */
	public PropertyMapping(String from, String value) {
		this(from, null, null, null, value, null);
	}

	/**
	 * Instantiates a new property mapping.
	 *
	 * @param from
	 *            the from
	 * @param valueChange
	 *            the value change
	 */
	public PropertyMapping(String from, PropertyValueChange valueChange) {
		this(from, null, null, null, null, valueChange);
	}

	/**
	 * Instantiates a new property mapping.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param required
	 *            the required
	 * @param onDuplicate
	 *            the on duplicate
	 */
	public PropertyMapping(String from, String to, Boolean required, OnDuplicateConfig onDuplicate) {
		this(from, to, required, onDuplicate, null, null);
	}

	/**
	 * Instantiates a new property mapping.
	 *
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param required
	 *            the required
	 * @param onDuplicate
	 *            the on duplicate
	 * @param value
	 *            the value
	 * @param valueChange
	 *            the value change
	 */
	public PropertyMapping(String from, String to, Boolean required, OnDuplicateConfig onDuplicate, String value,
			PropertyValueChange valueChange) {
		this.from = from;
		this.to = to;
		this.required = required == null ? Boolean.TRUE : required;
		this.onDuplicate = onDuplicate;
		this.value = value;
		this.valueChange = valueChange == null ? PropertyValueChange.CHANGED : valueChange;
	}

	/**
	 * Parses the given collection mappings to build collection of properties mappings
	 *
	 * @param mappings
	 *            the mappings
	 * @return the collection of mappings
	 */
	@SuppressWarnings("unchecked")
	public static Collection<PropertyMapping> parse(Collection<?> mappings) {
		Collection<PropertyMapping> propertyMapping = Collections.emptyList();
		if (!CollectionUtils.isEmpty(mappings)) {
			propertyMapping = new ArrayList<>(mappings.size());
			for (Object object : mappings) {
				if (object instanceof Map) {
					CollectionUtils.addNonNullValue(propertyMapping, parse((Map<String, Object>) object));
				}
			}
		}
		return propertyMapping;
	}

	/**
	 * Parses the given configuration to {@link PropertyMapping}
	 *
	 * @param map
	 *            the map
	 * @return the property mapping
	 */
	@SuppressWarnings("unchecked")
	public static PropertyMapping parse(Map<String, Object> map) {
		if (map == null) {
			return null;
		}
		String from = (String) map.get("from");
		String to = (String) map.get("to");
		String value = (String) map.get("value");
		PropertyValueChange valueChange = PropertyValueChange.parse(map.get("mode"));
		if (StringUtils.isEmpty(from)) {
			return null;
		}
		Object requiredValue = map.get("required");
		Boolean required = Boolean.TRUE;
		if (requiredValue instanceof Boolean) {
			required = (Boolean) requiredValue;
		}
		OnDuplicateConfig onDuplicate = OnDuplicateConfig.parse((Map<String, Object>) map.get("onDuplicate"));
		return new PropertyMapping(from, to, required, onDuplicate, value, valueChange);
	}

	/**
	 * Creates mapping from the given object. The returned map should be valid for calling the {@link #parse(Map)}
	 * method.
	 *
	 * @param mapping
	 *            the mapping object to read
	 * @return the map containing the properties of the object or <code>null</code> if the source is null
	 */
	public static Map<String, Object> toMap(PropertyMapping mapping) {
		if (mapping == null) {
			return Collections.emptyMap();
		}
		Map<String, Object> map = CollectionUtils.createHashMap(4);
		CollectionUtils.addNonNullValue(map, "from", mapping.getFrom());
		CollectionUtils.addNonNullValue(map, "to", mapping.getTo());
		CollectionUtils.addNonNullValue(map, "required", mapping.isRequired());
		CollectionUtils.addNonNullValue(map, "onDuplicate", OnDuplicateConfig.toMap(mapping.getOnDuplicate()));
		CollectionUtils.addNonNullValue(map, "value", mapping.getValue());
		if (mapping.getValueChange() != null) {
			CollectionUtils.addNonNullValue(map, "mode", String.valueOf(mapping.getValueChange()));
		}
		return map;
	}

	/**
	 * Getter method for from.
	 *
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * Getter method for to.
	 *
	 * @return the to
	 */
	public String getTo() {
		return to;
	}

	/**
	 * Getter method for onDuplicate.
	 *
	 * @return the onDuplicate
	 */
	public OnDuplicateConfig getOnDuplicate() {
		return onDuplicate;
	}

	/**
	 * Sets the on duplicate.
	 *
	 * @param onDuplicateConfig
	 *            the on duplicate config
	 * @return the on duplicate config
	 */
	public void setOnDuplicate(OnDuplicateConfig onDuplicateConfig) {
		onDuplicate = onDuplicateConfig;
	}

	/**
	 * Checks if is required.
	 *
	 * @return the required
	 */
	public boolean isRequired() {
		return required.booleanValue();
	}

	/**
	 * Getter method for value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Getter method for valueChange.
	 *
	 * @return the valueChange
	 */
	public PropertyValueChange getValueChange() {
		return valueChange;
	}

}