package com.sirma.itt.seip.tasks;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.security.annotation.RunAsSystem;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Asynchronous observer that handles the execution of asynchronous scheduler actions. This is used only for
 * {@link SchedulerEntryType#EVENT} actions configured with {@link SchedulerConfiguration#setSynchronous(boolean) SchedulerConfiguration.setSynchronous(false)}
 * <br>The thransaction management is set to bean, so that the actual transactions are handled by the concrete
 * {@link TransactionSupport} implementation
 *
 * @author BBonev
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class AsyncSchedulerExecuter {

	@Inject
	private TransactionSupport transactionSupport;

	/**
	 * Execute action asynchronously.
	 *
	 * @param event
	 *            the event
	 */
	@Asynchronous
	@SuppressWarnings("static-method")
	public void executeAsyncAction(@Observes @ExecuteAs(RunAs.DEFAULT) AsyncSchedulerExecuterEvent event) {
		invokeInTx(event);
	}

	private void invokeInTx(AsyncSchedulerExecuterEvent event) {
		transactionSupport.invokeInNewTx((Executable) event::call);
	}

	/**
	 * Execute action asynchronously.
	 *
	 * @param event
	 *            the event
	 */
	@Asynchronous
	@SuppressWarnings("static-method")
	public void executeAsyncActionAsAllTenants(
			@Observes @ExecuteAs(RunAs.ALL_TENANTS) AsyncSchedulerExecuterEvent event) {
		invokeInTx(event);
	}

	/**
	 * Execute action asynchronously without starting a transaction
	 *
	 * @param event
	 *            the event
	 */
	@RunAsSystem(protectCurrentTenant = false)
	@Asynchronous
	@SuppressWarnings("static-method")
	public void executeAsyncActionAsSystem(@Observes @ExecuteAs(RunAs.SYSTEM) AsyncSchedulerExecuterEvent event) {
		invokeInTx(event);
	}

	/**
	 * Execute action asynchronously without starting a transaction
	 *
	 * @param event
	 *            the event
	 */
	@RunAsSystem
	@Asynchronous
	@SuppressWarnings("static-method")
	public void executeAsyncActionAsAdmin(@Observes @ExecuteAs(RunAs.ADMIN) AsyncSchedulerExecuterEvent event) {
		invokeInTx(event);
	}

	/*
	 * No Tx observers
	 */

	/**
	 * Execute action asynchronously without starting a transaction
	 *
	 * @param event
	 *            the event
	 */
	@Asynchronous
	@SuppressWarnings("static-method")
	public void executeAsyncAction(@Observes @ExecuteAs(RunAs.DEFAULT) AsyncNoTxSchedulerExecuterEvent event) {
		event.call();
	}

	/**
	 * Execute action asynchronously.
	 *
	 * @param event
	 *            the event
	 */
	@Asynchronous
	@SuppressWarnings("static-method")
	public void executeAsyncActionAsAllTenants(
			@Observes @ExecuteAs(RunAs.ALL_TENANTS) AsyncNoTxSchedulerExecuterEvent event) {
		event.call();
	}

	/**
	 * Execute action asynchronously without starting a transaction
	 *
	 * @param event
	 *            the event
	 */
	@RunAsSystem(protectCurrentTenant = false)
	@Asynchronous
	@SuppressWarnings("static-method")
	public void executeAsyncActionAsSystem(@Observes @ExecuteAs(RunAs.SYSTEM) AsyncNoTxSchedulerExecuterEvent event) {
		event.call();
	}

	/**
	 * Execute action asynchronously without starting a transaction
	 *
	 * @param event
	 *            the event
	 */
	@RunAsSystem
	@Asynchronous
	@SuppressWarnings("static-method")
	public void executeAsyncActionAsAdmin(@Observes @ExecuteAs(RunAs.ADMIN) AsyncNoTxSchedulerExecuterEvent event) {
		event.call();
	}

}
