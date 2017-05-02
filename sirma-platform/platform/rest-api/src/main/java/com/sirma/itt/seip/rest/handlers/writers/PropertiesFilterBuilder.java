package com.sirma.itt.seip.rest.handlers.writers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.definition.DefinitionModel;

/**
 * Builds filter based on a given definition. The produced predicate then can be used for filtering properties for
 * different purposes. One of them is for selecting witch fields to be returned to the client.<br>
 * The benefit of using this filter builder instead of simple predicate is that the predicate could be build dependent
 * on the definition lazily
 *
 * @author BBonev
 */
@FunctionalInterface
public interface PropertiesFilterBuilder {

	/** {@link PropertiesFilterBuilder} that will match any field. */
	static PropertiesFilterBuilder MATCH_ALL = model -> field -> true;
	/** {@link PropertiesFilterBuilder} that will not match any field. */
	static PropertiesFilterBuilder MATCH_NONE = model -> field -> false;

	/**
	 * Creates predicate which could be used for properties filtering. The passed collection should contain only
	 * properties that should be added to the model. If the passed collection is empty or null, generated predicate will
	 * be: <b>{@code (k,v) -> false }</b>, which will remove all the properties from the instance.
	 *
	 * @param properties
	 *            properties to be filtered
	 * @return true if the passed properties contains the key of the entry, false otherwise
	 */
	static Predicate<String> onlyProperties(Collection<String> properties) {
		if (CollectionUtils.isEmpty(properties)) {
			return k -> false;
		}

		Set<String> filter = new HashSet<>(properties);
		return filter::contains;
	}

	/**
	 * Builds a filter that will be for the given {@link DefinitionModel}. The builder could build a static or dynamic
	 * filter based on the provided model. The filter will be used against fields provided from the given model
	 * instance.
	 *
	 * @param model
	 *            the model to use for filter building
	 * @return the that can test field identifier
	 */
	Predicate<String> buildFilter(DefinitionModel model);
}
