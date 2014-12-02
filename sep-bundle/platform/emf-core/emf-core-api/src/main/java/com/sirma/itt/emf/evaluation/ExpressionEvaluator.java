package com.sirma.itt.emf.evaluation;

import java.io.Serializable;

/**
 * Defines an interface for expression evaluation.
 *
 * @author BBonev
 */
public interface ExpressionEvaluator extends Serializable {

	/**
	 * Checks if the current evaluator can handle the current expression
	 *
	 * @param expression
	 *            the expression
	 * @return true, if can handle
	 */
	boolean canHandle(String expression);

	/**
	 * Evaluate the expression and return the result.
	 *
	 * @param expression
	 *            the expression
	 * @param values
	 *            the value, optional value to convert
	 * @return the evaluated value or <code>null</code>
	 */
	Serializable evaluate(String expression, Serializable... values);

	/**
	 * Evaluate the expression and return the result.
	 * 
	 * @param expression
	 *            the expression
	 * @param context
	 *            the context
	 * @param values
	 *            the value, optional value to convert
	 * @return the evaluated value or <code>null</code>
	 */
	Serializable evaluate(String expression, ExpressionContext context, Serializable... values);
}
