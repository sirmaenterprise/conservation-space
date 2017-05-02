package com.sirma.itt.seip.search.rest;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.SearchDialects;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.context.InstanceContextInitializer;
import com.sirma.itt.seip.search.SearchService;

/**
 * Rest service for suggesting values for object properties on instance create. If the property is single valued tries
 * to determine only one value from the context hierarchy and relations. If its multi valued then performs semantic
 * search and returns all found instances.
 *
 * @author smustafov
 */
@ApplicationScoped
@Path("/properties/suggest")
public class PropertiesSuggestRest {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String CONTEXT = "targetId";
	private static final String TYPE = "?type";
	private static final String MULTIVALUED = "multivalued";
	private static final List<String> EXCLUDED_TYPES = Arrays.asList("emf:User");

	/**
	 * Query that walks the hierarchy of the given context with given type.
	 *
	 * <pre>
	 * select distinct ?instance ?instanceType where {
	 * 		?targetId ptop:partOf ?instance .
	 * 		?instance emf:instanceType ?instanceType .
	 * 		?instance rdf:type ?type .
	 * 		?instance emf:isDeleted "false"^^xsd:boolean .
	 * 		optional {
	 * 			?instance ptop:partOf ?parentOfInstance .
	 * 		}
	 * 		filter(?type = type1 || ?type = type2, ...)
	 * }
	 * </pre>
	 */
	private static final String HIERARCHY_WALK_BY_TYPE = "select distinct ?instance ?instanceType where { ?" + CONTEXT
			+ " ptop:partOf ?instance . " + "?instance emf:instanceType ?instanceType . ?instance rdf:type " + TYPE
			+ " . ?instance emf:isDeleted \"false\"^^xsd:boolean . optional { ?instance ptop:partOf ?parentOfInstance .  }";

	/**
	 * Query that searches for instances which are related by any relation to the given context of given type.
	 *
	 * <pre>
	 * select ?instance ?instanceType where {
	 * 		?targetId ?relationType ?instance.
	 * 		?targetId emf:instanceType ?instanceType .
	 * 		?instance emf:isDeleted "false"^^xsd:boolean .
	 * 		?instance a ?type .
	 * 		?relationType a emf:DefinitionObjectProperty .
	 * 		filter(?type = type1 || ?type = type2, etc...)
	 * }
	 * </pre>
	 */
	private static final String INSTANCES_RELATED_TO_CONTEXT = "select distinct ?instance ?instanceType where { ?" + CONTEXT
			+ " ?relationType ?instance . ?instance emf:isDeleted \"false\"^^xsd:boolean . ?instance a " + TYPE + " . ?"
			+ CONTEXT + " emf:instanceType ?instanceType . ?relationType a emf:DefinitionObjectProperty . ";

	@Inject
	private SearchService searchService;

	@Inject
	private InstanceTypeResolver typeResolver;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private InstanceContextInitializer contextInitializer;

	/**
	 * Searches suggested values for given context id, type and if its multivalued.
	 *
	 * @param contextId
	 *            the context id
	 * @param type
	 *            the type of the property
	 * @param multivalued
	 *            if the property allows multiple values
	 * @return list of the suggested values
	 */
	@GET
	public List<Instance> suggest(@QueryParam(CONTEXT) String contextId, @QueryParam("type") String type,
			@QueryParam(MULTIVALUED) boolean multivalued) {
		List<Instance> result = new ArrayList<>(64);

		List<String> types = extractTypes(type);
		if (contextId.isEmpty() || types.isEmpty()) {
			return result;
		}

		Optional<InstanceReference> contextInstanceReferenceOptional = typeResolver.resolveReference(contextId);
		if (contextInstanceReferenceOptional.isPresent()) {
			InstanceReference reference = contextInstanceReferenceOptional.get();
			if (!multivalued) {
				findClosestParentOrRelated(contextId, result, reference, types);
			} else {
				populateHierarchyOrRelations(contextId, result, reference, types);
			}
		}

		LOGGER.debug("Found {} number of instances for suggesting", result.size());
		return result;
	}

