package com.sirma.itt.seip.definition.validator;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.TransitionGroupDefinition;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.validation.ValidationMessage;

/**
 * Validates transition and transition groups for consistency.
 *
 * @author Adrian Mitev
 */
public class TransitionGroupValidator implements DefinitionValidator {

	@Override
	public List<ValidationMessage> validate(GenericDefinition definition) {
		TransitionValidatorMessageBuilder messageBuilder = new TransitionValidatorMessageBuilder(definition);

		validateGroupProperties(definition, messageBuilder);

		if (ensureParentsExist(definition, messageBuilder)) {
			ensureNoCyclesInTheGroupHierarchy(definition, messageBuilder);
		}

		return messageBuilder.getMessages();
	}

	private static void validateGroupProperties(GenericDefinition definition, TransitionValidatorMessageBuilder messageBuilder) {
		definition.getTransitionGroups().forEach(transitionGroup -> {
			if (isBlank(transitionGroup.getLabelId())) {
				messageBuilder.missingTransitionGroupLabel(transitionGroup.getIdentifier());
			}

			if (isBlank(transitionGroup.getType())) {
				messageBuilder.missingTransitionGroupType(transitionGroup.getIdentifier());
			}
		});
	}

	private static boolean ensureParentsExist(GenericDefinition model, TransitionValidatorMessageBuilder messageBuilder) {
		int initialMessageCount = messageBuilder.getMessages().size();

		Set<String> existingGroups = model.getTransitionGroups()
				.stream()
				.map(TransitionGroupDefinition::getIdentifier)
				.collect(Collectors.toSet());

		model.getTransitions().stream()
				.filter(transition -> isNotBlank(transition.getGroup()))
				.filter(transition -> !existingGroups.contains(transition.getGroup()))
				.forEach(transition -> messageBuilder.missingTransitionGroup(transition.getIdentifier(), transition.getGroup()));

		model.getTransitionGroups().stream()
				.filter(group -> isNotBlank(group.getParent()))
				.filter(group -> !existingGroups.contains(group.getParent()))
				.forEach(group -> messageBuilder.missingTransitionGroupParent(group.getIdentifier(), group.getParent()));

		return messageBuilder.getMessages().size() == initialMessageCount;
	}

	private void ensureNoCyclesInTheGroupHierarchy(GenericDefinition definition, TransitionValidatorMessageBuilder messageBuilder) {

		Map<String, TransitionGroupDefinition> groupsMapping = definition.getTransitionGroups().stream()
				.collect(Collectors.toMap(TransitionGroupDefinition::getIdentifier, Function.identity()));

		Map<String, Set<String>> parents = new HashMap<>();

		for (TransitionGroupDefinition group : definition.getTransitionGroups()) {
			parents.putIfAbsent(group.getIdentifier(), getGroupParents(group.getIdentifier(), parents, groupsMapping, messageBuilder));
		}
	}

	private Set<String> getGroupParents(String group, Map<String, Set<String>> parents,
			Map<String, TransitionGroupDefinition> groups, TransitionValidatorMessageBuilder messageBuilder) {

		if (parents.containsKey(group)) {
			return parents.get(group);
		}

		TransitionGroupDefinition currentGroup = groups.get(group);

		if (StringUtils.isBlank(currentGroup.getParent())) {
			return CollectionUtils.emptySet();
		}

		Set<String> parentIds = new HashSet<>();

		parents.put(currentGroup.getIdentifier(), parentIds);

		Set<String> parentsOfParent = getGroupParents(currentGroup.getParent(), parents, groups, messageBuilder);

		if (parentsOfParent.contains(currentGroup.getIdentifier())) {
			messageBuilder.transitionGroupCycle(currentGroup.getIdentifier());
		} else {
			parentIds.add(currentGroup.getParent());
			parentIds.addAll(parentsOfParent);
		}

		return parentIds;
	}

	public class TransitionValidatorMessageBuilder extends DefinitionValidationMessageBuilder {

		public static final String MISSING_TRANSITION_GROUP_LABEL = "definition.validation.transition.group.missing.label";
		public static final String MISSING_TRANSITION_GROUP_TYPE = "definition.validation.transition.group.missing.type";
		public static final String MISSING_TRANSITION_GROUP = "definition.validation.transition.group.missing";
		public static final String MISSING_TRANSITION_GROUP_PARENT = "definition.validation.transition.group.missing.parent";
		public static final String TRANSITION_GROUP_CYCLE = "definition.validation.transition.group.cycle";

		public TransitionValidatorMessageBuilder(GenericDefinition genericDefinition) {
			super(genericDefinition);
		}

		private void missingTransitionGroupLabel(String group) {
			error(getId(), MISSING_TRANSITION_GROUP_LABEL, getId(), group);
		}

		private void missingTransitionGroupType(String group) {
			error(getId(), MISSING_TRANSITION_GROUP_TYPE, getId(), group);
		}

		private void missingTransitionGroup(String transition, String group) {
			error(getId(), MISSING_TRANSITION_GROUP, getId(), transition, group);
		}

		private void missingTransitionGroupParent(String groupId, String parent) {
			error(getId(), MISSING_TRANSITION_GROUP_PARENT, getId(), groupId, parent);
		}

		private void transitionGroupCycle(String group) {
			error(getId(), TRANSITION_GROUP_CYCLE, getId(), group);
		}
	}

}
