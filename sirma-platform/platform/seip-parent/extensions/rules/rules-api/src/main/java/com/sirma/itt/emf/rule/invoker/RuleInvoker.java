package com.sirma.itt.emf.rule.invoker;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.sirma.itt.seip.domain.instance.Instance;

/**
 * The Interface RuleInvoker is used to describe direct invocation of rules. Note: All rules are processed on
 * persisting, but if you like to process rules at different time will help you
 *
 * @author Hristo Lungov
 * @author BBonev
 */
public interface RuleInvoker {

	/**
	 * Invoke rules in new transaction. This can be used when called at transaction and new transaction is needed in
	 * order to run the rules. The new transaction is valid only for synchronous rules.
	 *
	 * @param currentInstance
	 *            the current instance
	 * @param oldVersionInstance
	 *            the old version instance
	 * @param operationName
	 *            the instance type
	 */
	void invokeRules(Instance currentInstance, Instance oldVersionInstance, String operationName);

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
