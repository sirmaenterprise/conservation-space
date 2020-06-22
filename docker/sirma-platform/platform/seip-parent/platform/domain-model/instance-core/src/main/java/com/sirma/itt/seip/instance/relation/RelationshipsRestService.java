package com.sirma.itt.seip.instance.relation;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.security.UserPreferences;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * REST service for working with relations.
 *
 * @author yasko
 */
@ApplicationScoped
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class RelationshipsRestService {

	@Inject
	private SemanticDefinitionService semanticDefinitionService;
	@Inject
	private UserPreferences userPreferences;
	@Inject
	private DefinitionService definitionService;
	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	/**
	 * Provides a information of relationship with id <code>relationShipId</code>
	 * Example ot returned json:
	 *
	 * <pre>
	 *     {
	 *        "domainClass": "ptop:Entity",
	 *        "id": "emf:createdBy",
	 *        "name": "emf:createdBy",
	 *        "rangeClass": "ptop:Agent",
	 *        "title": "Created By"
	 *     }
	 * </pre>
	 *
	 * @param relationShipId - relation id which information looking for.
	 * @return Information of relation with id <code>relationShipId</code> or empty json.
	 */
	@GET
	@Path("relationships/{relationShipId}/info")
	public String retrieveRelationship(@PathParam("relationShipId") String relationShipId) {
		return convertPropertyInstanceToJson(semanticDefinitionService.getRelation(relationShipId)).map(
				JSONObject::toString).orElse("{}");
	}

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
	 * @param domainFilter   List of class identifiers to use as domain filter.
	 * @param rangeFilter    List of class identifiers to use as range filter.
	 * @param rangeFilterIds A list of pipe concatenated instance identifiers and instanceTypes (id|type). The semantic classes are
	 *                       retrieve from the instance and then merged with the range filter.
	 * @param query          user generated query to filter out relationships.
	 * @return A JSON array containing all relationships or a filtered list if any filters are provided.
	 */
	@GET
	@Path("relationships")
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

		range.addAll(hierarchies);

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
			Instance instance = instanceTypeResolver.resolveReference(split[1])
					.map(InstanceReference::toInstance)
					.orElse(null);
			String clazz = null;

			if (instance == null) {
				return Collections.emptySet();
			}

			Serializable uri = instance.getProperties().get(DefaultProperties.SEMANTIC_TYPE);
			if (uri != null) {
				clazz = uri.toString();
			}
			if (StringUtils.isBlank(clazz)) {
				DataTypeDefinition typeDefinition = definitionService.getDataTypeDefinition(split[1]);
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
	 * @param properties Collection of properties to convert.
	 * @return A JSON array with the converted properties.
	 */
	// TODO: Move to utility class
	private String convertPropertyInstancesToJson(Collection<PropertyInstance> properties) {
		JSONArray result = new JSONArray();
		for (PropertyInstance propertyInstance : properties) {
			convertPropertyInstanceToJson(propertyInstance).ifPresent(result::put);
		}
		return result.toString();
	}

	private Optional<JSONObject> convertPropertyInstanceToJson(PropertyInstance propertyInstance) {
		if (propertyInstance == null) {
			return Optional.empty();
		}
		JSONObject jsonObject = new JSONObject();
		JsonUtil.addToJson(jsonObject, "id", propertyInstance.getId().toString());
		JsonUtil.addToJson(jsonObject, DefaultProperties.NAME, propertyInstance.getProperties().get("instance"));
		JsonUtil.addToJson(jsonObject, "domainClass", propertyInstance.getProperties().get("domainClass"));
		JsonUtil.addToJson(jsonObject, "rangeClass", propertyInstance.getProperties().get("rangeClass"));
		JsonUtil.addToJson(jsonObject, DefaultProperties.TITLE,
						   propertyInstance.getLabel(userPreferences.getLanguage()));
		return Optional.of(jsonObject);
	}
}