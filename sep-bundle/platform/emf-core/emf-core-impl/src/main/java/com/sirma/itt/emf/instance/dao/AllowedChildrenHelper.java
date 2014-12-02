package com.sirma.itt.emf.instance.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.dao.AllowedChildrenProvider;
import com.sirma.itt.emf.definition.model.AllowedChildConfiguration;
import com.sirma.itt.emf.definition.model.AllowedChildConfigurationImpl;
import com.sirma.itt.emf.definition.model.AllowedChildDefinition;
import com.sirma.itt.emf.definition.model.AllowedChildrenModel;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Identity;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.security.PermissionsEnum;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Helper class for calculating the allowed children of specific type of a give node.
 *
 * @author BBonev
 */
public class AllowedChildrenHelper {

	/** The Constant ALL. */
	private static final String ALL = "all";
	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(AllowedChildrenHelper.class);

	/**
	 * Synch allowed children configuration. Fixes and merges configuration when loading the
	 * definitions. Optimizes allowed and denied definitions.Collects the all allow and deny
	 * properties into 1 for allow and 1 for deny for each definition.
	 *
	 * @param definition
	 *            the definition model to optimize
	 */
	public static void optimizeAllowedChildrenConfiguration(AllowedChildrenModel definition) {
		List<AllowedChildDefinition> list = definition.getAllowedChildren();
		if ((list == null) || list.isEmpty()) {
			return;
		}

		Map<String, Set<String>> allWorkflows = new LinkedHashMap<String, Set<String>>();
		for (AllowedChildDefinition workflowIdDefinition : list) {
			CollectionUtils.addValueToSetMap(allWorkflows, workflowIdDefinition.getType(),
					workflowIdDefinition.getIdentifier());
		}

		for (AllowedChildDefinition workflowIdDefinition : list) {
			if (EqualsHelper.nullSafeEquals(workflowIdDefinition.getIdentifier(), ALL, true)) {
				workflowIdDefinition.setIdentifier(ALL);
			}
			optimizePermission(allWorkflows, workflowIdDefinition);

			optimizeFilters(workflowIdDefinition);
		}
	}

	/**
	 * Optimize permission.
	 * 
	 * @param allWorkflows
	 *            the all workflows
	 * @param workflowIdDefinition
	 *            the workflow id definition
	 */
	private static void optimizePermission(Map<String, Set<String>> allWorkflows,
			AllowedChildDefinition workflowIdDefinition) {
		if (workflowIdDefinition.getPermissions() != null) {
			Set<String> allow = new LinkedHashSet<String>();
			Set<String> deny = new LinkedHashSet<String>();

			collectPermissions(workflowIdDefinition, allow, deny);
			// remove any current permissions so we can add the combined ones
			workflowIdDefinition.getPermissions().clear();
			Set<String> currentDefinition = allWorkflows.get(workflowIdDefinition.getType());

			optimizeAllow(workflowIdDefinition, allow, deny, currentDefinition);
			optimizeDeny(workflowIdDefinition, allow, deny, currentDefinition);

			// nothing is defined we allow everything
			if (workflowIdDefinition.getPermissions().isEmpty()) {
				AllowedChildConfigurationImpl configuration = new AllowedChildConfigurationImpl();
				configuration.setProperty(PermissionsEnum.ALLOW.toString());
				configuration.setValues(new LinkedHashSet<String>(currentDefinition));
				workflowIdDefinition.getPermissions().add(configuration);
			}

			((BidirectionalMapping) workflowIdDefinition).initBidirection();
		}
	}

	/**
	 * Optimize filters.
	 * 
	 * @param workflowIdDefinition
	 *            the workflow id definition
	 */
	private static void optimizeFilters(AllowedChildDefinition workflowIdDefinition) {
		if ((workflowIdDefinition.getFilters() != null)
				&& !workflowIdDefinition.getFilters().isEmpty()) {
			// convert all values to lower case
			for (AllowedChildConfiguration configuration : workflowIdDefinition.getFilters()) {
				Set<String> values = new LinkedHashSet<String>();
				for (String string : configuration.getValues()) {
					values.add(string.toLowerCase());
				}
				configuration.getValues().clear();
				configuration.getValues().addAll(values);
			}
		}
	}

