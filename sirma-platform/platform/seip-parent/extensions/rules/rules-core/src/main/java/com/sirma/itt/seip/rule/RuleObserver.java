package com.sirma.itt.seip.rule;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.emf.rule.RulesConfigurations;
import com.sirma.itt.emf.rule.invoker.RuleInvoker;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.event.InstancePersistedEvent;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Rule Observer class, that will listen for persisted events and call underlying rules.
 *
 * @author Hristo Lungov
 */
@ApplicationScoped
public class RuleObserver {

	@Inject
	private RulesConfigurations rulesConfigurations;

	@Inject
	private RuleInvoker ruleInvoker;

	@Inject
	private TransactionSupport transactionSupport;

	@Inject
	private LockService lockService;

	/**
	 * Executes rules for the given persisted instance if its not locked.
	 *
	 * @param event
	 *            the event
	 */
	public void onInstancePersistedEvent(@Observes final InstancePersistedEvent<? extends Instance> event) {
		if (!rulesConfigurations.getIsRulesActivate()) {
			return;
		}
		//https://issues.jboss.org/browse/WELD-2019
		final Instance instance = event.getInstance();
		String operationId = event.getOperationId();
		Instance oldVersionInstance = event.getOldVersion();
		RuleInvoker invoker = ruleInvoker;
		LockService lockServiceInvoker = lockService;
		transactionSupport.invokeOnSuccessfulTransactionInTx(() -> {
			if (!lockServiceInvoker.lockStatus(instance.toReference()).isLocked()) {
				invoker.invokeRules(instance, oldVersionInstance, operationId);
			}
		});
	}
}
