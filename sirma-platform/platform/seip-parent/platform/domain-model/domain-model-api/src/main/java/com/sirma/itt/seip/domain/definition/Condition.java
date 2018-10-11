package com.sirma.itt.seip.domain.definition;

import java.util.function.Predicate;

import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Defines a single condition definitions.
 *
 * @author BBonev
 */
public interface Condition extends Identity {

	/**
	 * If the expression does not evaluate to <code>true</code> how to render the field.
	 *
	 * @return the render as
	 */
	String getRenderAs();

	/**
	 * Gets the expression that need to be evaluated to <code>true</code> or <code>false</code>.
	 *
	 * @return the expression to evaluate
	 */
	String getExpression();
	
	/**
	 * Build predicate function which check case-insensitive equality of condition and <code>conditionType</code>.
	 *
	 * @param conditionType
	 *            the condition type to be check.
	 * @return the predicate
	 */
	static Predicate<Condition> byType(String conditionType) {
		return condition -> EqualsHelper.nullSafeEquals(conditionType, condition.getRenderAs(), true);
	}
}
