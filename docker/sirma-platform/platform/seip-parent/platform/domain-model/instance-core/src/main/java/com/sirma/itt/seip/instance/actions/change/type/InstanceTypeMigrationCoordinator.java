package com.sirma.itt.seip.instance.actions.change.type;

import static com.sirma.itt.seip.domain.definition.PropertyDefinition.hasUri;
import static com.sirma.itt.seip.domain.definition.PropertyDefinition.resolveUri;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.Trackable;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.definition.StateTransition;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.CMInstance;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.instance.PropertyInstance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.dao.InstanceLoadDecorator;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.instance.validation.DynamicCodeListFilter;
import com.sirma.itt.seip.instance.validator.DynamicCodelistFiltersExtractor;
import com.sirma.itt.seip.rest.exceptions.ResourceNotFoundException;
import com.sirma.itt.seip.search.ResultItemTransformer;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.itt.semantic.model.vocabulary.Proton;

/**
 * Helper class that provides means of converting the instance properties to other target model
 * (semantic type and/or definition).
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/02/2019
 */
@Singleton
public class InstanceTypeMigrationCoordinator {

	/**
	 * Search for instances that have properties pointing to the {@code target} instance that have the following criteria
	 * <ul>
	 * <li>does not have inverse relations</li>
	 * <li>are not symmetric properties</li>
	 * <li>have a range defined</li>
	 * <li>the defined range is not a sub type of the specified new target type</li>
	 * </ul>
	 */
	private static final String SEARCH_RELATED_INSTANCES = "SELECT DISTINCT ?instance WHERE {\n"
			+ "?instance ?property ?target.\n"
			+ "?instance emf:isDeleted \"false\"^^xsd:boolean.\n"
			+ "?property a emf:DefinitionObjectProperty.\n"
			+ "?property rdfs:range ?range.\n"
			+ "filter not exists { ?property owl:inverseOf ?inverse. ?target ?inverse ?instance. }\n"
			+ "filter not exists { ?property a owl:SymmetricProperty. }\n"
			+ "filter not exists { ?newType rdfs:subClassOf ?range. } \n"
			+ "filter (?target != ?instance).\n"
			+ "}";

