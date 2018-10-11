package com.sirma.itt.emf.rule.invoker;

import java.util.Collection;

import com.sirma.itt.emf.rule.RuleState;

/**
 * Provides access to the status of the currently running rules on a single instance.
 *
 * @author BBonev
 */
public interface RuleExecutionStatusAccessor {

	/**
	 * Gets the current state for the currently processing instance.
	 *
	 * @return the current state or <code>null</code> if no such is present
	 */
	RuleState getCurrentState();

	/**
	 * Returns the operation id that triggered the rules execution.
	 *
	 * @return the operation. May be <code>null</code> if triggered by null operation.
	 */
	String getTriggerOperation();

	/**
	 * Returns the id of the currently processed rule or <code>null</code> if no rule is currently processing due to not
	 * started or completed.
	 *
	 * @return the rule id or <code>null</code>.
	 */
	String getCurrentlyProcessedRule();

	/**
	 * Gets the active rules.
	 *
	 * @return the active rules
	 */
	Collection<String> getPendingRules();

	/**
	 * Gets the failed rules.
	 *
	 * @return the failed rules
	 */
	Collection<String> getFailedRules();

	/**
	 * Gets the completed rules.
	 *
	 * @return the completed rules
	 */
	Collection<String> getCompletedRules();

	/**
	 * The time current rule has been executing in milliseconds.
	 *
	 * @return the time in milliseconds
	 */
	long currentRuleExecutionTime();

	/**
	 * The time this rules set has been executing in milliseconds
	 *
	 * @return the time in milliseconds
	 */
	long executionTime();

	/**
	 * Checks execution has been completed
	 *
	 * @return true, if is done
	 */
	boolean isDone();

}