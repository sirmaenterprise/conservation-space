package com.sirma.sep.model.management.converter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.label.Displayable;
import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.AbstractModelNode;
import com.sirma.sep.model.management.ModelAttribute;

/**
 * Utilities for {@link com.sirma.sep.model.ModelNode} and related API classes conversions.
 *
 * @author Mihail Radkov
 */
public class ModelConverterUtilities {

	private ModelConverterUtilities() {
		// Prevent instantiation
	}

	/**
	 * Inserts {@link com.sirma.sep.model.management.ModelAttribute} into the provided model.
	 * <p>
	 * Based on the attribute name, it will resolve the attribute's type from the supplied model meta information.
	 * <p>
	 * The provided attribute value will be converted to {@link String}
	 *
	 * @param model the model in which attribute will  be inserted
	 * @param name the attribute name
	 * @param value the attribute value
	 * @throws IllegalArgumentException if the node lacks meta information for the given name
	 */
	public static void addStringAttribute(AbstractModelNode model, String name, Serializable value) {
		if (value != null) {
			addAttribute(model, name, value.toString());
		}
	}

	/**
	 * Inserts {@link com.sirma.sep.model.management.ModelAttribute} into the provided model.
	 * <p>
	 * Based on the attribute name, it will resolve the attribute's type from the supplied model meta information.
	 *
	 * @param model the model in which attribute will  be inserted
	 * @param name the attribute name
	 * @param value the attribute value
	 * @throws IllegalArgumentException if the node lacks meta information for the given name
	 */
	public static void addAttribute(AbstractModelNode model, String name, Object value) {
		if (value != null) {
			if (model.getAttributesMetaInfo().containsKey(name)) {
				model.addAttribute(name, value);
			} else {
				throw new IllegalArgumentException("Missing meta information for " + name + " in " + model.getId());
			}
		}
	}

	/**
	 * Assigns labels from a provided {@link LabelDefinition} to the supplied {@link AbstractModelNode} based on the {@link Displayable}
	 * label identifier.
	 *
	 * @param displayable used to obtain label identifier for label resolving
	 * @param modelNode target for assigning resolved labels
	 * @param labelProvider provides labels map for the provided label identifier
	 */
	public static void addLabels(Displayable displayable, AbstractModelNode modelNode,
			Function<String, Map<String, String>> labelProvider) {
		if (StringUtils.isNotBlank(displayable.getLabelId())) {
			Map<String, String> labels = labelProvider.apply(displayable.getLabelId());
			if (CollectionUtils.isNotEmpty(labels)) {
				modelNode.setLabels(labels);
			}
		}
	}

	/**
	 * Supplies a {@link ModelAttribute} value to a value {@link Consumer} by searching the given {@link ModelNode}'s attributes.
	 * <p>
	 * This ignores hierarchy resolving of attributes.
	 * <p>
	 * If the node lacks such attribute then <code>null</code> will be provided to the value {@link Consumer}.
	 *
	 * @param node the node which attributes will be searched
	 * @param attributeName the name of the attribute to search for
	 * @param valueConsumer the consumer of the found attribute's value
	 * @param <V> the value type
	 */
	public static <V> void copyAttribute(ModelNode node, String attributeName, Consumer<V> valueConsumer) {
		copyAttribute(node, attributeName, value -> (V) value, valueConsumer);
	}

	/**
	 * Supplies a {@link ModelAttribute} value to a value {@link Consumer} by searching the given {@link ModelNode}'s attributes.
	 * If the attribute is found, its value will be converted via the provided converter.
	 * <p>
	 * This ignores hierarchy resolving of attributes.
	 * <p>
	 * If the node lacks such attribute then <code>null</code> will be provided to the value {@link Consumer}.
	 *
	 * @param node the node which attributes will be searched
	 * @param attributeName the name of the attribute to search for
	 * @param valueConverter a converter for the found attribute's value
	 * @param valueConsumer the consumer of the found attribute's value
	 * @param <V> the value type
	 */
	public static <V> void copyAttribute(ModelNode node, String attributeName, Function<Object, V> valueConverter,
			Consumer<V> valueConsumer) {
		if (node.hasAttribute(attributeName)) {
			node.getAttribute(attributeName).ifPresent(convertAndConsume(valueConverter, valueConsumer));
		} else {
			valueConsumer.accept(null);
		}
	}

	private static <V> Consumer<ModelAttribute> convertAndConsume(Function<Object, V> valueConverter, Consumer<V> valueConsumer) {
		return attribute -> {
			if (attribute.getValue() != null) {
				valueConsumer.accept(valueConverter.apply(attribute.getValue()));
			} else {
				throw new IllegalArgumentException("Attribute " + attribute.getName() + " has null for value which cannot be converted!");
			}
		};
	}

	/**
	 * Function that parses provided object to a {@link Integer}.
	 * <p>
	 * Can be combined with {@link ModelConverterUtilities#copyAttribute(ModelNode, String, Function, Consumer)}
	 *
	 * @return parsing function for {@link Integer}
	 */
	public static Function<Object, Integer> toInteger() {
		return v -> {
			if (v instanceof Integer) {
				return (Integer) v;
			}
			String value = Objects.toString(v);
			return StringUtils.isNotBlank(value) ? Integer.valueOf(value) : null;
		};
	}

	/**
	 * A function that parses provided object to a {@link DisplayType}.
	 * <p>
	 * Can be combined with {@link ModelConverterUtilities#copyAttribute(ModelNode, String, Function, Consumer)}
	 *
	 * @return parsing function for {@link DisplayType}
	 */
	public static Function<Object, DisplayType> toDisplayType() {
		return v -> DisplayType.parse(v.toString());
	}

	/**
	 * Normalizes the provided labels {@link Map} by converting the language keys in lower case.
	 *
	 * @param map the map to transform
	 * @return the normalized {@link Map} or empty one if it was <code>null</code>
	 */
	public static Map<String, String> normalizeLabelsMap(Map map) {
		if (CollectionUtils.isNotEmpty(map)) {
			Map<String, String> labels = (Map<String, String>) map;
			Set<String> languages = new LinkedHashSet<>(labels.keySet());
			languages.forEach(lang -> labels.put(lang.toLowerCase(), labels.remove(lang)));
			return labels;
		}
		return new HashMap<>();
	}

	/**
	 * A function that convert provided object to String.
	 *
	 * @return null if object is null or {@link Object#toString()} method return blank string.
	 */
	public static Function<Object, String> toStringOrNullIfBlank() {
		return value -> {
			if (value == null) {
				return null;
			}
			String toString = value.toString();
			return StringUtils.isBlank(toString) ? null : toString;
		};
	}
}