	private static final String PTOP_DOCUMENT = Proton.NAMESPACE + "Document";
	private static final String EMF_MEDIA = EMF.NAMESPACE + "Media";

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "instance.actions.changeType.skippedTypes", type = Set.class,
			defaultValue = PTOP_DOCUMENT + "," + EMF_MEDIA, subSystem = "actions", label = "Defines which classes should "
			+ "be skipped from hierarchy resolving during calculation for change type. The configured classes will be "
			+ "ignored and the search will continue up in the hierarchy.")
	private ConfigurationProperty<Set<String>> skippedClasses;

	@Inject
	private DomainInstanceService domainInstanceService;
	@Inject
	private SemanticDefinitionService semanticDefinitionService;
	@Inject
	private DefinitionService definitionService;
	@Inject
	private InstanceLoadDecorator loadDecorators;
	@Inject
	private CodelistService codelistService;
	@Inject
	private SearchService searchService;
	@Inject
	private InstanceService instanceService;

	/**
	 * Loads and converts the property names of the original instance to be represented by the given definition.
	 * The definition should be from the same hierarchy of the original semantic class. This means that the new
	 * definition should be of a semantic class that is sibling of the current type or sub type of it. These rules apply
	 * also for the definitions. <br>
	 * The given target definition should not be defined abstract. If this is the case then
	 * {@link IllegalArgumentException} will be thrown.<br>
	 * The implementation will perform relation filtering based on the domain of the relations in a such way that any
	 * property with defined domain should be equals or super type of the new type. If this is not met then this
	 * relation will not be copied and later deleted if instance is saved with the given type.
	 *
	 * @param id the id of the instance to convert
	 * @param asType the new non abstract type to apply.
	 * @return an instance copy that has the same original id but properties mapped to the new target model.
	 */
	Instance getInstanceAs(String id, String asType) {
		Instance instance = getInstance(id);
		if (EqualsHelper.nullSafeEquals(instance.getIdentifier(), asType)) {
			// the type is the same just return the instance
			return instance;
		}
		DefinitionModel newDefinition = findDefinitionByName(asType);

		validateDefinitionCompatibility(instance, newDefinition);

		Instance instanceCopy = createInstanceCopy(instance, newDefinition);

		// regenerate instance headers
		loadDecorators.clearDecoratedStatus(instanceCopy);
		loadDecorators.decorateInstance(instanceCopy);
		return instanceCopy;
	}

	private Instance createInstanceCopy(Instance instance, DefinitionModel newDefinition) {
		InstanceType newSemanticType = getSemanticType(newDefinition);

		Instance instanceCopy = createInstance(instance, newDefinition.getIdentifier(), newSemanticType);
		remapInstanceProperties(instance, instanceCopy, newDefinition, newSemanticType);

		// make sure the content id is copied no matter if the definition have it or not
		// this will ensure that the instance is not broken and no longer considered for uploadable
		instanceCopy.addIfNotNull(DefaultProperties.PRIMARY_CONTENT_ID, instance.get(DefaultProperties.PRIMARY_CONTENT_ID));
		// override semantic type as it's transferred from the original instance
		instanceCopy.add(DefaultProperties.SEMANTIC_TYPE, newSemanticType.getId());
		// update the type field value as it should match the new definition
		newDefinition.getField(DefaultProperties.TYPE)
				.ifPresent(type -> instanceCopy.add(type.getName(), type.getDefaultValue()));
		// remove the old template so we can pick new one that matches the new type and sub type
		newDefinition.getField(LinkConstants.HAS_TEMPLATE)
				.map(PropertyDefinition::getName)
				.ifPresent(instanceCopy::remove);

		runFiltersOnFields(instanceCopy, newDefinition);

		// copy or reset status based on the destination type
		setProperInstanceStatus(instanceCopy, newDefinition);
		return instanceCopy;
	}

	private void setProperInstanceStatus(Instance instanceCopy, DefinitionModel newDefinition) {
		GenericDefinition genericDefinition = (GenericDefinition) newDefinition;

		PropertyDefinition statusField = newDefinition.getField(DefaultProperties.STATUS)
				.orElseThrow(() -> new IllegalArgumentException(
						"Could not find status field in the target model " + newDefinition.getIdentifier()));

		String currentStatus = instanceCopy.getString(DefaultProperties.STATUS);
		boolean isValidStatusCode = true;
		if (statusField.getCodelist() != null) {
			isValidStatusCode = codelistService.getCodeValue(statusField.getCodelist(), currentStatus) != null;
			isValidStatusCode &= genericDefinition.getStateTransitions()
						.stream()
						.anyMatch(hasChangeTypeTransitionFromState(currentStatus));
			// if the status is considered valid here this means there is a state transition defined
			// with the current status and operation
		}
		if (!isValidStatusCode) {
			boolean hasInitialTransition = genericDefinition.getStateTransitions()
					.stream()
					.anyMatch(hasChangeTypeTransitionFromState(PrimaryStates.INITIAL_KEY));

			if (hasInitialTransition) {
				// reset the instance status to match the new type
				// the new status will be as the instance is just created
				// this is done as the user cannot change it in the change type dialog as this field is hidden
				instanceCopy.add(DefaultProperties.STATUS, PrimaryStates.INITIAL_KEY);
			} else {
				throw new IllegalArgumentException("Could not change type to " + newDefinition.getIdentifier()
						+ ". No defined state transition for operation " + ChangeTypeRequest.OPERATION_NAME);
			}
		}
	}

	private Predicate<StateTransition> hasChangeTypeTransitionFromState(String currentStatus) {
		return stateTransition -> stateTransition.getFromState().equals(currentStatus)
				&& ChangeTypeRequest.OPERATION_NAME.equals(stateTransition.getTransitionId());
	}

	private void remapInstanceProperties(Instance originalInstance, Instance targetInstance, DefinitionModel newDefinition,
			InstanceType newSemanticType) {
		Map<String, Serializable> uriToValueMapping = getRawInstanceProperties(originalInstance);

		Map<String, String> newUriToNameMapping = newDefinition.fieldsStream()
				.filter(hasUri())
				.collect(Collectors.toMap(resolveUri(), PropertyDefinition::getName));

		// convert originalInstance properties from one type to other based on their URIs
		uriToValueMapping.forEach((uri, value) -> {
			String name = newUriToNameMapping.get(uri);
			// if name is null then the property value is not supported in the new model
			if (name != null) {
				targetInstance.add(name, value);
			}
		});
		// enable changes tracking so the removed relations are properly recorded
		//
		Trackable.enableTracking(targetInstance);
		newUriToNameMapping.forEach(removeNotApplicableProperties(targetInstance, newSemanticType));
	}

	private void runFiltersOnFields(Instance targetInstance, DefinitionModel newDefinition) {
		Map<String, DynamicCodeListFilter> dynamicClFilters = DynamicCodelistFiltersExtractor.getDynamicClFilters(
				newDefinition, targetInstance);
		newDefinition.fieldsStream()
				.filter(PropertyDefinition.hasCodelist())
				.filter(property -> targetInstance.isPropertyPresent(property.getName()))
				.forEach(property -> {
					Map<String, CodeValue> codeValues = getCodeValues(property, dynamicClFilters);
					List<Serializable> invalidValues = targetInstance.getAsCollection(property.getName(),
							LinkedList::new)
							.stream()
							.filter(String.class::isInstance)
							.map(String.class::cast)
							.filter(value -> !codeValues.containsKey(value)).collect(Collectors.toList());
					invalidValues.forEach(value -> targetInstance.remove(property.getName(), value));
				});
	}

	private Map<String, CodeValue> getCodeValues(PropertyDefinition propertyDefinition, Map<String, DynamicCodeListFilter> dynamicClFilters) {
		Integer codeList = propertyDefinition.getCodelist();

		// handles filters that are set directly in the definition
		Set<String> filters = propertyDefinition.getFilters();
		if (CollectionUtils.isNotEmpty(filters)) {
			return codelistService.getFilteredCodeValues(codeList, filters.toArray(new String[filters.size()]));
		}

		// Handles code list filters that are set through conditions.
		DynamicCodeListFilter filter = dynamicClFilters.get(propertyDefinition.getName());
		if (filter != null && filter.isFilterValid()) {
			// Filter a field depending on supplied custom filter
			if (filter.getReRenderFieldName().equals(filter.getSourceFilterFieldName())) {
				String[] values = filter.getFilterSource().replaceAll("\\s+", "").split(",");
				return codelistService.filterCodeValues(codeList, filter.isInclusive(), Arrays.asList(values));
			}
			Collection<String> values = filter.getValues();
			String[] valuesArray = values.toArray(new String[values.size()]);
			// Filter a field depending on another field value (restrictions are described in codelist "extra" columns)
			return codelistService.filterCodeValues(codeList, filter.isInclusive(), filter.getFilterSource(),
					valuesArray);
		}
		return codelistService.getFilteredCodeValues(codeList);
	}

	private BiConsumer<String, String> removeNotApplicableProperties(Instance copy, InstanceType newSemanticType) {
		return (uri, name) -> {
			if (!isPropertyApplicable(uri, newSemanticType)) {
				copy.remove(name);
			}
		};
	}

	private void validateDefinitionCompatibility(Instance instance, DefinitionModel newDefinition) {
		InstanceType currentType = instance.type();
		InstanceType newType = getSemanticType(newDefinition);

		if (instance.isUploaded()) {
			if (!newType.isUploadable()) {
				throw new IllegalArgumentException(String.format("The new target type %s(%s) is not Uploadable",
						newType.getProperty(DefaultProperties.TITLE), newType.getId()));
			}
		} else if (!newType.isCreatable()) {
			throw new IllegalArgumentException(String.format("The new target type %s(%s) is not Creatable",
					newType.getProperty(DefaultProperties.TITLE), newType.getId()));
		}
		Collection<InstanceType> superTypes = getAllowedSuperTypes(currentType);
		boolean isPartOfTheImmediateTypeTree = superTypes
				.stream()
				.anyMatch(parent -> parent.hasSubType(newType));
		if (!isPartOfTheImmediateTypeTree) {
			throw new IllegalArgumentException(String.format(
							"The new target type %s(%s} is not part of the accepted types with initial type %s(%s)",
							newType.getProperty(DefaultProperties.TITLE), newType.getId(),
							currentType.getProperty(DefaultProperties.TITLE), currentType.getId()));
		}
	}

	/**
	 * Provides the allowed types that can be used to determine the possible change type target.
	 *
	 * @param instanceType the current instance type to start from
	 * @return a collection with at least one super type that can be used to collect children applicable for type change
	 */
	public Collection<InstanceType> getAllowedSuperTypes(InstanceType instanceType) {
		Collection<InstanceType> superTypes = instanceType.getSuperTypes();
		Collection<InstanceType> result = new LinkedHashSet<>(superTypes);

		for (InstanceType superType : superTypes) {
			if (skippedClasses.get().contains(superType.getId().toString())) {
				ClassInstance skippedType = semanticDefinitionService.getClassInstance(superType.getId().toString());
				if (skippedType != null) {
					result.addAll(skippedType.getSuperClasses());
				}
			}
		}
		return result;
	}

	private DefinitionModel findDefinitionByName(String asType) {
		DefinitionModel newType = definitionService.find(asType);
		if (newType == null) {
			throw new ResourceNotFoundException(asType);
		}
		if (newType instanceof GenericDefinition && ((GenericDefinition) newType).isAbstract()) {
			throw new IllegalArgumentException("Cannot convert to abstract type " + asType);
		}
		return newType;
	}

	private Map<String, Serializable> getRawInstanceProperties(Instance instance) {
		return definitionService.getInstanceDefinition(instance)
				.fieldsStream()
				.filter(hasUri())
				.filter(property -> instance.isValueNotNull(property.getName()))
				.collect(Collectors.toMap(resolveUri(), property -> instance.get(property.getName())));
	}

	private InstanceType getSemanticType(DefinitionModel definitionModel) {
		return definitionModel.getField(DefaultProperties.SEMANTIC_TYPE)
					.map(PropertyDefinition::getDefaultValue)
					.map(semanticDefinitionService::getClassInstance)
					.orElseThrow(() -> new IllegalArgumentException("The given definition "
							+ definitionModel.getIdentifier() + " does not have a property "
							+ DefaultProperties.SEMANTIC_TYPE));
	}

	private boolean isPropertyApplicable(String uri, InstanceType newSemanticType) {
		PropertyInstance property = semanticDefinitionService.getRelation(uri);
		if (property == null) {
			property = semanticDefinitionService.getProperty(uri);
			if (property == null) {
				// not object or data property, probably not supported semantic property
				// leave it as is
				return true;
			}
		}
		ClassInstance domainClass = semanticDefinitionService.getClassInstance(property.getDomainClass());
		return isRelationTypeCompatible(newSemanticType, domainClass);
	}

	private static boolean isRelationTypeCompatible(InstanceType newType, ClassInstance domainOrRange) {
		if (domainOrRange == null) {
			// no restriction, we are good
			return true;
		}
		// the domainOrRange could be the same as the new type if the semantic class is not actually changed
		// to be compatible the domainOrRange should be super type of the new type
		return domainOrRange.equals(newType) || domainOrRange.hasSubType(newType);
	}

	private static Instance createInstance(Instance instance, String asType, InstanceType newSemanticType) {
		ObjectInstance copy = new ObjectInstance();
		copy.setId(instance.getId());
		copy.setRevision(instance.getRevision());
		copy.setContentManagementId(CMInstance.getContentManagementId(instance, null));
		copy.setIdentifier(asType);
		copy.setType(newSemanticType);
		return copy;
	}

	private Instance getInstance(String id) {
		try {
			return domainInstanceService.loadInstance(id);
		} catch (IllegalArgumentException e) {
			// this is thrown from the namespace registry service when instance is loaded
			// and the id is not in the correct format
			throw new InstanceNotFoundException(id);
		}
	}

	/**
	 * Count how many affected instances there are based on the given instance and new type
	 * @param instanceId the instance that have it's type changed
	 * @param asType the new type of the instance
	 * @return the number of affected non deleted instances
	 */
	@SuppressWarnings("unchecked")
	int countAffectedInstanceOfTypeChangeOf(Serializable instanceId, InstanceType asType) {
		SearchArguments searchArguments = createSearchCriteriaForReferringInstances(instanceId, asType.getId());
		try (Stream<Serializable> stream = searchService.stream(searchArguments, ResultItemTransformer.asSingleValue("instance"))) {
			return (int) stream.count();
		}
	}

	/**
	 * Load and modify the affected instance by the type change fo the given instance. The returned instance will have
	 * removed the current instance from their relations if the relation range does not allow the new instance type.
	 *
	 * @param instanceId the instance that have it's type changed
	 * @param asType the new type of the instance
	 * @return the collection of all non deleted instances updated to reflect the new type of the instance
	 */
	Collection<Instance> getAffectedInstanceOfTypeChangeOf(Serializable instanceId, InstanceType asType) {
		List<Instance> referringInstances = loadReferringInstances(instanceId, asType.getId());

		return referringInstances
				.stream()
				.filter(isReferredInstanceApplicable(instanceId, asType).negate())
				.collect(Collectors.toList());
	}

	private Predicate<? super Instance> isReferredInstanceApplicable(Serializable instanceId, InstanceType newSemanticType) {
		return referredInstance -> {
			List<String> affectedProperties = referredInstance.getProperties()
					.entrySet()
					.stream()
					.filter(entry -> isInstanceIdPresent(entry.getValue(), instanceId))
					.map(Map.Entry::getKey)
					.collect(Collectors.toList());
			if (affectedProperties.isEmpty()) {
				// the instance is not affected. This should not happen if the query returned the correct data
				return true;
			}
			DefinitionModel definition = definitionService.getInstanceDefinition(referredInstance);
			long modifiedProperties = affectedProperties.stream()
					.map(definition::getField)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.filter(relationsWithCompatibleRanges(newSemanticType).negate())
					.filter(property -> referredInstance.remove(property.getName(), instanceId))
					.count();
			return modifiedProperties == 0;
		};
	}

	private Predicate<PropertyDefinition> relationsWithCompatibleRanges(InstanceType newSemanticType) {
		return property -> {
			String uri = PropertyDefinition.resolveUri().apply(property);
			PropertyInstance relation = semanticDefinitionService.getRelation(uri);
			String rangeClass = relation.getRangeClass();
			ClassInstance rangeType = semanticDefinitionService.getClassInstance(rangeClass);
			return isRelationTypeCompatible(newSemanticType, rangeType);
		};
	}

	private boolean isInstanceIdPresent(Serializable value, Serializable instanceId) {
		if (value instanceof Collection) {
			return ((Collection) value).contains(instanceId);
		}
		return EqualsHelper.nullSafeEquals(value, instanceId);
	}

	@SuppressWarnings("unchecked")
	private List<Instance> loadReferringInstances(Serializable instanceId, Serializable newType) {
		SearchArguments searchArguments = createSearchCriteriaForReferringInstances(instanceId, newType);

		List<Serializable> instanceIds;
		try (Stream<Serializable> stream = searchService.stream(searchArguments, ResultItemTransformer.asSingleValue("instance"))) {
			// the query have distinct clause so no need to use Set here
			instanceIds = stream.collect(Collectors.toList());
		}
		return instanceService.loadByDbId(instanceIds);
	}

	@SuppressWarnings("unchecked")
	private SearchArguments createSearchCriteriaForReferringInstances(Serializable instanceId, Serializable newType) {
		SearchArguments searchArguments = new SearchArguments();
		searchArguments.setStringQuery(SEARCH_RELATED_INSTANCES);
		searchArguments.setPermissionsType(SearchArguments.QueryResultPermissionFilter.NONE);
		searchArguments.getQueryConfigurations().put(SPARQLQueryHelper.INCLUDE_INFERRED_CONFIGURATION, Boolean.TRUE);
		searchArguments.getArguments().put("target", instanceId);
		searchArguments.getArguments().put("newType", newType);
		return searchArguments;
	}
}
