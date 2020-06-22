package com.sirma.sep.instance.relations;

import static com.sirma.itt.seip.collections.CollectionUtils.throwingMerger;
import static com.sirma.itt.seip.domain.definition.PropertyDefinition.hasUri;
import static com.sirma.itt.seip.domain.definition.PropertyDefinition.isObjectProperty;
import static com.sirma.itt.seip.domain.definition.PropertyDefinition.resolveUri;
import static java.util.Collections.singleton;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.domain.search.SearchArguments.QueryResultPermissionFilter;
import com.sirma.itt.seip.domain.search.Sorter;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.relation.InstanceRelationsService;
import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.permissions.InstanceAccessPermissions;
import com.sirma.itt.seip.permissions.SecurityModel;
import com.sirma.itt.seip.search.ResultItem;
import com.sirma.itt.seip.search.ResultItemTransformer;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Base implementation for {@link InstanceRelationsService}, containing logic for evaluating instance relations. To the
 * relations are applied permissions checks. The service supports evaluation of relations for version instances.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class InstanceRelationsServiceImpl implements InstanceRelationsService {

	private static final Set<String> EXPLICIT_PROPERTIES = new HashSet<>(
			Arrays.asList(DefaultProperties.LOCKED_BY, DefaultProperties.SEMANTIC_TYPE));

	/** Used to retrieve the property from the result item from the search operation. */
	private static final String PROPERTY = "property";

	/** Used to retrieve the values of the properties from the result item from the search operation. */
	private static final String INSTANCE = "instance";

	/**
	 * <pre>
	 * SELECT ?property ?instance WHERE {
	 * 	%s ?property ?instance.
	 * 	?instance emf:isDeleted "false"^^xsd:boolean .
	 * 	FILTER(?property IN (%s)).
	 * }
	 * </pre>
	 *
	 * the first <b>%s</b> is replaced with the id of the instance which properties should be retrieved <br>
	 * the second <b>%s</b> is replaces with comma separated property uris which values should be retrieved
	 */
	private static final String SPECIFIC_PROPERTIES_QUERY = "SELECT DISTINCT ?" + PROPERTY + " ?" + INSTANCE + " WHERE"
			+ " { %s ?" + PROPERTY + " ?" + INSTANCE + ". ?" + INSTANCE + " " + SPARQLQueryHelper.IS_NOT_DELETED
			+ " FILTER(?" + PROPERTY + " IN (%s)). }";

	private static final String URIS_DELIMITER = ", ";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "object.properties.initial.load.limit", type = Integer.class, defaultValue = "5", label = "Configuration for initial load limit for instance object properties.")
	private ConfigurationProperty<Integer> relationsInitialLoadLimit;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private InstanceAccessEvaluator instanceAccessEvaluator;

	@Inject
	private SearchService searchService;

	@Inject
	private NamespaceRegistryService registryService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;
	@Inject
	private InstancePropertyNameResolver fieldConverter;

	@Override
	public List<String> evaluateRelations(Instance instance, String propertyIdentifier, int offset, int limit) {
		Set<String> identifier = singleton(propertyIdentifier);
		Optional<Map<String, List<String>>> relationsMap = evaluateRelationsInternal(instance, identifier, offset, limit);
		if (!relationsMap.isPresent()) {
			throw new EmfRuntimeException("Property [" + propertyIdentifier + "] is not recognized for instance "
					+ instance.get(DefaultProperties.HEADER_LABEL, fieldConverter) + " or it's not object property.");
		}

		if (relationsMap.get().isEmpty()) {
			return Collections.emptyList();
		}

		return relationsMap.get().entrySet().iterator().next().getValue();
	}

	@Override
	public Map<String, List<String>> evaluateRelations(Instance instance, Collection<String> propertyIdentifiers) {
		return evaluateRelationsInternal(instance, propertyIdentifiers, 0, -1).orElseGet(Collections::emptyMap);
	}

	private Optional<Map<String, List<String>>> evaluateRelationsInternal(Instance instance,
			Collection<String> propertyIdentifiers, int offset, int limit) {
		Objects.requireNonNull(instance, "Instance is required!");
		Objects.requireNonNull(propertyIdentifiers, "Property identifer is required!");
		Map<String, List<String>> relations = new HashMap<>(propertyIdentifiers.size());
		Serializable id = instance.getId();

		// when the instance is created(currently doesn't exist), we need to return the default values of the properties
		if (!instanceTypeResolver.resolveReference(id).isPresent()) {
			propertyIdentifiers.forEach(p -> relations.put(p, getObjectPropertiesIds(instance.get(p), offset, limit)));
			return Optional.of(relations);
		}

		Set<String> explicitProperties = getExplicitProperties(propertyIdentifiers);
		Map<String, String> propertyMap = extractUris(instance, propertyIdentifiers, explicitProperties);
		if (propertyMap.isEmpty() && explicitProperties.isEmpty()) {
			return Optional.empty();
		}

		if (!explicitProperties.isEmpty()) {
			explicitProperties.forEach(p -> relations.put(p, getObjectPropertiesIds(instance.get(p), offset, limit)));
			if (propertyMap.isEmpty()) {
				return Optional.of(relations);
			}
		}

		if (InstanceVersionService.isVersion(id)) {
			return Optional.of(propertyMap.entrySet().stream().collect(Collectors.toMap(Entry::getValue,
					entry -> getObjectPropertiesIds(instance.get(entry.getValue()), offset, limit), throwingMerger(),
					() -> relations)));
		}

		SearchArguments<Instance> arguments = buildSearchArguments(id.toString(), propertyMap.keySet(), offset, limit);
		try (Stream<ResultItem> resultStream = searchService.stream(arguments, ResultItemTransformer.asIs())) {
			return Optional.of(resultStream.collect(Collectors.groupingBy(
					item -> propertyMap.get(item.getResultValue(PROPERTY).toString()), () -> relations,
					Collectors.mapping(item -> item.getResultValue(INSTANCE).toString(), Collectors.toList()))));
		}
	}

	private static Set<String> getExplicitProperties(Collection<String> propertyIdentifiers) {
		return EXPLICIT_PROPERTIES.stream().filter(propertyIdentifiers::contains).collect(Collectors.toSet());
	}

	/**
	 * Returns map where the key is the property uri and the value is name. The uris are used to execute search, but we
	 * need the names of the properties in order to return the results as they are requested.
	 */
	private Map<String, String> extractUris(Instance instance, Collection<String> properties,
			Collection<String> explicit) {
		return definitionService
				.getInstanceDefinition(instance)
					.fieldsStream()
					.filter(isRequestedAndNotExplicit(properties, explicit).and(isObjectProperty()).and(hasUri()))
					.map(propertyDef -> new StringPair(resolveUri().apply(propertyDef), propertyDef.getName()))
					.collect(Pair.toMap());
	}

	private static Predicate<PropertyDefinition> isRequestedAndNotExplicit(Collection<String> requestedProperties,
			Collection<String> explicit) {
		return property -> requestedProperties.contains(property.getName()) && !explicit.contains(property.getName());
	}

	/**
	 * Retrieves collection of ids for the property values. Limits the returned ids, if the limit is not negative.
	 */
	private List<String> getObjectPropertiesIds(Serializable propertyValue, int offset, int limit) {
		Set<Serializable> values = PropertiesSerializationUtil
				.convertObjectProperty(propertyValue)
					.collect(Collectors.toSet());

		Map<Serializable, InstanceAccessPermissions> roles = instanceAccessEvaluator.isAtLeastRole(values,
				SecurityModel.BaseRoles.VIEWER, SecurityModel.BaseRoles.MANAGER);
		Stream<Serializable> valuesStream = values
				.stream()
					// when loading object properties of versions, the actual values of the properties are versioned,
					// but we are using original instances for permissions resolving, which causes mismatch of the keys,
					// when the results map with roles is returned
					// so we need to get the id without version suffix for correct permission calculation
					.filter(id -> InstanceAccessPermissions
							.canRead(roles.get(InstanceVersionService.getIdFromVersionId(id))))
					.skip(offset);
		if (limit > 0) {
			valuesStream = valuesStream.limit(limit);
		}

		return valuesStream.map(String.class::cast).collect(Collectors.toList());
	}

	private SearchArguments<Instance> buildSearchArguments(String id, Collection<String> properties, int offset,
			int limit) {
		SearchArguments<Instance> arguments = new SearchArguments<>();
		// we always want all properties returned from the semantic
		// so we can sort them and correct total count
		arguments.setMaxSize(Integer.MAX_VALUE);
		arguments.setPermissionsType(QueryResultPermissionFilter.READ);
		// determine how many items we will return to the caller
		if (limit < 0) {
			// all of them
			arguments.setPageSize(-1);
		} else {
			// the requested limit or the default count
			arguments.setPageSize(limit == 0 ? getDefaultLimitPerInstanceProperty() : limit);
		}
		arguments.setSkipCount(offset);
		arguments.addSorter(Sorter.descendingSorter(DefaultProperties.MODIFIED_ON));
		// this will make sure the id is in short format
		String instanceId = registryService.getShortUri(id);
		arguments.setStringQuery(String.format(SPECIFIC_PROPERTIES_QUERY, instanceId,
				properties.stream().collect(Collectors.joining(URIS_DELIMITER))));
		return arguments;
	}

	@Override
	public int getDefaultLimitPerInstanceProperty() {
		return relationsInitialLoadLimit.get();
	}
}
