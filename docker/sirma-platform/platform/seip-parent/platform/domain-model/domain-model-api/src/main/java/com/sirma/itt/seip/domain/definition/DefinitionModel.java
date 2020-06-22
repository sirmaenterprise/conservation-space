package com.sirma.itt.seip.domain.definition;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.Node;

/**
 * Common definition model for all definitions. Defines list of properties
 *
 * @author BBonev
 */
public interface DefinitionModel extends Serializable, HashableDefinition, Node, Iterable<PropertyDefinition> {

	/**
	 * Gets the list of field definitions. By default the fields are sorted by order
	 *
	 * @return the fields
	 */
	List<PropertyDefinition> getFields();

	/**
	 * Gets the revision.
	 *
	 * @return the revision
	 */
	Long getRevision();

	/**
	 * Returns a stream of all fields of the current definition.
	 *
	 * @return the stream of fields
	 */
	default Stream<PropertyDefinition> fieldsStream() {
		return getFields().stream();
	}

	/**
	 * Gets the fields as mapping by field name.
	 * <p>
	 * The default implementation creates new map each type this method is called. More optimal implementation is to
	 * cache the constructed map.
	 *
	 * @return the fields as map
	 */
	default Map<String, PropertyDefinition> getFieldsAsMap() {
		return fieldsStream().collect(Collectors.toMap(Identity::getIdentifier, Function.identity(), (v1, v2) -> v2));
	}

	/**
	 * Gets the field by name if such exists in the current definition.
	 * <p>
	 * The default implementation searches the properties by traversing the {@link #fieldsStream()}. More optimal
	 * implementation is to use cached mapping provided from {@link #getFieldsAsMap()}.
	 *
	 * @param name
	 *            the name of the field to find
	 * @return an optional with the found field if any, never <code>null</code>
	 */
	default Optional<PropertyDefinition> getField(String name) {
		if (StringUtils.isBlank(name)) {
			return Optional.empty();
		}
		return findField(PropertyDefinition.hasName(name));
	}

	/**
	 * Find the first field that matches the specified predicated.
	 * <p>
	 * The default implementation searches for the property by traversing the {@link #fieldsStream()}.
	 *
	 * @param filter
	 *            the filter to use when searching for property
	 * @return an optional with the found field if any, never <code>null</code>
	 * @see PropertyDefinition#hasName(String)
	 * @see PropertyDefinition#hasUri(String)
	 */
	default Optional<PropertyDefinition> findField(Predicate<PropertyDefinition> filter) {
		if (filter == null) {
			return Optional.empty();
		}
		return fieldsStream().filter(filter).findAny();
	}

	/**
	 * Gets the requested fields their dependencies id any. If all of the requested fields are present in the definition
	 * model the method should return at least the requested fields up to all definition fields. The dependent fields
	 * may be resolved via {@link PropertyDefinition#getDependentFields()}.
	 * <p>
	 * Example:<br>
	 * There is a definition with fields:
	 * <ul>
	 * <li>field1 -&gt; field2
	 * <li>field2,
	 * <li>field3 -&gt; field4,
	 * <li>field4 -&gt; field1, field2
	 * </ul>
	 * Requesting the fields
	 * <ul>
	 * <li>field4 - will result returning the fields: field4, field1, field2
	 * <li>field1, field2, field3 - will result returning the fields field1, field2, field3, field4
	 * <li>field2 - will result returning the fields: field2
	 * </ul>
	 *
	 * @param fields
	 *            the fields to fetch
	 * @return the fields and dependencies
	 */
	default Stream<PropertyDefinition> getFieldsAndDependencies(Collection<String> fields) {
		if (isEmpty(fields)) {
			return Stream.empty();
		}
		Map<String, PropertyDefinition> fieldsMapping = getFieldsAsMap();
		// fetch the requested fields and then get all of their dependencies
		return fields
				.stream()
					.map(fieldsMapping::get)
					.filter(Objects::nonNull)
					.flatMap(field -> Stream.concat(Stream.of(field),
							field.getDependentFields().stream().map(fieldsMapping::get).filter(Objects::nonNull)))
					.distinct();
	}

	/**
	 * Returns an iterator for the all fields of the current definition. The default implementation uses the iterator
	 * returned from the {@link #fieldsStream()}.
	 *
	 * @return the iterator for all fields
	 */
	@Override
	default Iterator<PropertyDefinition> iterator() {
		return fieldsStream().iterator();
	}

	/**
	 * Gets type of the definition. Like - document, project, case, etc.
	 *
	 * @return the type of the definition
	 */
	String getType();

}
