package com.sirma.itt.seip.rule;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.rule.InstanceRule;
import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.emf.rule.RuleState;
import com.sirma.itt.emf.rule.invoker.RuleExecutionStatusAccessor;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.util.SecureCallable;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Wrapper object that is scheduled for asynchronous rule execution on an instance and particular operation. The
 * instance provides means to check the status of the executed rules.
 *
 * @author BBonev
 */
public class RulesExecutionContext extends SecureCallable<Void>implements RuleExecutionStatusAccessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final Deque<InstanceRule> rulesToRun;
	private final RuleContext context;
	private final TimeTracker ruleTimeTracker;
	private final Deque<InstanceRule> failedRules = new ConcurrentLinkedDeque<>();
	private final Deque<InstanceRule> ranRules = new ConcurrentLinkedDeque<>();

	private final TransactionSupport transactionSupport;

	private final Collection<RuleExecutionStatusAccessor> unregisterFrom;

	private volatile String processedRule;

	/**
	 * Instantiates a new rules execution context.
	 *
	 * @param rulesToRun
	 *            the rules to run
	 * @param context
	 *            the context
	 * @param statistics
	 *            the statistics
	 * @param activeCounter
	 *            the active counter
	 * @param unregisterFrom
	 *            the unregister from
	 * @param transactionSupport
	 *            the transaction support
	 * @param securityContextManager
	 *            the security context manager
	 */
	@SuppressWarnings("unchecked")
	protected RulesExecutionContext(List<InstanceRule> rulesToRun, RuleContext context, Statistics statistics,
			Collection<RuleExecutionStatusAccessor> unregisterFrom,
			TransactionSupport transactionSupport, SecurityContextManager securityContextManager) {
		super(securityContextManager);
		this.transactionSupport = transactionSupport;
		if (rulesToRun instanceof Deque) {
			this.rulesToRun = (Deque<InstanceRule>) rulesToRun;
		} else {
			this.rulesToRun = new LinkedList<>(rulesToRun);
		}
		this.context = context;
		this.unregisterFrom = unregisterFrom;
		this.ruleTimeTracker = new TimeTracker();
	}

	@Override
	protected Void doCall() {
		try {
			InstanceRule instanceRule;
			while ((instanceRule = rulesToRun.poll()) != null) {
				// processing cancelled
				if (Thread.currentThread().isInterrupted()) {
					break;
				}
				processedRule = instanceRule.getRuleInstanceName();

				if (invokeRule(instanceRule)) {
					ranRules.add(instanceRule);
				} else {
					failedRules.add(instanceRule);
				}
			}
		} finally {
			processedRule = null;
			unregisterFrom.remove(this);
		}
		return null;
	}

	private boolean invokeRule(InstanceRule instanceRule) {
		try {
			return transactionSupport
					.invokeInTx(new RuleCallable(instanceRule, context, ruleTimeTracker));
		} catch (Exception e) {
			LOGGER.warn("Rule [{}] invocation failed due to: ", instanceRule.getRuleInstanceName(), e.getMessage(), e);
			return false;
		}
	}

	/**
	 * Gets the active rules.
	 *
	 * @return the active rules
	 */
	@Override
	public Collection<String> getPendingRules() {
		return collectRuleIds(rulesToRun);
	}

	/**
	 * Gets the failed rules.
	 *
	 * @return the failed rules
	 */
	@Override
	public Collection<String> getFailedRules() {
		return collectRuleIds(failedRules);
	}

	/**
	 * Gets the completed rules.
	 *
	 * @return the completed rules
	 */
	@Override
	public Collection<String> getCompletedRules() {
		return collectRuleIds(ranRules);
	}

	private Collection<String> collectRuleIds(Collection<InstanceRule> instanceRules) {
		List<String> ruleIds = new LinkedList<>();
		for (InstanceRule instanceRule : instanceRules) {
			ruleIds.add(instanceRule.getRuleInstanceName());
		}
		return ruleIds;
	}

	@Override
	public String getCurrentlyProcessedRule() {
		return processedRule;
	}

	@Override
	public String getTriggerOperation() {
		return context.getOperation();
	}

	@Override
	public long currentRuleExecutionTime() {
		return ruleTimeTracker.elapsedTime();
	}

	@Override
	public boolean isDone() {
		return rulesToRun.isEmpty() && processedRule == null;
	}

	@Override
	public RuleState getCurrentState() {
		return context.getState();
	}
}
