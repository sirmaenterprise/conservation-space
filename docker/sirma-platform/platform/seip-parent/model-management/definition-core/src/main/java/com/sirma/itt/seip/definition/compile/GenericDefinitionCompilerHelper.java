package com.sirma.itt.seip.definition.compile;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.AllowedChildConfiguration;
import com.sirma.itt.seip.definition.AllowedChildDefinition;
import com.sirma.itt.seip.definition.AllowedChildrenModel;
import com.sirma.itt.seip.definition.model.AllowedChildConfigurationImpl;
import com.sirma.itt.seip.domain.BidirectionalMapping;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Utility containing logic that is used for optimization of allowed children configuration in the generic definitions.
 *
 * @author A. Kunchev
 */
// TODO tests ^_^
class GenericDefinitionCompilerHelper {

	private static final String ALL = "all";

	/**
	 * Synch allowed children configuration. Fixes and merges configuration when loading the definitions. Optimizes
	 * allowed and denied definitions.Collects the all allow and deny properties into 1 for allow and 1 for deny for
	 * each definition.
	 *
	 * @param definitionModel
	 *            the definition model to optimize
	 */
	public static void optimizeAllowedChildrenConfiguration(AllowedChildrenModel definitionModel) {
		List<AllowedChildDefinition> list = definitionModel.getAllowedChildren();
		if (CollectionUtils.isEmpty(list)) {
			return;
		}

		Map<String, Set<String>> allDefinitions = new LinkedHashMap<>();
		for (AllowedChildDefinition definition : list) {
			CollectionUtils.addValueToSetMap(allDefinitions, definition.getType(), definition.getIdentifier());
		}

		for (AllowedChildDefinition childDefinition : list) {
			// done to unify the format of the ALL constant as it could be written as All, ALL, all
			if (EqualsHelper.nullSafeEquals(childDefinition.getIdentifier(), ALL, true)) {
				childDefinition.setIdentifier(ALL);
			}

			optimizePermission(allDefinitions, childDefinition);
			optimizeFilters(childDefinition);
		}
	}

	private static void optimizePermission(Map<String, Set<String>> allDefinitions,
			AllowedChildDefinition childDefinition) {
		if (childDefinition.getPermissions() == null) {
			return;
		}

		Set<String> allow = new LinkedHashSet<>();
		Set<String> deny = new LinkedHashSet<>();

		collectPermissions(childDefinition, allow, deny);
		// remove any current permissions so we can add the combined ones
		childDefinition.getPermissions().clear();
		Set<String> currentDefinition = allDefinitions.get(childDefinition.getType());

		optimizeAllow(childDefinition, allow, deny, currentDefinition);
		optimizeDeny(childDefinition, allow, deny, currentDefinition);

		// nothing is defined we allow everything
		if (childDefinition.getPermissions().isEmpty()) {
			AllowedChildConfigurationImpl configuration = new AllowedChildConfigurationImpl();
			configuration.setProperty(PermissionsEnum.ALLOW.toString());
			configuration.setValues(new LinkedHashSet<>(currentDefinition));
			childDefinition.getPermissions().add(configuration);
		}

		((BidirectionalMapping) childDefinition).initBidirection();
	}

	private static void collectPermissions(AllowedChildDefinition childDefinition, Set<String> allow,
			Set<String> deny) {
		for (AllowedChildConfiguration configuration : childDefinition.getPermissions()) {
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

	private static void optimizeAllow(AllowedChildDefinition childDefinition, Set<String> allow, Set<String> deny,
			Set<String> currentDefinition) {
		if (allow.isEmpty()) {
			return;
		}

		// convert allow=all to actual workflow IDs
		if (CollectionUtils.containsIgnoreCase(deny, ALL)) {
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
		childDefinition.getPermissions().add(configuration);
	}

	private static void optimizeDeny(AllowedChildDefinition definition, Set<String> allow, Set<String> deny,
			Set<String> currentDefinition) {
		if (deny.isEmpty()) {
			return;
		}

		// check if we have deny=all
		if (CollectionUtils.containsIgnoreCase(deny, ALL)) {
			// convert deny=all to actual workflow IDs
			Set<String> temp = new LinkedHashSet<>(currentDefinition);
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
		definition.getPermissions().add(configuration);
	}

	private static void optimizeFilters(AllowedChildDefinition workflowIdDefinition) {
		if (workflowIdDefinition.getFilters() == null || workflowIdDefinition.getFilters().isEmpty()) {
			return;
		}

		// convert all values to lower case
		for (AllowedChildConfiguration configuration : workflowIdDefinition.getFilters()) {
			Set<String> values = CollectionUtils.createLinkedHashSet(configuration.getValues().size());
			for (String string : configuration.getValues()) {
				values.add(string.toLowerCase());
			}

			configuration.getValues().clear();
			configuration.getValues().addAll(values);
		}
	}

	private GenericDefinitionCompilerHelper() {
		// utility
	}

	private enum PermissionsEnum {
		ALLOW, DENY
	}
}
