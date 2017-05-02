package com.sirma.itt.seip.search;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sirma.itt.seip.Resettable;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchableProperty;

/**
 * Service for extracting searchable properties from the system.
 *
 * @author Mihail Radkov
 */
public interface SearchablePropertiesService extends Resettable {

	/**
	 * Retrieves the searchable definition properties for the provided type that have a corresponding property in the
	 * semantics and intersects them with those indexed in Solr if specified.
	 *
	 * @param forType
	 *            - the provided type
	 * @param commonOnly
	 *            - tells if the service should return the intersection of the properties
	 * @param multiValued
	 *            - indicates whether the definition properties should be compared to the multi-valued fields in solr or
	 *            to their single valued equivalents
	 * @param skipObjectProperties
	 *            indicates whether to skip the object properties or to return them as well as data properties
	 * @return the extracted searchable properties
	 */
	List<SearchableProperty> getSearchableSolrProperties(String forType, Boolean commonOnly, Boolean multiValued,
			Boolean skipObjectProperties);

	/**
	 * Retrieves the searchable definition properties for the provided type that have a corresponding property in the
	 * semantics but does <b>not</b> intersect them with those indexed in Solr.
	 *
	 * @param forType
	 *            - the provided type
	 * @return the extracted searchable properties
	 */
	List<SearchableProperty> getSearchableSemanticProperties(String forType);

	/**
	 * Gets all searchable properties for given instance. Gets properties for all searchable classes in the system or
	 * get properties for given definition.
	 *
	 * @param instance
	 *            - the instance which searchable properties will be found
	 * @param type
	 *            - the semantic type (shortUri)
	 * @param definitionId
	 *            - the definition identifier needed for fields extraction
	 * @return all definition properties for a searchable type
	 */
	Map<String, List<PropertyDefinition>> getTypeFields(Instance instance, String type, String definitionId);

	/**
	 * Retrieve the {@link SearchableProperty} by it's id and the definition it's in.
	 *
	 * @param forType
	 *            the for type
	 * @param propertyId
	 *            the id of the property
	 * @return the {@link SearchableProperty}
	 */
	Optional<SearchableProperty> getSearchableProperty(String forType, String propertyId);

}
