package com.sirma.itt.seip.expressions;

/**
 * Base evalueator used for variables.
 * @author BBonev
 * @author yasko yasko
 */
public abstract class VariableEvaluator extends BaseEvaluator {
	private static final long serialVersionUID = 6997865622981396742L;
	protected static final String VAR_PREFIX = "var_";

	@Override
	public String getExpressionId() {
		return "var";
	}

	@Override
	public boolean isCachingSupported() {
		return false;
	}
}