	private static List<String> extractTypes(String type) {
		List<String> types = Arrays.asList(type.split(","));
		return types.stream()
				.map(currentType -> currentType.trim())
				.filter(currentType -> !EXCLUDED_TYPES.contains(currentType))
				.collect(Collectors.toList());
	}

	private void findClosestParentOrRelated(String contextId, List<Instance> result, InstanceReference reference,
			List<String> types) {
		if (isOfType(reference.getType(), types)) {
			result.add(reference.toInstance());
			return;
		}

		contextInitializer.restoreHierarchy(reference);
		InstanceReference closestParent = findClosestParentOfType(reference, types);
		if (closestParent != null) {
			result.add(closestParent.toInstance());
			return;
		}

		Collection<Instance> relations = anyRelations(contextId, types);
		if (relations.size() == 1) {
			result.addAll(relations);
		}
	}

	private void populateHierarchyOrRelations(String contextId, List<Instance> result, InstanceReference reference,
			List<String> types) {
		if (isOfType(reference.getType(), types)) {
			result.add(reference.toInstance());
		}

		result.addAll(walkHierarchy(contextId, types));

		if (result.isEmpty()) {
			result.addAll(anyRelations(contextId, types));
		}
	}

	private InstanceReference findClosestParentOfType(InstanceReference reference, List<String> types) {
		InstanceReference parent = reference.getParent();
		if (parent.isRoot()) {
			return null;
		}
		if (isOfType(parent.getType(), types)) {
			return parent;
		}
		return findClosestParentOfType(parent, types);
	}

	private ShortUri convertToShortUri(Serializable fullUri) {
		return typeConverter.convert(ShortUri.class, fullUri);
	}

	private boolean isOfType(InstanceType instanceType, List<String> types) {
		ShortUri uri = convertToShortUri(instanceType.getId());
		if (types.contains(uri.toString())) {
			return true;
		}
		for (InstanceType superType : instanceType.getSuperTypes()) {
			uri = convertToShortUri(superType.getId());
			if (types.contains(uri.toString())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Walks the hierarchy of the given instance id with any of the given types.
	 *
	 * @param id
	 *            the instance id
	 * @param types
	 *            the types to check for
	 * @return list of found parent instances
	 */
	private Collection<Instance> walkHierarchy(String id, List<String> types) {
		List<Instance> hierarchy = performSearch(id, appendTypesFilter(HIERARCHY_WALK_BY_TYPE, types));
		List<Serializable> parentIds = hierarchy.stream()
				.map(parent -> parent.getId())
				.collect(Collectors.toList());
		return typeResolver.resolveInstances(parentIds);
	}

	private Collection<Instance> anyRelations(String id, List<String> types) {
		List<Instance> anyRelations = performSearch(id, appendTypesFilter(INSTANCES_RELATED_TO_CONTEXT, types));
		List<Serializable> instancesUris = anyRelations.stream()
				.map(instance -> instance.getId())
				.collect(Collectors.toList());
		return typeResolver.resolveInstances(instancesUris);
	}

	private List<Instance> performSearch(String id, String query) {
		SearchArguments<Instance> args = new SearchArguments<>();
		args.setStringQuery(query);
		args.setDialect(SearchDialects.SPARQL);
		args.setPermissionsType(QueryResultPermissionFilter.NONE);
		args.setFaceted(false);
		args.setMaxSize(-1);

		Map<String, Serializable> bindings = new HashMap<>();
		bindings.put(CONTEXT, id);
		args.setArguments(bindings);

		searchService.search(Instance.class, args);
		return args.getResult();
	}

	/**
	 * Appends filter to the given query with the types as the filter value.
	 *
	 * @param query
	 *            the query
	 * @param types
	 *            the types
	 * @return the constructed query
	 */
	private static String appendTypesFilter(String query, List<String> types) {
		StringBuilder builder = new StringBuilder(" filter(");
		for (int i = 0; i < types.size() - 1; i++) {
			builder.append(TYPE).append(" = ").append(types.get(i)).append(" || ");
		}
		builder.append(TYPE).append(" = ").append(types.get(types.size() - 1));
		builder.append(" )}");

		return query + builder.toString();
	}

}
