package com.sirma.itt.objects.web.rest;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.emf.rest.EmfRestService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * REST service for working with relationships.
 *
 * @author yasko
 */
@ApplicationScoped
@Path("/relationships")
@Produces(MediaType.APPLICATION_JSON)
public class RelationshipsRestService extends EmfRestService {

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	/**
	 * Provides a list of relationships that exist in the system. If no filters are provided - all searchable
	 * relationships are returned. Relationships can be filtered by domain and range classes of the relationship. When
	 * domain, range or both filters are provided the following logic is performed to filter the relationships:
	 * <ol>
	 * <li>Retrieve all parent and sub classes for each class in a given filter and collect them in a set</li>
	 * <li>If (domain filter is empty OR the filter contains the domain class of the relationship) AND (range filter is
	 * empty OR the filter contains the range class of the relationship) then return the relationship</li>
	 * <ol>
	 *
	 * @param domainFilter
	 *            List of class identifiers to use as domain filter.
	 * @param rangeFilter
	 *            List of class identifiers to use as range filter.
	 * @param rangeFilterIds
	 *            A list of pipe concatenated instance identifiers and instanceTypes (id|type). The semantic classes are
	 *            retrieve from the instance and then merged with the range filter.
	 * @param query
	 *            user generated query to filter out relationships.
	 * @return A JSON array containing all relationships or a filtered list if any filters are provided.
	 */
	@GET
	public String retrieveRelationships(@QueryParam("domainFilter") List<String> domainFilter,
			@QueryParam("rangeFilter") List<String> rangeFilter,
			@QueryParam("rangeFilterIds") List<String> rangeFilterIds, @QueryParam("q") String query) {

		if (isEmpty(domainFilter) && isEmpty(rangeFilter) && isEmpty(rangeFilterIds) && StringUtils.isBlank(query)) {
			return convertPropertyInstancesToJson(semanticDefinitionService.getSearchableRelations());
		}

		Set<PropertyInstance> relations = new HashSet<>(semanticDefinitionService.getSearchableRelations());

		if (isEmpty(domainFilter) && isEmpty(rangeFilter) && isEmpty(rangeFilterIds) && StringUtils.isNotBlank(query)) {
			filterByQuery(relations, query);
			return convertPropertyInstancesToJson(relations);
		}

		Set<String> domain = getEntireClassHierarchies(domainFilter);
		Set<String> range = getEntireClassHierarchies(rangeFilter);
		Set<String> hierarchies = getEntireClassHierarchiesForInstances(rangeFilterIds);

		if (hierarchies != null) {
			range.addAll(hierarchies);
		}

		Iterator<PropertyInstance> iterator = relations.iterator();

		String language = userPreferences.getLanguage();

		while (iterator.hasNext()) {
			PropertyInstance relationship = iterator.next();
			String relationshipDomain = relationship.getDomainClass();
			String relationshipRange = relationship.getRangeClass();

			boolean domainMatches = isEmpty(domain) || domain.contains(relationshipDomain);
			boolean rangeMatches = isEmpty(range) || range.contains(relationshipRange);
			boolean isMatching = StringUtils.containsIgnoreCase(relationship.getLabel(language), query);

			if (!domainMatches || !rangeMatches || !isMatching) {
				iterator.remove();
			}
		}

		return convertPropertyInstancesToJson(relations);
	}

	private void filterByQuery(Set<PropertyInstance> relations, String query) {
		Iterator<PropertyInstance> iterator = relations.iterator();
		String language = userPreferences.getLanguage();
		while (iterator.hasNext()) {
			PropertyInstance relationship = iterator.next();
			if (!StringUtils.containsIgnoreCase(relationship.getLabel(language), query)) {
				iterator.remove();
			}
		}
	}

	private Set<String> getEntireClassHierarchiesForInstances(Collection<String> instanceIds) {
		Set<String> classes = new HashSet<>();
		if (isEmpty(instanceIds)) {
			return classes;
		}

		// TODO: this crap can be simplified when we have load by uri only
		for (String id : instanceIds) {
			String[] split = id.split("\\|");
			Instance instance = loadInstanceInternal(split[1], namespaceRegistryService.getShortUri(split[0]));
			String clazz = null;

			if (instance == null) {
				return null;
			}

			Serializable uri = instance.getProperties().get(DefaultProperties.SEMANTIC_TYPE);
			if (uri != null) {
				clazz = uri.toString();
			}
			if (StringUtils.isBlank(clazz)) {
				DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(split[1]);
				clazz = typeDefinition.getFirstUri();
			}
			classes.add(clazz);
		}
		return getEntireClassHierarchies(classes);
	}

	private Set<String> getEntireClassHierarchies(Collection<String> classes) {
		if (isEmpty(classes)) {
			return new HashSet<>();
		}

		Set<String> branches = new HashSet<>();
		for (String clazz : classes) {
			branches.addAll(semanticDefinitionService.getHierarchy(clazz));
			addSubClasses(clazz, branches);
		}
		return branches;
	}

	private void addSubClasses(String clazz, Set<String> classes) {
		ClassInstance classInstance = semanticDefinitionService.getClassInstance(clazz);
		Set<String> subClasses = classInstance.getSubClasses().keySet();

		for (String subClass : subClasses) {
			classes.add(subClass);
			addSubClasses(subClass, classes);
		}
	}

	/**
	 * Converts a collection of {@link PropertyInstance}s to a JSON array.
	 *
	 * @param properties
	 *            Collection of properties to convert.
	 * @return A JSON array with the converted properties.
	 */
	// TODO: Move to utility class
	private String convertPropertyInstancesToJson(Collection<PropertyInstance> properties) {
		JSONArray result = new JSONArray();
		JSONObject jsonObject = null;
		for (PropertyInstance propertyInstance : properties) {
			jsonObject = new JSONObject();
			JsonUtil.addToJson(jsonObject, "id", propertyInstance.getId().toString());
			JsonUtil.addToJson(jsonObject, DefaultProperties.NAME, propertyInstance.getProperties().get("instance"));
			JsonUtil.addToJson(jsonObject, "domainClass", propertyInstance.getProperties().get("domainClass"));
			JsonUtil.addToJson(jsonObject, "rangeClass", propertyInstance.getProperties().get("rangeClass"));
			JsonUtil.addToJson(jsonObject, DefaultProperties.TITLE,
					propertyInstance.getLabel(userPreferences.getLanguage()));
			result.put(jsonObject);
		}
		return result.toString();
	}
}