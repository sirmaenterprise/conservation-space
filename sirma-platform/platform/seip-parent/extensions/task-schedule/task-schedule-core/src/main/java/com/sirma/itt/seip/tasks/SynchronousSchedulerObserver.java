package com.sirma.itt.seip.tasks;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import com.sirma.itt.seip.security.annotation.RunAsSystem;

/**
 * Observer that handles the execution of synchronous scheduler actions without the overhead of the EJB classes as in
 * {@link AsyncSchedulerExecuter}
 *
 * @author BBonev
 */
@Singleton
public class SynchronousSchedulerObserver {

	/**
	 * Execute action asynchronously without starting a transaction
	 *
	 * @param event
	 *            the event
	 */
	@SuppressWarnings("static-method")
	public void executeSyncActionAsAllTenants(@Observes @ExecuteAs(RunAs.ALL_TENANTS) SchedulerAllTenantsExecuterEvent event) {
		event.call();
	}

	/**
	 * Execute action asynchronously without starting a transaction
	 *
	 * @param event
	 *            the event
	 */
	@SuppressWarnings("static-method")
	@RunAsSystem(protectCurrentTenant = false)
	public void executeSyncActionAsSystem(@Observes @ExecuteAs(RunAs.SYSTEM) SchedulerExecuterEvent event) {
		event.call();
	}

	/**
	 * Execute action asynchronously without starting a transaction
	 *
	 * @param event
	 *            the event
	 */
	@SuppressWarnings("static-method")
	public void executeSyncAction(@Observes @ExecuteAs(RunAs.DEFAULT) SchedulerExecuterEvent event) {
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
	public void executeSyncActionAsAdmin(@Observes @ExecuteAs(RunAs.ADMIN) SchedulerAllTenantsExecuterEvent event) {
		event.call();
	}

	/**
	 * Execute action asynchronously without starting a transaction
	 *
	 * @param event
	 *            the event
	 */
	@SuppressWarnings("static-method")
	public void executeSyncActionAsDefault(@Observes @ExecuteAs(RunAs.DEFAULT) SchedulerAllTenantsExecuterEvent event) {
		event.call();
	}

}
