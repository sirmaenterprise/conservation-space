package com.sirma.itt.seip.expressions;

import java.io.Serializable;

/**
 * Defines an interface for expression evaluation.
 *
 * @author BBonev
 */
public interface ExpressionEvaluator extends Serializable {

	/**
	 * Gets the expression id. It should be identifier for the expression or <code>null</code> if non is applicable. The
	 * identifier will be used to identify the expression more easily.
	 *
	 * @return the expression id
	 */
	String getExpressionId();

	/**
	 * Checks if is caching supported. The result of expression could be cached in the current expression context for
	 * faster parsing but some expression could not have their values cached. This evaluator should return
	 * <code>false</code>.
	 *
	 * @return <code>true</code>, if caching is supported and <code>false</code> if the returned value should not be
	 *         cached.
	 */
	boolean isCachingSupported();

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
