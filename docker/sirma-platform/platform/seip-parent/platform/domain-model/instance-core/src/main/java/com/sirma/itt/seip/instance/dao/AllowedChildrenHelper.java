package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.AllowedChildConfiguration;
import com.sirma.itt.seip.definition.AllowedChildDefinition;
import com.sirma.itt.seip.definition.AllowedChildrenModel;
import com.sirma.itt.seip.definition.AllowedChildrenProvider;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.TopLevelDefinition;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.FilterMode;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Helper class for calculating the allowed children of specific type of a give node.
 *
 * @author BBonev
 */
public class AllowedChildrenHelper {

	public static final String ALL = "all";

	private static final Logger LOGGER = LoggerFactory.getLogger(AllowedChildrenHelper.class);

	private AllowedChildrenHelper() {
		// utility class
	}

	/**
	 * Removes the not supported children types from the given model.
	 *
	 * @param definition
	 *            the definition to check for invalid types
	 * @param typesToRemove
	 *            the types to remove
	 */
	public static void removeNotSupportedChildrenTypes(AllowedChildrenModel definition, Set<String> typesToRemove) {
		if (typesToRemove.isEmpty()) {
			// nothing to do
			return;
		}
		String identifier = definition.getClass().getSimpleName();
		if (definition instanceof Identity) {
			identifier += " (" + ((Identity) definition).getIdentifier() + ")";
		}
		for (Iterator<AllowedChildDefinition> it = definition.getAllowedChildren().iterator(); it.hasNext();) {
			AllowedChildDefinition childDefinition = it.next();
			if (typesToRemove.contains(childDefinition.getType())) {
				LOGGER.warn("Removed not supported child definition with id={} and type={} from {}",
						childDefinition.getIdentifier(), childDefinition.getType(), identifier);
				it.remove();
			}
		}
	}

	/**
	 * Gets the allowed children.
	 *
	 * @param <S>
	 *            the generic type
	 * @param <A>
	 *            the generic type
	 * @param sourceInstance
	 *            the source instance
	 * @param calculator
	 *            the calculator
	 * @param definitionService
	 *            the dictionary service
	 * @return the allowed children
	 */
	public static <S extends Instance, A extends Instance> Map<String, List<DefinitionModel>> getAllowedChildren(
			S sourceInstance, AllowedChildrenProvider<S> calculator, DefinitionService definitionService) {
		DefinitionModel model = calculator.getDefinition(sourceInstance);
		if (model instanceof AllowedChildrenModel) {
			Set<String> types = getAllowedChildrenTypes((AllowedChildrenModel) model);
			Map<String, List<DefinitionModel>> result = CollectionUtils.createLinkedHashMap(types.size());
			for (String type : types) {
				List<DefinitionModel> list = getAllowedChildren(sourceInstance, model, type, calculator,
						definitionService, false);
				result.put(type, list);
			}
			return result;
		}
		return CollectionUtils.emptyMap();
	}

	/**
	 * Gets the allowed children for a specific type using the provided calculator.
	 *
	 * @param <S>
	 *            the generic type
	 * @param <A>
	 *            the generic type
	 * @param sourceInstance
	 *            the source instance
	 * @param calculator
	 *            the calculator
	 * @param definitionService
	 *            the dictionary service
	 * @param childType
	 *            the child type
	 * @return the allowed children
	 */
	public static <S extends Instance, A extends Instance> List<DefinitionModel> getAllowedChildren(S sourceInstance,
			AllowedChildrenProvider<S> calculator, DefinitionService definitionService, String childType) {
		if (sourceInstance == null || childType == null) {
			return CollectionUtils.emptyList();
		}
		DefinitionModel model = calculator.getDefinition(sourceInstance);
		if (model instanceof AllowedChildrenModel) {
			return getAllowedChildren(sourceInstance, model, childType, calculator, definitionService, false);
		}
		return CollectionUtils.emptyList();
	}

