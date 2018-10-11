package com.sirma.itt.seip.rule;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.rule.InstanceRule;
import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.emf.rule.invoker.RuleExecutionStatusAccessor;
import com.sirma.itt.emf.rule.invoker.RuleInvoker;
import com.sirma.itt.emf.rule.invoker.RuleRunner;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * {@link RuleInvoker} implementation that uses a {@link RuleStore} to access rule instances and default
 * {@link RuleRunner} to execute rules on instances requested by {@link #invokeRules(Instance, Instance, String)}
 * method. Rules that require synchronous execution will be run in new transaction each. The asynchronous rules will be
 * run using the default installed {@link RuleInvoker}. If no other {@link RuleInvoker} is present
 * {@link ThreadPoolRuleRunner} will be used.
 *
 * @author Hristo Lungov
 * @author BBonev
 * @see ThreadPoolRuleRunner
 */
@ApplicationScoped
public class RuleInvokerImpl implements RuleInvoker {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private volatile Statistics statistics;

	@Inject
	private RuleStore ruleStore;

	@Inject
	private RuleRunner ruleRunner;

	@Override
	public Map<Serializable, Collection<RuleExecutionStatusAccessor>> getAllActiveRules() {
		return ruleRunner.getAllActiveRules();
	}

	@Override
	public Collection<RuleExecutionStatusAccessor> getActiveRules(Serializable instanceId) {
		return ruleRunner.getActiveRules(instanceId);
	}

	@Override
	public void cancelRunningRulesForInstance(Serializable instanceId) {
		ruleRunner.cancelRunningRulesForInstance(instanceId);
	}

	/**
	 * Invoke rules in new transaction. This can be used when called at transaction and new transaction is needed in
	 * order to run the rules. The new transaction is valid only for synchronous rules.
	 *
	 * @param currentInstance
	 *            the current instance
	 * @param oldVersionInstance
	 *            the old version instance
	 * @param operationName
	 *            the operation name
	 */
	@Override
	@RunAsSystem(protectCurrentTenant = true)
	public void invokeRules(Instance currentInstance, Instance oldVersionInstance, String operationName) {
		if (Options.DISABLE_RULES.isEnabled()) {
			return;
		}
		statistics.updateMeter(null, "RuleActivationRequests");

		TimeTracker allRulesTimeTracker = TimeTracker.createAndStart();
		try {
			collectAndRunRulesForInstance(currentInstance, oldVersionInstance, operationName);
		} finally {
			LOGGER.trace("All Rules Processed in {} ms", allRulesTimeTracker.stop());
		}
	}

	private void collectAndRunRulesForInstance(Instance currentInstance, Instance oldVersionInstance,
			String operation) {

		RuleContext context = RuleContext.create(currentInstance, oldVersionInstance, operation);

		Collection<InstanceRule> rulesToProcess = ruleStore.findRules(operation, context);

		List<InstanceRule> syncRules = new ArrayList<>(rulesToProcess.size());
		List<InstanceRule> asyncRules = new ArrayList<>(rulesToProcess.size());
		for (InstanceRule instanceRule : rulesToProcess) {
			if (instanceRule.isAsyncSupported()) {
				asyncRules.add(instanceRule);
			} else {
				syncRules.add(instanceRule);
			}
		}
		// use separate context for async execution to minimize collisions with running in
		// background threads and the sync operations if any
		executeRulesAsynchronously(asyncRules, RuleContext.copy(context));

		executeRulesSynchronously(syncRules, context);
	}

	private void executeRulesSynchronously(List<InstanceRule> syncRules, RuleContext context) {
		if (CollectionUtils.isEmpty(syncRules)) {
			// no rules nothing to do
			return;
		}
		ruleRunner.runRules(syncRules, context);
	}

	private void executeRulesAsynchronously(final List<InstanceRule> syncRules, final RuleContext context) {
		if (CollectionUtils.isEmpty(syncRules)) {
			// no rules nothing to do
			return;
		}
		ruleRunner.scheduleRules(syncRules, context);
	}

}
