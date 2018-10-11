package com.sirma.itt.seip.expressions;

import java.util.LinkedList;
import java.util.List;

/**
 * Parsed EL expression object. The objects holds a single expression and any sub expressions.
 *
 * @author BBonev
 */
public class ElExpression {

	private String expressionId;

	private String expression;

	private List<ElExpression> subExpressions;

	/** Cached evaluator instance that can handle the current expression. */
	private transient ExpressionEvaluator evaluator;

	@Override
	public String toString() {
		if (getSubExpressions().isEmpty()) {
			return "[" + expressionId + "]" + getExpression();
		}
		if (getExpression() == null) {
			return getSubExpressions().toString();
		}
		return "[" + expressionId + "]" + getExpression() + " -> " + getSubExpressions();
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public List<ElExpression> getSubExpressions() {
		if (subExpressions == null) {
			subExpressions = new LinkedList<>();
		}
		return subExpressions;
	}

	public void setSubExpressions(List<ElExpression> subExpressions) {
		this.subExpressions = subExpressions;
	}

	public ExpressionEvaluator getEvaluator() {
		return evaluator;
	}

	public void setEvaluator(ExpressionEvaluator evaluator) {
		this.evaluator = evaluator;
	}

	public String getExpressionId() {
		return expressionId;
	}

	public void setExpressionId(String expressionId) {
		this.expressionId = expressionId;
	}
}