	/**
	 * Collect permissions.
	 * 
	 * @param workflowIdDefinition
	 *            the workflow id definition
	 * @param allow
	 *            the allow
	 * @param deny
	 *            the deny
	 */
	private static void collectPermissions(AllowedChildDefinition workflowIdDefinition,
			Set<String> allow,
			Set<String> deny) {
		for (AllowedChildConfiguration configuration : workflowIdDefinition
				.getPermissions()) {
			String property = configuration.getProperty();
			PermissionsEnum permission = PermissionsEnum.valueOf(property.toUpperCase());
			if (permission == null) {
				continue;
			}
			switch (permission) {
				case ALLOW:
					allow.addAll(configuration.getValues());
					break;
				case DENY:
					deny.addAll(configuration.getValues());
					break;
				default:
					// nothing to do here for now
					break;
			}
		}
	}

	/**
	 * Optimize deny.
	 * 
	 * @param workflowIdDefinition
	 *            the workflow id definition
	 * @param allow
	 *            the allow
	 * @param deny
	 *            the deny
	 * @param currentDefinition
	 *            the current definition
	 */
	private static void optimizeDeny(AllowedChildDefinition workflowIdDefinition,
			Set<String> allow, Set<String> deny, Set<String> currentDefinition) {
		if (!deny.isEmpty()) {
			// check if we have deny=all
			boolean denyAll = CollectionUtils.containsIgnoreCase(deny, ALL);
			if (denyAll) {
				// convert deny=all to actual workflow IDs
				Set<String> temp = new LinkedHashSet<String>(currentDefinition);
				temp.removeAll(allow);
				deny.clear();
				deny.addAll(temp);
			}
			// remove any denied workflows that are not defined in the
			// current definition
			deny.retainAll(currentDefinition);

			AllowedChildConfigurationImpl configuration = new AllowedChildConfigurationImpl();
			configuration.setProperty(PermissionsEnum.DENY.toString());
			configuration.setValues(deny);
			workflowIdDefinition.getPermissions().add(configuration);
		}
	}

	/**
	 * Optimize allow.
	 * 
	 * @param workflowIdDefinition
	 *            the workflow id definition
	 * @param allow
	 *            the allow
	 * @param deny
	 *            the deny
	 * @param currentDefinition
	 *            the current definition
	 */
	private static void optimizeAllow(AllowedChildDefinition workflowIdDefinition,
			Set<String> allow,
			Set<String> deny, Set<String> currentDefinition) {
		if (!allow.isEmpty()) {
			boolean allowAll = CollectionUtils.containsIgnoreCase(deny, ALL);
			// convert allow=all to actual workflow IDs
			if (allowAll) {
				allow.clear();
				allow.addAll(currentDefinition);
			} else {
				// remove any allowed workflows that are not defined in the
				// current definition
				allow.retainAll(currentDefinition);
			}

			AllowedChildConfigurationImpl configuration = new AllowedChildConfigurationImpl();
			configuration.setProperty(PermissionsEnum.ALLOW.toString());
			configuration.setValues(allow);
			workflowIdDefinition.getPermissions().add(configuration);
		}
	}

