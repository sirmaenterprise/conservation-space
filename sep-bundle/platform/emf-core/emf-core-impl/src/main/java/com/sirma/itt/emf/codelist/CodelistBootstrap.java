/*
 *
 */
package com.sirma.itt.emf.codelist;

import java.util.concurrent.Callable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.emf.codelist.event.LoadCodelists;
import com.sirma.itt.emf.concurrent.NonTxAsyncCallableEvent;
import com.sirma.itt.emf.event.EventService;

/**
 * Forcefully initialize codelists on server startup.
 *
 * @author BBonev
 */
@ApplicationScoped
public class CodelistBootstrap {

	/** The codelist service. */
	@Inject
	private CodelistService codelistService;
	/** The event service. */
	@Inject
	private EventService eventService;

	/**
	 * Bootstrap codelists.
	 */
	public void bootstrapCodelists() {
		codelistService.getCodeValues(0);
	}

	/**
	 * Initializes the codelists.
	 * 
	 * @param events
	 *            the events
	 */
	public void initCodelists(@Observes LoadCodelists events) {
		// fire asynchronous event that will trigger codelist loading
		eventService.fire(new NonTxAsyncCallableEvent(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				bootstrapCodelists();
				return null;
			}
		}));
	}
}
