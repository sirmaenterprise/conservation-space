package com.sirma.itt.emf.evaluation.el;

import java.util.LinkedList;
import java.util.List;

import com.sirma.itt.emf.evaluation.ExpressionEvaluator;

/**
 * Parsed EL expression object. The objects holds a single expression and any sub expressions.
 * 
 * @author BBonev
 */
public class ElExpression {

	/** The expression. */
	private String expression;
	/** The sub expressions. */
	private List<ElExpression> subExpressions;

	/** Cached evaluator instance that can handle the current expression. */
	private transient ExpressionEvaluator evaluator;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		if (getSubExpressions().isEmpty()) {
			return getExpression();
		}
		if (getExpression() == null) {
			return getSubExpressions().toString();
		}
		return getExpression() + " -> " + getSubExpressions();
	}

	/**
	 * Getter method for expression.
	 *
	 * @return the expression
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * Setter method for expression.
	 *
	 * @param expression the expression to set
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

	/**
	 * Getter method for subExpressions.
	 *
	 * @return the subExpressions
	 */
	public List<ElExpression> getSubExpressions() {
		if (subExpressions == null) {
			subExpressions = new LinkedList<ElExpression>();
		}
		return subExpressions;
	}

	/**
	 * Setter method for subExpressions.
	 *
	 * @param subExpressions the subExpressions to set
	 */
	public void setSubExpressions(List<ElExpression> subExpressions) {
		this.subExpressions = subExpressions;
	}

	/**
	 * Gets the cached evaluator instance that can handle the current expression.
	 * 
	 * @return the cached evaluator instance that can handle the current expression
	 */
	public ExpressionEvaluator getEvaluator() {
		return evaluator;
	}

	/**
	 * Sets the cached evaluator instance that can handle the current expression.
	 * 
	 * @param evaluator
	 *            the new cached evaluator instance that can handle the current expression
	 */
	public void setEvaluator(ExpressionEvaluator evaluator) {
		this.evaluator = evaluator;
	}
}
