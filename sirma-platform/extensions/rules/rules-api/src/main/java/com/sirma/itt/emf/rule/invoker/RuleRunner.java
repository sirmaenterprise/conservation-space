package com.sirma.itt.emf.rule.invoker;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sirma.itt.emf.rule.InstanceRule;
import com.sirma.itt.emf.rule.RuleContext;

/**
 * Handles rule execution. Provides means of accessing the status of the currently running rules.
 *
 * @author BBonev
 */
public interface RuleRunner {

	/**
	 * Schedule to run the given rules using the given context asynchronously. The implementation should decide how or
	 * when to run the rules. The method should return as soon as possible.
	 *
	 * @param rulesToRun
	 *            the rules to run
	 * @param context
	 *            the context to use to run the rules with
	 */
	void scheduleRules(List<InstanceRule> rulesToRun, RuleContext context);

	/**
	 * Run the given rules using the given context now.
	 *
	 * @param rulesToRun
	 *            the rules to run
	 * @param context
	 *            the context to use to run the rules with
	 */
	void runRules(List<InstanceRule> rulesToRun, RuleContext context);

	/**
	 * Gets the all active or pending rules for execution. They are mapped by instance id.
	 * <p>
	 * Note that this is only a snapshot of the running state and changes over time.
	 *
	 * @return the all active rules
	 */
	Map<Serializable, Collection<RuleExecutionStatusAccessor>> getAllActiveRules();

	/**
	 * Gets the active rule sets for the given instance. if no rules are running then the method should return an empty
	 * collection.
	 * <p>
	 * Note that this is only a snapshot of the running state and changes over time.
	 *
	 * @param instanceId
	 *            the instance id to check for active rules
	 * @return the active rules for the instance or empty collection if none
	 */
	Collection<RuleExecutionStatusAccessor> getActiveRules(Serializable instanceId);

	/**
	 * Tries to cancel the running rules if any for the given instance
	 *
	 * @param instanceId
	 *            the instance id
	 */
	void cancelRunningRulesForInstance(Serializable instanceId);
}
