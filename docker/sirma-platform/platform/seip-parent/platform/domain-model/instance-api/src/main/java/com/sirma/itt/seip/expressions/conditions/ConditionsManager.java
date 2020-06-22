package com.sirma.itt.seip.expressions.conditions;

import com.sirma.itt.seip.domain.definition.Conditional;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;

import java.util.stream.Stream;

/**
 * Conditions evaluator manager.
 *
 * @author Hristo Lungov
 */
public interface ConditionsManager {

	/**
	 * Verify conditions based on renderAsType.
	 *
	 * @param model
	 * 		the model definition
	 * @param conditionRenderAsType
	 * 		is type of condition like: HIDDEN, READONLY,MANDATORY and etc.
	 * @param instance
	 * 		the instance
	 * @return true, if there are defined conditions and condition's expression evalution is successful
	 * @see ConditionType
	 */
	boolean evalPropertyConditions(Conditional model, ConditionType conditionRenderAsType, Instance instance);

	/**
	 * Evaluates all conditions in a instance definitions by their {@link ConditionType}.
	 *
	 * @param model
	 * 		the definition model
	 * @param conditionType
	 * 		the {@link ConditionType}
	 * @param instance
	 * 		the instance
	 * @return stream of property definitions which can then be processed.
	 */
	Stream<PropertyDefinition> getVerifiedFieldsByType(DefinitionModel model, ConditionType conditionType,
			Instance instance);
}
