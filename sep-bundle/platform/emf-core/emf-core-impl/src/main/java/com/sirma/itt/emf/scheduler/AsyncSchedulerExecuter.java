package com.sirma.itt.emf.scheduler;

import java.util.concurrent.Callable;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.emf.scheduler.event.AsyncSchedulerExecuterEvent;
import com.sirma.itt.emf.security.context.SecurityContext;
import com.sirma.itt.emf.security.context.SecurityContextManager;

/**
 * Asynchronous observer that handles the execution of asynchronous actions.
 * 
 * @author BBonev
 */
@Stateless
public class AsyncSchedulerExecuter {

	/** The scheduler executer. */
	@Inject
	private SchedulerExecuter schedulerExecuter;

	/**
	 * Execute action asynchronously.
	 * 
	 * @param event
	 *            the event
	 */
	@Asynchronous
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void executeAsyncAction(@Observes final AsyncSchedulerExecuterEvent event) {
		SecurityContext context = event.getSecurityContext();
		// if we have a security context we will execute the action in that context
		if (context != null) {
			SecurityContextManager.callAs(context.getAuthentication(), context.getEffectiveAuthentication(),
					new Callable<Void>() {

						@Override
						public Void call() throws Exception {
							execute(event.getSchedulerEntry());
							return null;
						}
					});
		} else {
			execute(event.getSchedulerEntry());
		}
	}

	/**
	 * Execute.
	 * 
	 * @param entry
	 *            the entry
	 */
	private void execute(SchedulerEntry entry) {
		schedulerExecuter.executeImmediate(entry);
	}

}