	/**
	 * Removes the not supported children types from the given model.
	 *
	 * @param definition
	 *            the definition to check for invalid types
	 * @param typesToRemove
	 *            the types to remove
	 */
	public static void removeNotSupportedChildrenTypes(AllowedChildrenModel definition,
			Set<String> typesToRemove) {
		if (typesToRemove.isEmpty()) {
			// nothing to do
			return;
		}
		String identifier = definition.getClass().getSimpleName();
		if (definition instanceof Identity) {
			identifier += " (" + ((Identity) definition).getIdentifier() + ")";
		}
		for (Iterator<AllowedChildDefinition> it = definition.getAllowedChildren().iterator(); it
				.hasNext();) {
			AllowedChildDefinition childDefinition = it.next();
			if (typesToRemove.contains(childDefinition.getType())) {
				LOGGER.warn("Removed not supported child definition with id="
						+ childDefinition.getIdentifier() + " and type="
						+ childDefinition.getType() + " from " + identifier);
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
	 * @param dictionaryService
	 *            the dictionary service
	 * @return the allowed children
	 */
	public static <S extends Instance, A extends Instance> Map<String, List<DefinitionModel>> getAllowedChildren(
			S sourceInstance, AllowedChildrenProvider<S> calculator,
			DictionaryService dictionaryService) {
		DefinitionModel model = calculator.getDefinition(sourceInstance);
		if (model instanceof AllowedChildrenModel) {
			Set<String> types = getAllowedChildrenTypes((AllowedChildrenModel) model);
			Map<String, List<DefinitionModel>> result = CollectionUtils.createLinkedHashMap(types
					.size());
			for (String type : types) {
				List<DefinitionModel> list = getAllowedChildren(sourceInstance, model,
						calculator.getDefinition(type), type,
						calculator.calculateActive(sourceInstance, type),
						calculator.getActive(sourceInstance, type), dictionaryService, false);
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
	 * @param dictionaryService
	 *            the dictionary service
	 * @param childType
	 *            the child type
	 * @return the allowed children
	 */
	public static <S extends Instance, A extends Instance> List<DefinitionModel> getAllowedChildren(
			S sourceInstance, AllowedChildrenProvider<S> calculator,
			DictionaryService dictionaryService, String childType) {
		if ((sourceInstance == null) || (childType == null)) {
			return CollectionUtils.emptyList();
		}
		DefinitionModel model = calculator.getDefinition(sourceInstance);
		if (model instanceof AllowedChildrenModel) {
			List<DefinitionModel> list = getAllowedChildren(sourceInstance, model,
					calculator.getDefinition(childType), childType,
					calculator.calculateActive(sourceInstance, childType),
					calculator.getActive(sourceInstance, childType), dictionaryService, false);
			return list;
		}
		return CollectionUtils.emptyList();
	}

	/**
	 * Checks if a child of the given type is allowed. If the result is <code>true</code> then the
	 * methods {@link #getAllowedChildren(Instance, AllowedChildrenProvider, DictionaryService)} and
	 * {@link #getAllowedChildren(Instance, AllowedChildrenProvider, DictionaryService, String)}
	 * should not return a non empty list of definitions for the given particular type.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param <A>
	 *            the generic type
	 * @param sourceInstance
	 *            the source instance
	 * @param calculator
	 *            the calculator
	 * @param dictionaryService
	 *            the dictionary service
	 * @param childType
	 *            the child type
	 * @return true, if is child allowed
	 */
	public static <S extends Instance, A extends Instance> boolean isChildAllowed(S sourceInstance,
			AllowedChildrenProvider<S> calculator, DictionaryService dictionaryService,
			String childType) {
		if ((sourceInstance == null) || (childType == null)) {
			return false;
		}
		DefinitionModel model = calculator.getDefinition(sourceInstance);
		if (model instanceof AllowedChildrenModel) {
			List<DefinitionModel> list = getAllowedChildren(sourceInstance, model,
					calculator.getDefinition(childType), childType,
					calculator.calculateActive(sourceInstance, childType),
					calculator.getActive(sourceInstance, childType), dictionaryService, true);
			return !list.isEmpty();
		}
		return false;
	}

	/**
	 * Gets the allowed children for the given instance and requested type. The method will try to
	 * determine which of the defined elements of the requested type could be created based on the
	 * definition of the source instance and the active elements, that are of the same type of the
	 * requested.
	 *
	 * @param <S>
	 *            the source instance type
	 * @param <R>
	 *            the result definition type type
	 * @param <A>
	 *            the active child instance type
	 * @param sourceInstance
	 *            the the source instance to check for allowed children.
	 * @param resultClass
	 *            the result class
	 * @param allowedChildType
	 *            the allowed child type
	 * @param checkActive
	 *            if need to checking the active instance of the requested child type
	 * @param active
	 *            the list of active active children from the requested type
	 * @param dictionaryService
	 *            the dictionary service to use for fetching definitions
	 * @return the list of allowed children
	 */
	public static <S extends Instance, R extends DefinitionModel, A extends Instance> List<R> getAllowedChildren(
			S sourceInstance, Class<R> resultClass, String allowedChildType, boolean checkActive,
			List<A> active, DictionaryService dictionaryService) {
		DefinitionModel definitionModel = dictionaryService.getInstanceDefinition(sourceInstance);
		return getAllowedChildren(sourceInstance, definitionModel, resultClass, allowedChildType,
				checkActive, active, dictionaryService, false);
	}

	/**
	 * Gets the allowed children for the given instance and requested type. The method will try to
	 * determine which of the defined elements of the requested type could be created based on the
	 * definition of the source instance and the active elements, that are of the same type of the
	 * requested.
	 * 
	 * @param <S>
	 *            the source instance type
	 * @param <R>
	 *            the result definition type type
	 * @param <A>
	 *            the active child instance type
	 * @param sourceInstance
	 *            the the source instance to check for allowed children.
	 * @param sourceDefinition
	 *            the definition of the source instance
	 * @param resultClass
	 *            the result class
	 * @param allowedChildType
	 *            the allowed child type
	 * @param checkActive
	 *            if need to checking the active instance of the requested child type
	 * @param active
	 *            the list of active active children from the requested type
	 * @param dictionaryService
	 *            the dictionary service to use for fetching definitions
	 * @param firstOnly
	 *            the first only - if <code>true</code> then the method will return only the first
	 *            found child and will not try to fetch all of them
	 * @return the list of allowed children
	 */
	public static <S extends Instance, R extends DefinitionModel, A extends Instance> List<R> getAllowedChildren(
			S sourceInstance, DefinitionModel sourceDefinition, Class<R> resultClass,
			String allowedChildType, boolean checkActive, List<A> active,
			DictionaryService dictionaryService, boolean firstOnly) {

		if (sourceDefinition instanceof AllowedChildrenModel) {
			List<AllowedChildDefinition> definitionIds = ((AllowedChildrenModel) sourceDefinition)
					.getAllowedChildren();
			if ((definitionIds == null) || definitionIds.isEmpty()) {
				// if nothing is defined then we cannot do anything more
				return new LinkedList<R>();
			}
			Map<String, AllowedChildDefinition> definedChildren = CollectionUtils
					.createLinkedHashMap(definitionIds.size());
			// collect all defined child definitions
			for (AllowedChildDefinition childDefinition : definitionIds) {
				if (EqualsHelper.nullSafeEquals(childDefinition.getType(), allowedChildType, true)) {
					definedChildren.put(childDefinition.getIdentifier(), childDefinition);
				}
			}

			if (checkActive && (active != null)) {
				collectActive(sourceInstance, active, definedChildren);
			}

			Set<R> definitions = CollectionUtils.createLinkedHashSet(definedChildren.size());
			Set<R> highPriority = CollectionUtils.createLinkedHashSet(10);

			for (Entry<String, AllowedChildDefinition> entry : definedChildren.entrySet()) {
				if (EqualsHelper.nullSafeEquals(entry.getKey(), ALL, true)) {
					List<R> allDefinitions = dictionaryService.getAllDefinitions(resultClass);
					// TODO: fix implementation to support filtering -> CMF-2082
					// return the first result only
					if (firstOnly && !allDefinitions.isEmpty()) {
						return allDefinitions.subList(0, 1);
					}
					// does not iterate the list to copy the elements if not needed
					definitions.addAll(allDefinitions);
					continue;
				}
				R definition = dictionaryService.getDefinition(resultClass, entry.getKey());
				if (definition != null) {
					// we skip abstract definitions
					if ((definition instanceof TopLevelDefinition)
							&& ((TopLevelDefinition) definition).isAbstract()) {
						continue;
					}
					processDefinition(sourceInstance, definitions, highPriority, entry, definition);
					// return the first result only
					if (firstOnly && (!highPriority.isEmpty() || !definitions.isEmpty())) {
						ArrayList<R> l = new ArrayList<R>(1);
						if (highPriority.isEmpty()) {
							l.add(definitions.iterator().next());
						} else {
							l.add(highPriority.iterator().next());
						}
						return l;
					}
				} else {
					LOGGER.warn("No " + allowedChildType + " definition for ID=" + entry.getKey()
							+ "! Please check the definition XMLs");
				}
			}
			List<R> list = new ArrayList<R>(highPriority.size() + definitions.size());
			list.addAll(highPriority);
			list.addAll(definitions);
			return list;
		}
		return CollectionUtils.emptyList();
	}

	/**
	 * Process definition.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param <R>
	 *            the generic type
	 * @param sourceInstance
	 *            the source instance
	 * @param definitions
	 *            the definitions
	 * @param highPriority
	 *            the high priority
	 * @param entry
	 *            the entry
	 * @param definition
	 *            the definition
	 */
	private static <S extends Instance, R extends DefinitionModel> void processDefinition(
			S sourceInstance, Set<R> definitions, Set<R> highPriority,
			Entry<String, AllowedChildDefinition> entry, R definition) {
		List<AllowedChildConfiguration> list = entry.getValue().getFilters();
		if ((list != null) && !list.isEmpty()) {
			for (AllowedChildConfiguration childConfiguration : list) {
				// checks if the parent has the given property value
				// as one of the filter, if so we can have the given child
				if (checkFilter(sourceInstance, childConfiguration)) {
					if (entry.getValue().isDefault()) {
						highPriority.add(definition);
					} else {
						definitions.add(definition);
					}
				}
			}
		} else {
			// if no filtering is defined we can start the child right away
			if (entry.getValue().isDefault()) {
				highPriority.add(definition);
			} else {
				definitions.add(definition);
			}
		}
	}

	/**
	 * Collect active.
	 * 
	 * @param <S>
	 *            the generic type
	 * @param <A>
	 *            the generic type
	 * @param sourceInstance
	 *            the source instance
	 * @param active
	 *            the active
	 * @param definedChildren
	 *            the defined children
	 */
	private static <S extends Instance, A extends Instance> void collectActive(S sourceInstance,
			List<A> active, Map<String, AllowedChildDefinition> definedChildren) {
		// by default defined workflows are allowed
		Set<String> allowed = new LinkedHashSet<String>(definedChildren.keySet());
		Set<String> denied = new LinkedHashSet<String>();
		Set<String> processed = new LinkedHashSet<String>();

		// for each active child we collect the allowed and denied children types
		for (A child : active) {
			if (processed.contains(child.getIdentifier())) {
				continue;
			}
			processed.add(child.getIdentifier());

			AllowedChildDefinition childDefinition = definedChildren.get(child
					.getIdentifier());

			if (childDefinition == null) {
				// we probably have a definition that was created using the ALL
				// configuration so we check for it
				childDefinition = definedChildren.get(ALL);
				// if still not found then we cannot continue with this one
				if (childDefinition == null) {
					LOGGER.warn("The source " + sourceInstance.getClass().getSimpleName()
							+ " has a child with definition " + child.getIdentifier()
							+ " that is not allowed, based on the configuration: "
							+ definedChildren.keySet());
					continue;
				}
			}
			List<AllowedChildConfiguration> permissions = childDefinition.getPermissions();

			Set<String> lAllowed = new LinkedHashSet<String>();
			Set<String> lDenied = new LinkedHashSet<String>();
			for (AllowedChildConfiguration configuration : permissions) {
				PermissionsEnum permission = PermissionsEnum.valueOf(configuration
						.getProperty());
				if (permission == PermissionsEnum.ALLOW) {
					lAllowed.addAll(configuration.getValues());
				} else if (permission == PermissionsEnum.DENY) {
					lDenied.addAll(configuration.getValues());
				}
			}
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

		// from all children remove all not allowed children
		// it should be equal to calling removeAll(denied)
		definedChildren.keySet().retainAll(allowed);
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
		Set<String> types = new LinkedHashSet<String>();
		for (AllowedChildDefinition allowedChildDefinition : children) {
			types.add(allowedChildDefinition.getType());
		}
		return types;
	}

	/**
	 * Checks if the {@link PropertyModel} has the given property value as one of the filter.
	 *
	 * @param model
	 *            the model
	 * @param filter
	 *            the filter
	 * @return true, if found
	 */
	private static boolean checkFilter(PropertyModel model, AllowedChildConfiguration filter) {
		Serializable serializable = model.getProperties().get(filter.getProperty());
		// checks if the case has the given property value
		// as one of the filter, if so we can start the
		// given workflow definition
		if ((filter.getValues() != null) && !filter.getValues().isEmpty()
				&& (serializable instanceof String)
				&& filter.getValues().contains(serializable.toString().toLowerCase())) {
			return true;
		}
		return false;
	}
}
