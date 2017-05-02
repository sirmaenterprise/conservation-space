package com.sirma.itt.seip.expressions.conditions;

import com.sirma.itt.seip.domain.definition.Conditional;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Conditions evaluator manager.
 * 
 * @author Hristo Lungov
 */
public interface ConditionsManager {

	/**
	 * Verify conditions based on renderAsType.
	 * 
	 * @see ConditionType
	 * @param model
	 *            the model definition
	 * @param conditionRenderAsType
	 *            is type of condition like: HIDDEN, READONLY,MANDATORY and etc.
	 * @param instance
	 *            the instance
	 * @return true, if there are defined conditions and condition's expression evalution is successful
	 */
	boolean evalPropertyConditions(Conditional model, ConditionType conditionRenderAsType,
			Instance instance);

}
