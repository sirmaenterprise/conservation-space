package com.sirma.itt.seip.instance.relation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Provides means for retrieving relation(object) properties for instance. The service supports evaluation of relations
 * of version instances.
 *
 * @author A. Kunchev
 */
public interface InstanceRelationsService {

	/**
	 * Evaluates all relations for specific property for given instance.
	 *
	 * @param instance which relations property will be resolved
	 * @param propertyIdentifier the id of the relation property
	 * @return {@link List} of values for the specified property
	 */
	default List<String> evaluateRelations(Instance instance, String propertyIdentifier) {
		return evaluateRelations(instance, propertyIdentifier, 0, -1);
	}

	/**
	 * Evaluates relations for specific property for given instance. The result could be limited to specified number of
	 * elements.
	 *
	 * @param instance which relations property will be resolved
	 * @param propertyIdentifier the id of the relation property
	 * @param offset the number of the leading elements that should be skipped
	 * @param limit the number of elements that result should be limited to
	 * @return {@link List} of values for the specified property
	 */
	List<String> evaluateRelations(Instance instance, String propertyIdentifier, int offset, int limit);

	/**
	 * Evaluates relations for specific properties of given instance.
	 *
	 * @param instance which relations properties will be resolved
	 * @param propertyIdentifiers the ids of the relation properties
	 * @return {@link Map} where the key is the id of property and the value is {@link List} of values for that property
	 */
	Map<String, List<String>> evaluateRelations(Instance instance, Collection<String> propertyIdentifiers);

	/**
	 * Retrieves the default limit for the result elements per relation property. Could be used for optimization on
	 * initial instance loading.
	 *
	 * @return default number of elements that should be evaluated per relation property
	 */
	int getDefaultLimitPerInstanceProperty();
}
