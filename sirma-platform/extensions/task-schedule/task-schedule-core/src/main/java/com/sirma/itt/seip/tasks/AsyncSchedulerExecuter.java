package com.sirma.itt.seip.tasks;

import com.sirma.itt.seip.security.annotation.RunAsSystem;

import javax.ejb.Asynchronous;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;

/**
 * Asynchronous observer that handles the execution of asynchronous actions.
 *
 * @author BBonev
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class AsyncSchedulerExecuter {

	/**
	 * Execute action asynchronously.
	 *
	 * @param event
	 *            the event
	 */
	@Asynchronous
	@SuppressWarnings("static-method")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void executeAsyncAction(@Observes @ExecuteAs(RunAs.DEFAULT) AsyncSchedulerExecuterEvent event) {
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void executeAsyncActionAsAllTenants(
			@Observes @ExecuteAs(RunAs.ALL_TENANTS) AsyncSchedulerExecuterEvent event) {
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void executeAsyncActionAsSystem(@Observes @ExecuteAs(RunAs.SYSTEM) AsyncSchedulerExecuterEvent event) {
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
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void executeAsyncActionAsAdmin(@Observes @ExecuteAs(RunAs.ADMIN) AsyncSchedulerExecuterEvent event) {
		event.call();
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
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
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
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
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
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
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
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void executeAsyncActionAsAdmin(@Observes @ExecuteAs(RunAs.ADMIN) AsyncNoTxSchedulerExecuterEvent event) {
		event.call();
	}

	/*
	 * Synchronous events
	 */

	/**
	 * Execute action asynchronously without starting a transaction
	 *
	 * @param event
	 *            the event
	 */
	@SuppressWarnings("static-method")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void executeSyncAction(@Observes @ExecuteAs(RunAs.DEFAULT) SchedulerExecuterEvent event) {
		event.call();
	}

	/**
	 * Execute action asynchronously.
	 *
	 * @param event
	 *            the event
	 */
	@SuppressWarnings("static-method")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void executeSyncActionAsAllTenants(@Observes @ExecuteAs(RunAs.ALL_TENANTS) SchedulerExecuterEvent event) {
		event.call();
	}

	/**
	 * Execute action asynchronously without starting a transaction
	 *
	 * @param event
	 *            the event
	 */
	@RunAsSystem(protectCurrentTenant = false)
	@SuppressWarnings("static-method")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void executeSyncActionAsSystem(@Observes @ExecuteAs(RunAs.SYSTEM) SchedulerExecuterEvent event) {
		event.call();
	}

	/**
	 * Execute action asynchronously without starting a transaction
	 *
	 * @param event
	 *            the event
	 */
	@RunAsSystem
	@SuppressWarnings("static-method")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public void executeSyncActionAsAdmin(@Observes @ExecuteAs(RunAs.ADMIN) SchedulerExecuterEvent event) {
		event.call();
	}

}
