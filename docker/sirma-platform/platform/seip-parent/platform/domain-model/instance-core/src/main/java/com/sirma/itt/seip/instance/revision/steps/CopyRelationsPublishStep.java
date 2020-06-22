package com.sirma.itt.seip.instance.revision.steps;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.expressions.conditions.ConditionType;
import com.sirma.itt.seip.expressions.conditions.ConditionsManager;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Publish step that makes sure to copy the proper relations from the original instance to the revision. Such properties
 * are the static, dynamic and transitional mandatory properties.
 *
 * @author BBonev
 */
@Extension(target = PublishStep.EXTENSION_NAME, order = 111)
public class CopyRelationsPublishStep implements PublishStep {

	@Inject
	private DefinitionService definitionService;

	@Inject
	private ConditionsManager conditionsEvaluatorManager;

	@Inject
	private StateTransitionManager stateTransitionManager;

	@Inject
	private StateService stateService;

	@Override
	public void execute(PublishContext publishContext) {
		Instance instanceToPublish = publishContext.getRequest().getInstanceToPublish();
		Instance revision = publishContext.getRevision();

		DefinitionModel model = definitionService.getInstanceDefinition(instanceToPublish);

		Stream<PropertyDefinition> staticallyRequired = model
				.fieldsStream()
					.filter(PropertyDefinition.isObjectProperty())
					.filter(PropertyDefinition::isMandatory);

		Stream<PropertyDefinition> dynamicRequired = model
				.fieldsStream()
					.filter(PropertyDefinition.isObjectProperty())
					.filter(filterDynamicRequired(instanceToPublish));

		Set<String> requiredFields = stateTransitionManager.getRequiredFields(revision,
				stateService.getPrimaryState(revision),
				publishContext.getRequest().getTriggerOperation().getOperation());

		Stream<PropertyDefinition> transitionRequired = requiredFields
				.stream()
					.filter(field -> model.getField(field).isPresent())
					.map(field -> model.getField(field).get())
					.filter(PropertyDefinition.isObjectProperty());

		Stream
				.of(staticallyRequired, dynamicRequired, transitionRequired)
					.flatMap(field -> field)
					.distinct()
					.map(PropertyDefinition::getName)
					.forEach(field -> PropertiesUtil.copyValue(instanceToPublish, publishContext.getRevision(), field));
	}

	private Predicate<? super PropertyDefinition> filterDynamicRequired(Instance instanceToPublish) {
		ConditionsManager evaluator = conditionsEvaluatorManager;
		return field -> evaluator.evalPropertyConditions(field, ConditionType.MANDATORY, instanceToPublish)
				|| evaluator.evalPropertyConditions(field, ConditionType.REQUIRED, instanceToPublish);
	}

	@Override
	public String getName() {
		return Steps.COPY_RELATIONS.getName();
	}
}