	/**
	 * Checks if a child of the given type is allowed. If the result is <code>true</code> then the methods
	 * {@link #getAllowedChildren(Instance, AllowedChildrenProvider, DefinitionService)} and
	 * {@link #getAllowedChildren(Instance, AllowedChildrenProvider, DefinitionService, String)} should not return a non
	 * empty list of definitions for the given particular type.
	 *
	 * @param <S>
	 *            the generic type
	 * @param <A>
	 *            the generic type
	 * @param sourceInstance
	 *            the source instance
	 * @param calculator
	 *            the calculator
	 * @param definitionService
	 *            the dictionary service
	 * @param childType
	 *            the child type
	 * @return true, if is child allowed
	 */
	public static <S extends Instance, A extends Instance> boolean isChildAllowed(S sourceInstance,
			AllowedChildrenProvider<S> calculator, DefinitionService definitionService, String childType) {
		return isDefinitionChildAllowed(sourceInstance, calculator, definitionService, childType, null);
	}

	/**
	 * Checks if a child of the given type and definition id is allowed. If the result is <code>true</code> then the
	 * methods {@link #getAllowedChildren(Instance, AllowedChildrenProvider, DefinitionService)} and
	 * {@link #getAllowedChildren(Instance, AllowedChildrenProvider, DefinitionService, String)} should not return a non
	 * empty list of definitions for the given particular type.
	 *
	 * @param <S>
	 *            the generic type
	 * @param <A>
	 *            the generic type
	 * @param sourceInstance
	 *            the source instance
	 * @param calculator
	 *            the calculator
	 * @param definitionService
	 *            the dictionary service
	 * @param childType
	 *            the child type
	 * @param definitionId
	 *            the definition id
	 * @return true, if is child allowed
	 */
	public static <S extends Instance, A extends Instance> boolean isDefinitionChildAllowed(S sourceInstance,
			AllowedChildrenProvider<S> calculator, DefinitionService definitionService, String childType,
			String definitionId) {
		if (sourceInstance == null || childType == null) {
			return false;
		}
		DefinitionModel model = calculator.getDefinition(sourceInstance);
		if (model instanceof AllowedChildrenModel) {
			List<DefinitionModel> list = getAllowedChildren(sourceInstance, model, childType, calculator,
					definitionService, false);
			if (list.isEmpty()) {
				return false;
			} else if (definitionId == null) {
				// if we do not care for the definition type
				return true;
			}
			for (DefinitionModel definitionModel : list) {
				if (EqualsHelper.nullSafeEquals(definitionId, definitionModel.getIdentifier(), true)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets the allowed children for the given instance and requested type. The method will try to determine which of
	 * the defined elements of the requested type could be created based on the definition of the source instance and
	 * the active elements, that are of the same type of the requested.
	 *
	 * @param <S>
	 *            the source instance type
	 * @param <A>
	 *            the active child instance type
	 * @param sourceInstance
	 *            the the source instance to check for allowed children.
	 * @param sourceDefinition
	 *            the definition of the source instance
	 * @param allowedChildType
	 *            the allowed child type
	 * @param calculator
	 *            the calculator
	 * @param definitionService
	 *            the dictionary service to use for fetching definitions
	 * @param firstOnly
	 *            the first only - if <code>true</code> then the method will return only the first found child and will
	 *            not try to fetch all of them
	 * @return the list of allowed children
	 */
	public static <S extends Instance, A extends Instance> List<DefinitionModel> getAllowedChildren(S sourceInstance,
			DefinitionModel sourceDefinition, String allowedChildType, AllowedChildrenProvider<S> calculator,
			DefinitionService definitionService, boolean firstOnly) {

		if (sourceDefinition instanceof AllowedChildrenModel) {
			List<AllowedChildDefinition> definitionIds = ((AllowedChildrenModel) sourceDefinition).getAllowedChildren();
			if (definitionIds == null || definitionIds.isEmpty()) {
				// if nothing is defined then we cannot do anything more
				return CollectionUtils.emptyList();
			}
			PermissionConfig permissionConfig = new PermissionConfig();
			permissionConfig.allowedChildType = allowedChildType;
			permissionConfig.firstOnly = firstOnly;
			permissionConfig.definedChildren = CollectionUtils.createLinkedHashMap(definitionIds.size());

			// collect all defined child definitions
			for (AllowedChildDefinition childDefinition : definitionIds) {
				if (EqualsHelper.nullSafeEquals(childDefinition.getType(), permissionConfig.allowedChildType, true)) {
					permissionConfig.definedChildren.put(childDefinition.getIdentifier(), childDefinition);
					readPermissions(childDefinition, permissionConfig, calculator, sourceInstance);
				}
			}

			// First we check if we need to calculate active due to collection itself could be slow
			// operation.
			if (calculator.calculateActive(sourceInstance, permissionConfig.allowedChildType)) {
				permissionConfig.filterOutDenied = true;
				List<A> active = calculator.getActive(sourceInstance, permissionConfig.allowedChildType);
				if (active != null && !active.isEmpty()) {
					collectActive(sourceInstance, active, permissionConfig);
				}
			}

			return doFinalAllowedChildrenFiltering(sourceInstance, calculator, definitionService, permissionConfig);
		}
		return CollectionUtils.emptyList();
	}

	private static <S extends Instance> List<DefinitionModel> doFinalAllowedChildrenFiltering(S sourceInstance,
			AllowedChildrenProvider<S> calculator, DefinitionService definitionService,
			PermissionConfig permissionConfig) {
		Set<DefinitionModel> definitions = CollectionUtils.createLinkedHashSet(permissionConfig.definedChildren.size());
		Set<DefinitionModel> highPriority = CollectionUtils.createLinkedHashSet(10);

		for (Entry<String, AllowedChildDefinition> entry : permissionConfig.definedChildren.entrySet()) {
			if (EqualsHelper.nullSafeEquals(entry.getKey(), ALL, true)) {
				List<DefinitionModel> allDefinitions = permissionConfig.getAllDefinitions(calculator, sourceInstance,
						permissionConfig.allowedChildType, permissionConfig.filterOutDenied);
				// TODO: fix implementation to support filtering -> CMF-2082
				// return the first result only
				if (permissionConfig.firstOnly && !allDefinitions.isEmpty()) {
					return allDefinitions.subList(0, 1);
				}
				// does not iterate the list to copy the elements if not needed
				definitions.addAll(allDefinitions);
				continue;
			}
			DefinitionModel definition = definitionService.find(entry.getKey());
			if (definition != null) {
				List<DefinitionModel> priority = CollectionUtils.emptyList();
				// we skip abstract definitions
				if (!(definition instanceof TopLevelDefinition && ((TopLevelDefinition) definition).isAbstract())) {
					processDefinition(sourceInstance, definitions, highPriority, entry, definition);
					priority = getFirstDefinition(permissionConfig, definitions, highPriority);
				}
				if (!priority.isEmpty()) {
					return priority;
				}
			} else {
				LOGGER.warn("No {} definition for ID={}! Please check the definition XMLs",
						permissionConfig.allowedChildType, entry.getKey());
			}
		}
		List<DefinitionModel> list = new ArrayList<>(highPriority.size() + definitions.size());
		list.addAll(highPriority);
		list.addAll(definitions);
		return list;
	}

	/**
	 * Gets the first high priority.
	 *
	 * @param permissionConfig
	 *            the permission config
	 * @param definitions
	 *            the definitions
	 * @param highPriority
	 *            the high priority
	 * @return the first high priority
	 */
	private static List<DefinitionModel> getFirstDefinition(PermissionConfig permissionConfig,
			Set<DefinitionModel> definitions, Set<DefinitionModel> highPriority) {
		// return the first result only
		if (permissionConfig.firstOnly && (!highPriority.isEmpty() || !definitions.isEmpty())) {
			List<DefinitionModel> l = new ArrayList<>(1);
			if (highPriority.isEmpty()) {
				l.add(definitions.iterator().next());
			} else {
				l.add(highPriority.iterator().next());
			}
			return l;
		}
		return Collections.emptyList();
	}

	private static <S extends Instance> void readPermissions(AllowedChildDefinition childDefinition,
			PermissionConfig permissionConfig, AllowedChildrenProvider<S> calculator, S instance) {

		List<AllowedChildConfiguration> permissions = childDefinition.getPermissions();
		Set<String> lAllowed = new LinkedHashSet<>();
		Set<String> lDenied = new LinkedHashSet<>();
		if (ALL.equalsIgnoreCase(childDefinition.getIdentifier())) {
			lAllowed.addAll(permissionConfig.getAll(calculator, instance, permissionConfig.allowedChildType));
		} else {
			// the current element is allowed
			lAllowed.add(childDefinition.getIdentifier());
		}
		collectPermissions(permissions, lAllowed, lDenied);
		permissionConfig.mergePermissions(lAllowed, lDenied);
	}

	private static <S extends Instance, R extends DefinitionModel> void processDefinition(S sourceInstance,
			Set<R> definitions, Set<R> highPriority, Entry<String, AllowedChildDefinition> entry, R definition) {
		List<AllowedChildConfiguration> filters = entry.getValue().getFilters();
		if (filters != null && !filters.isEmpty()) {
			processDefinition(sourceInstance, definitions, highPriority, entry, definition, filters);
			// if no filtering is defined we can start the child right away
		} else if (entry.getValue().isDefault()) {
			highPriority.add(definition);
		} else {
			definitions.add(definition);
		}
	}

	private static <S extends Instance, R extends DefinitionModel> void processDefinition(S sourceInstance,
			Set<R> definitions, Set<R> highPriority, Entry<String, AllowedChildDefinition> entry, R definition,
			List<AllowedChildConfiguration> filters) {
		// only if all filters match then the allowed child will be added.
		for (AllowedChildConfiguration childConfiguration : filters) {
			// checks if the parent has the given property value
			// as one of the filter, if so we can have the given child
			if (!checkFilter(sourceInstance, childConfiguration)) {
				return;
			}
		}
		if (entry.getValue().isDefault()) {
			highPriority.add(definition);
		} else {
			definitions.add(definition);
		}
	}

	private static <S extends Instance, A extends Instance> void collectActive(S sourceInstance, List<A> active,
			PermissionConfig permissionConfig) {
		// // by default defined workflows are allowed
		Set<String> processed = new LinkedHashSet<>();
		Map<String, AllowedChildDefinition> definedChildren = permissionConfig.definedChildren;
		// for each active child we collect the allowed and denied children types
		for (A child : active) {
			if (processed.contains(child.getIdentifier())) {
				continue;
			}
			processed.add(child.getIdentifier());

			AllowedChildDefinition childDefinition = definedChildren.get(child.getIdentifier());

			if (childDefinition == null) {
				// we probably have a definition that was created using the ALL
				// configuration so we check for it
				childDefinition = definedChildren.get(ALL);
				// if still not found then we cannot continue with this one
				if (childDefinition == null) {
					LOGGER.warn(
							"The source {} has a child with definition {} that is not allowed, based on the configuration: {}",
							sourceInstance.getClass().getSimpleName(), child.getIdentifier(), definedChildren.keySet());
					continue;
				}
			}
			List<AllowedChildConfiguration> permissions = childDefinition.getPermissions();

			Set<String> lAllowed = new LinkedHashSet<>();
			Set<String> lDenied = new LinkedHashSet<>();
			collectPermissions(permissions, lAllowed, lDenied);
			permissionConfig.mergePermissions(lAllowed, lDenied);
		}
	}

	private static void collectPermissions(List<AllowedChildConfiguration> permissions, Set<String> allowed,
			Set<String> denied) {
		for (AllowedChildConfiguration configuration : permissions) {
			switch (configuration.getProperty()) {
				case "ALLOW":
					allowed.addAll(configuration.getValues());
					break;
				case "DENY":
					denied.addAll(configuration.getValues());
					break;
				default:
					throw new IllegalStateException("Invalid permission type " + configuration.getProperty());
			}
		}
	}

	/**
	 * Gets the allowed children types.
	 *
	 * @param model
	 *            the model
	 * @return the allowed children types
	 */
	public static Set<String> getAllowedChildrenTypes(AllowedChildrenModel model) {
		List<AllowedChildDefinition> children = model.getAllowedChildren();
		Set<String> types = new LinkedHashSet<>();
		for (AllowedChildDefinition allowedChildDefinition : children) {
			types.add(allowedChildDefinition.getType());
		}
		return types;
	}

	/**
	 * Checks if the {@link PropertyModel} has the given property value as one of the filter.
	 *
	 * @return true, if found
	 */
	private static boolean checkFilter(PropertyModel model, AllowedChildConfiguration filter) {
		Serializable serializable = model.getProperties().get(filter.getProperty());

		FilterMode mode = filter.getFilterMode();
		if (mode == null) {
			mode = FilterMode.IN;
		}
		switch (mode) {
			case CONTAINS:
				return filterWithContains(filter, serializable);
			case EQUALS:
				return EqualsHelper.nullSafeEquals(tryToConvertToString(serializable), filter.getXmlValues());
			case HAS_VALUE:
				return serializable != null;
			case IN:
				return filterWithIn(filter, serializable);
			case IS_EMPTY:
				return serializable == null || StringUtils.isBlank(serializable.toString());
			default:
				return filterWithIn(filter, serializable);
		}
	}

	private static boolean filterWithIn(AllowedChildConfiguration filter, Serializable serializable) {
		String value = null;
		return filter.getValues() != null && !filter.getValues().isEmpty()
				&& (value = tryToConvertToString(serializable)) != null
				&& filter.getValues().contains(value.toLowerCase());
	}

	private static boolean filterWithContains(AllowedChildConfiguration filter, Serializable serializable) {
		if (serializable instanceof Collection) {
			return ((Collection<?>) serializable).contains(filter.getXmlValues());
		} else if (serializable != null) {
			return tryToConvertToString(serializable).toLowerCase().contains(filter.getXmlValues().toLowerCase());
		}
		return false;
	}

	private static String tryToConvertToString(Serializable serializable) {
		return TypeConverterUtil.getConverter().tryConvert(String.class, serializable);
	}

	/**
	 * Holder class for arguments when processing allowed children.
	 *
	 * @author BBonev
	 */
	private static class PermissionConfig {
		Set<String> allowed = new LinkedHashSet<>();
		Set<String> denied = new LinkedHashSet<>();
		Set<String> all = new LinkedHashSet<>();
		List<DefinitionModel> allDefinitions = null;
		Map<String, AllowedChildDefinition> definedChildren;
		String allowedChildType;
		boolean filterOutDenied = false;
		boolean firstOnly;

		/**
		 * Gets the all definition identifiers if not retrieved will be calculated using the given provider and stored.
		 *
		 * @param <S>
		 *            the generic type
		 * @param calculator
		 *            the calculator
		 * @param instance
		 *            the instance
		 * @param type
		 *            the type
		 * @return the all definition identifiers
		 */
		<S extends Instance> Set<String> getAll(AllowedChildrenProvider<S> calculator, S instance, String type) {
			if (allDefinitions == null) {
				setAllDefinitions(calculator.getAllDefinitions(instance, type));
			}
			return all;
		}

		/**
		 * Gets the all definitions if not retrieved will be calculated using the given provider and stored.
		 *
		 * @param <S>
		 *            the generic type
		 * @param calculator
		 *            the calculator
		 * @param instance
		 *            the instance
		 * @param type
		 *            the type
		 * @param excludeDenied
		 *            the exclude denied
		 * @return the all definitions
		 */
		<S extends Instance> List<DefinitionModel> getAllDefinitions(AllowedChildrenProvider<S> calculator, S instance,
				String type, boolean excludeDenied) {
			if (allDefinitions == null) {
				setAllDefinitions(calculator.getAllDefinitions(instance, type));
			}
			if (excludeDenied) {
				List<DefinitionModel> filtred = new ArrayList<>(allDefinitions.size());
				for (DefinitionModel definitionModel : allDefinitions) {
					if (!denied.contains(definitionModel.getIdentifier())) {
						filtred.add(definitionModel);
					}
				}
				return filtred;
			}
			return allDefinitions;
		}

		/**
		 * Sets the all definitions.
		 *
		 * @param definitions
		 *            the new all definitions
		 */
		void setAllDefinitions(List<DefinitionModel> definitions) {
			allDefinitions = definitions;
			for (DefinitionModel definitionModel : definitions) {
				all.add(definitionModel.getIdentifier());
			}
		}

		/**
		 * Merge the given allowed and denied definitions with the internal allowed and denied
		 *
		 * @param lAllowed
		 *            the source allowed
		 * @param lDenied
		 *            the source denied
		 */
		void mergePermissions(Set<String> lAllowed, Set<String> lDenied) {
			// from locally allowed removed any denied from other active children
			lAllowed.removeAll(denied);
			// then remove any locally denied - if correctly defined should
			// not be needed but who knows
			lAllowed.removeAll(lDenied);

			// remove any locally denied from the global list
			allowed.removeAll(lDenied);
			// add to the globally allowed the locally allowed
			allowed.addAll(lAllowed);
			// update deny list
			denied.addAll(lDenied);
		}
	}
}
