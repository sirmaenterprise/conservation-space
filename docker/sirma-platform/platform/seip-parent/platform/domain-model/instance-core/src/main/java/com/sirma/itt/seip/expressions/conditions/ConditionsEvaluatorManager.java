package com.sirma.itt.seip.expressions.conditions;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.inject.Singleton;

import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.Conditional;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Eval all conditions based on PropertyDefinition.
 *
 * @author Hristo Lungov
 */
@Singleton
public class ConditionsEvaluatorManager implements ConditionsManager {

	@Override
	public boolean evalPropertyConditions(Conditional model, ConditionType conditionType, Instance instance) {
		if (Objects.isNull(conditionType)) {
			return false;
		}

		return Optional.ofNullable(model.getConditions())
				.orElseGet(Collections::emptyList)
				.stream()
				.filter(Objects::nonNull)
				.filter(Condition.byType(conditionType.getRenderAs()))
				.anyMatch(condition -> ConditionsEvaluator.evaluate(condition.getExpression()).test(instance));
	}

	@Override
	public Stream<PropertyDefinition> getVerifiedFieldsByType(DefinitionModel model, ConditionType conditionType,
			Instance instance) {
		return model.fieldsStream().filter(field -> evalPropertyConditions(field, conditionType, instance));
	}

}