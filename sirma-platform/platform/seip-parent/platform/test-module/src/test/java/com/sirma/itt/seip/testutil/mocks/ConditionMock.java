/*
 *
 */
package com.sirma.itt.seip.testutil.mocks;

import com.sirma.itt.seip.domain.definition.Condition;

/**
 * @author BBonev
 */
public class ConditionMock implements Condition {

	private String renderAs;
	private String expression;
	private String identifier;

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public String getRenderAs() {
		return renderAs;
	}

	/**
	 * Sets the render as.
	 *
	 * @param renderAs
	 *            the new render as
	 */
	public void setRenderAs(String renderAs) {
		this.renderAs = renderAs;
	}

	@Override
	public String getExpression() {
		return expression;
	}

	/**
	 * Sets the expression.
	 *
	 * @param expression
	 *            the new expression
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

}
