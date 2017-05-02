package com.sirma.itt.seip.rule.operations;

import com.sirma.itt.emf.rule.BaseDynamicInstanceRule;
import com.sirma.itt.emf.rule.RuleOperation;
import com.sirma.itt.seip.context.Context;

/**
 * Abstract rule operation implementation to accommodate some basic common logic for implementing {@link RuleOperation}
 * s.
 *
 * @author BBonev
 */
public abstract class BaseRuleOperation extends BaseDynamicInstanceRule implements RuleOperation {

	@Override
	public void processingStarted(Context<String, Object> processingContext, Context<String, Object> context) {
		// nothing to do
	}

	@Override
	public void processingEnded(Context<String, Object> processingContext, Context<String, Object> context) {
		// nothing to do
	}

}
