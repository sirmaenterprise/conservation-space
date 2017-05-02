/**
 *
 */
package com.sirma.itt.seip.security.decorators;

import java.lang.annotation.Annotation;

import javax.annotation.Priority;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;
import javax.interceptor.Interceptor;

import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.security.util.SecureEvent;
import com.sirma.itt.seip.security.util.SecureExecutor;

/**
 * Automatically initialize security context executor for secure events.
 *
 * @author BBonev
 */
@Decorator
@Priority(Interceptor.Priority.APPLICATION)
public abstract class SecureEventServiceDecorator implements EventService {

	@Inject
	@Delegate
	private EventService delegate;
	@Inject
	private SecurityContextManager securityContextManager;

	@Override
	public void fire(EmfEvent event, Annotation... qualifiers) {
		delegate.fire(decorateEvent(event), qualifiers);
	}

	@Override
	public void fireNextPhase(TwoPhaseEvent event, Annotation... qualifiers) {
		delegate.fireNextPhase(decorateEvent(event), qualifiers);
	}

	private <T extends EmfEvent> T decorateEvent(T event) {
		if (event instanceof SecureEvent) {
			SecureEvent secureEvent = (SecureEvent) event;
			if (secureEvent.getSecureExecutor() == null) {
				secureEvent.setSecureExecutor(new SecureExecutor(securityContextManager));
			}
		}
		return event;
	}

}
