package com.sirma.itt.seip.definition.validator;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.TransitionGroupDefinition;
import com.sirma.itt.seip.definition.ValidationLoggingUtil;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;

/**
 * Validates transition and transition groups for consistency.
 *
 * @author Adrian Mitev
 */
public class TransitionGroupValidator implements DefinitionValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransitionGroupValidator.class);

	@Override
	public List<String> validate(RegionDefinitionModel model) {
		return this.validate((DefinitionModel) model);
	}

	@Override
	public List<String> validate(DefinitionModel model) {
		List<String> errors = new ArrayList<>();

		if (model instanceof GenericDefinition) {
			GenericDefinition genericDefinition = (GenericDefinition) model;

			List<TransitionGroupDefinition> transitionGroups = genericDefinition.getTransitionGroups();
			validateGroupProperties(transitionGroups, errors);

			if (ensureParentsExist(genericDefinition, errors)) {
				ensureNoCyclesInTheGroupHierarhy(transitionGroups, errors);
			}

			printErrors(genericDefinition, errors);
		}


		return errors;
	}

	private static void validateGroupProperties(List<TransitionGroupDefinition> groups, List<String> errors) {
		for (TransitionGroupDefinition group : groups) {
			if (isBlank(group.getLabelId())) {
				errors.add("Group " + group.getIdentifier() + " has no label set");
			}

			if (isBlank(group.getType())) {
				errors.add("Group " + group.getIdentifier() + " has no type set");
			}
		}
	}

	private static boolean ensureParentsExist(GenericDefinition model, List<String> errors) {
		List<String> missingParentErrors = new ArrayList<>();

		Set<String> groups = new HashSet<>();

		model.getTransitionGroups().forEach(group -> groups.add(group.getIdentifier()));

		model
				.getTransitions()
					.stream()
					.filter(transition -> isNotBlank(transition.getGroup()))
					.filter(transition -> !groups.contains(transition.getGroup()))
					.map(transition -> "Transition '" + transition.getIdentifier() + "' has a non-existing group '"
							+ transition.getGroup() + "'")
					.forEach(missingParentErrors::add);

		model
				.getTransitionGroups()
					.stream()
					.filter(group -> isNotBlank(group.getParent()))
					.filter(group -> !groups.contains(group.getParent()))
					.map(group -> "Group '" + group.getIdentifier() + "' has a non-existing parent group '"
							+ group.getParent() + "'")
					.forEach(missingParentErrors::add);

		errors.addAll(missingParentErrors);

		return missingParentErrors.isEmpty();
	}

	private Map<String, Set<String>> ensureNoCyclesInTheGroupHierarhy(List<TransitionGroupDefinition> groups,
			List<String> errors) {
		Map<String, Set<String>> parents = new HashMap<>();

		for (TransitionGroupDefinition group : groups) {
			parents.putIfAbsent(group.getIdentifier(), getGroupParents(group.getIdentifier(), parents, groups, errors));
		}

		return parents;
	}

	private Set<String> getGroupParents(String group, Map<String, Set<String>> parents,
			List<TransitionGroupDefinition> groups, List<String> errors) {

		if (parents.containsKey(group)) {
			return parents.get(group);
		}

		TransitionGroupDefinition currentGroup = groups
				.stream()
					.filter(current -> current.getIdentifier().equals(group))
					.findFirst()
					.get();

		if (StringUtils.isBlank(currentGroup.getParent())) {
			return CollectionUtils.emptySet();
		}

		Set<String> parentIds = new HashSet<>();

		parents.put(currentGroup.getIdentifier(), parentIds);

		Set<String> parentsOfParent = getGroupParents(currentGroup.getParent(), parents, groups, errors);
		if (parentsOfParent.contains(currentGroup.getIdentifier())) {
			errors.add("Cycle detected in the hierarchy of group '" + currentGroup.getIdentifier() + "'");
		} else {
			parentIds.add(currentGroup.getParent());
			parentIds.addAll(parentsOfParent);
		}

		return parentIds;
	}

	private static void printErrors(GenericDefinition model, List<String> errors) {
		if (!errors.isEmpty()) {
			StringBuilder builder = new StringBuilder(errors.size() * 60);
			builder
					.append("\n=====================================================================\nFound errors in definition: ")
						.append(model.getIdentifier())
						.append("\n");
			for (String error : errors) {
				builder.append(error).append("\n");
			}
			builder.append("=====================================================================");

			String errorMessage = builder.toString();
			LOGGER.error(errorMessage);
			ValidationLoggingUtil.addErrorMessage(errorMessage);
		}
	}

}
