package com.sirma.sep.instance.properties;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

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
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.properties.PropertiesSuggestService;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.search.SearchService;

/**
 * Base implementation for {@link PropertiesSuggestService} which returns all eligible properties for suggestion. Тhe
 * service supports multiple or single valued results
 *
 * @author svetlozar.iliev
 */
public class PropertiesSuggestServiceImpl implements PropertiesSuggestService {

	private static final String TYPE = "?type";
	private static final List<String> EXCLUDED_TYPES = Collections.singletonList("emf:User");
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String CONTEXT = "targetId";

	/**
	 * Query that walks the hierarchy of the given context with given type.
	 * <p>
	 * <pre>
	 * select distinct ?instance ?instanceType where {
	 * 		?targetId ptop:partOf ?instance .
	 * 		?instance emf:instanceType ?instanceType .
	 * 		?instance rdf:type ?type .
	 * 		?instance emf:isDeleted "false"^^xsd:boolean .
	 * 		optional {
	 * 			?instance ptop:partOf ?parentOfInstance .
	 *        }
	 * 		filter(?type = type1 || ?type = type2, ...)
	 * }
	 * </pre>
	 */
	private static final String HIERARCHY_WALK_BY_TYPE = ResourceLoadUtil.loadResource(
			PropertiesSuggestServiceImpl.class, "HIERARCHY_WALK_BY_TYPE.sparql");

	/**
	 * Query that searches for instances which are related by any relation to the given context of given type.
	 * <p>
	 * <pre>
	 *     SELECT DISTINCT ?instance ?instanceType where {
	 *        ?targetId ?relationType ?instance.
	 *        ?instance emf:isDeleted "false"^^xsd:boolean.
	 *        ?instance a ?type.
	 *        ?instance emf:instanceType ?instanceType.
	 *        ?relationType a ?definitionObjectProperty.
	 *        FILTER(?definitionObjectProperty = emf:DefinitionObjectProperty || ?relationType = emf:parentOf).
	 * </pre>
	 */
	private static final String INSTANCES_RELATED_TO_CONTEXT = ResourceLoadUtil.loadResource(
			PropertiesSuggestServiceImpl.class, "INSTANCES_RELATED_TO_CONTEXT.sparql");

	@Inject
	private SearchService searchService;

	@Inject
	private InstanceTypeResolver typeResolver;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private InstanceContextService contextService;

	public List<String> suggestPropertiesIds(String contextId, String type, boolean multivalued) {
		List<String> result = new ArrayList<>(64);

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
				.map(String::trim)
				.filter(currentType -> !EXCLUDED_TYPES.contains(currentType))
				.collect(Collectors.toList());
	}

	private void populateHierarchyOrRelations(String contextId, List<String> result,
			InstanceReference reference, List<String> types) {
		if (isOfType(reference.getType(), types)) {
			result.add(reference.getId());
		}

		result.addAll(walkHierarchy(contextId, types));

		if (result.isEmpty()) {
			result.addAll(anyRelations(contextId, types));
		}
	}

	private void findClosestParentOrRelated(String contextId, List<String> result,
			InstanceReference reference, List<String> types) {
		if (isOfType(reference.getType(), types)) {
			result.add(reference.getId());
			return;
		}

		InstanceReference closestParent = findClosestParentOfType(reference, types);
		if (closestParent != null) {
			result.add(closestParent.getId());
			return;
		}

		Collection<String> relations = anyRelations(contextId, types);
		if (relations.size() == 1) {
			result.addAll(relations);
		}
	}

	private InstanceReference findClosestParentOfType(InstanceReference reference, List<String> types) {
		Optional<InstanceReference> context = contextService.getContext(reference);
		if (!context.isPresent()) {
			return null;
		}
		if (isOfType(context.get().getType(), types)) {
			return context.get();
		}
		return findClosestParentOfType(context.get(), types);
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
	 * 		the instance id
	 * @param types
	 * 		the types to check for
	 * @return list of found parent instances
	 */
	private Collection<String> walkHierarchy(String id, List<String> types) {
		List<Instance> hierarchy = performSearch(id, appendTypesFilter(HIERARCHY_WALK_BY_TYPE, types));
		return hierarchy.stream().map(instance -> instance.getId().toString()).collect(Collectors.toList());
	}

	private Collection<String> anyRelations(String id, List<String> types) {
		List<Instance> anyRelations = performSearch(id, appendTypesFilter(INSTANCES_RELATED_TO_CONTEXT, types));
		return anyRelations.stream().map(instance -> instance.getId().toString()).collect(Collectors.toList());
	}

	private List<Instance> performSearch(String id, String query) {
		SearchArguments<Instance> args = new SearchArguments<>();
		args.setStringQuery(query);
		args.setDialect(SearchDialects.SPARQL);
		args.setPermissionsType(QueryResultPermissionFilter.READ);
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
	 * 		the query
	 * @param types
	 * 		the types
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
