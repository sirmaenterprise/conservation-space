package com.sirma.itt.seip.instance.revision.steps;

import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.itt.seip.expressions.conditions.ConditionType;
import com.sirma.itt.seip.expressions.conditions.ConditionsManager;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Publish step that makes sure to copy the proper relations from the original instance to the revision. Such properties
 * are the static and dynamic mandatory properties.
 *
 * @author BBonev
 */
@Extension(target = PublishStep.EXTENSION_NAME, order = 111)
public class CopyRelationsPublishStep implements PublishStep {

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private ConditionsManager conditionsEvaluatorManager;

	@Override
	public void execute(PublishContext publishContext) {
		Instance instanceToPublish = publishContext.getRequest().getInstanceToPublish();
		DefinitionModel model = dictionaryService.getInstanceDefinition(instanceToPublish);

		Stream<PropertyDefinition> staticallyRequired = model
				.fieldsStream()
					.filter(PropertyDefinition.isObjectProperty())
					.filter(PropertyDefinition::isMandatory);

		Stream<PropertyDefinition> dynamicRequired = model
				.fieldsStream()
					.filter(PropertyDefinition.isObjectProperty())
					.filter(filterDynamicRequired(instanceToPublish));

		Stream
				.concat(staticallyRequired, dynamicRequired)
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
