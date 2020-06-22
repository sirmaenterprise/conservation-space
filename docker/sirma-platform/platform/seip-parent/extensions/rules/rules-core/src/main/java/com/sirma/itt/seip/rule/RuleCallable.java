package com.sirma.itt.seip.rule;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.rule.InstanceRule;
import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Callable objects for new transaction execution.
 *
 * @author BBonev
 */
class RuleCallable implements Callable<Boolean> {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private final InstanceRule ruleToCall;
	private final RuleContext context;
	private final TimeTracker ruleTimeTracker;

	/**
	 * Instantiates a new rule callable.
	 *
	 * @param ruleToCall
	 *            the rule to call
	 * @param context
	 *            the context
	 * @param ruleTimeTracker
	 *            the rule time tracker
	 * @param activeCounter
	 *            the active counter
	 */
	public RuleCallable(InstanceRule ruleToCall, RuleContext context, TimeTracker ruleTimeTracker) {
		this.ruleToCall = ruleToCall;
		this.context = context;
		this.ruleTimeTracker = ruleTimeTracker;
	}

	@Override
	public Boolean call() throws Exception {
		return executeRule(ruleToCall, context, ruleTimeTracker);
	}

	/**
	 * Execute the provided rule using the given context and update the statistics objects
	 *
	 * @param rule
	 *            the rule
	 * @param context
	 *            the context
	 * @param ruleTimeTracker
	 *            the rule time tracker
	 * @param activeRulesCounter
	 *            the active rules counter
	 * @return true, if successful
	 */
	private static Boolean executeRule(InstanceRule rule, RuleContext context, TimeTracker ruleTimeTracker) {
		ruleTimeTracker.begin();
		try {
			rule.execute(context);
			return Boolean.TRUE;
		} catch (Exception e) {
			LOGGER.error("Error while processing Rule: {} with: {}", rule.getRuleInstanceName(), e.getMessage(), e);
			return Boolean.FALSE;
		} finally {
			LOGGER.trace("Processed Rule: {} in {} ms", rule.getRuleInstanceName(), ruleTimeTracker.stop());
		}
	}
}