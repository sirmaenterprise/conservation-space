/**
 *
 */
package com.sirma.itt.seip.tenant.security.interceptor;

import java.lang.annotation.Annotation;
import java.util.function.BiConsumer;

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
import com.sirma.itt.seip.security.util.SecureEventAllTenants;
import com.sirma.itt.seip.tenant.context.TenantInfo;
import com.sirma.itt.seip.tenant.context.TenantManager;

/**
 * Intercepts event service and calls the delegate for every active tenant once. This is done for events that implement
 * the {@link SecureEventAllTenants} interface. This decorator should be called before the SecureEventServiceDecorator.
 *
 * @author BBonev
 */
@Decorator
@Priority(Interceptor.Priority.APPLICATION - 100)
public abstract class SecureEventServiceAllTenantsDecorator implements EventService {

	@Inject
	@Delegate
	private EventService delegate;
	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private TenantManager tenantManager;

	@Override
	public void fire(EmfEvent event, Annotation... qualifiers) {
		if (shouldDecorate(event)) {
			SecureEventAllTenants secureEventAllTenants = (SecureEventAllTenants) event;
			tenantManager.getActiveTenantsInfo(secureEventAllTenants.allowParallel()).forEach(
					info -> fireInternal(info, delegate::fire, event, qualifiers));
			return;
		}
		delegate.fire(event, qualifiers);
	}

	private <T extends EmfEvent> boolean shouldDecorate(T event) {
		return event instanceof SecureEventAllTenants && ((SecureEvent) event).getSecureExecutor() == null
				&& securityContextManager.getCurrentContext().isSystemTenant();
	}

	private <T extends EmfEvent> void fireInternal(TenantInfo info, BiConsumer<T, Annotation[]> delegateMethod, T event,
			Annotation... qualifiers) {
		securityContextManager.initializeTenantContext(info.getTenantId());
		try {
			delegateMethod.accept(prepareEvent(event), qualifiers);
		} finally {
			securityContextManager.endContextExecution();
		}
	}

	/**
	 * This method should be called to clean the secure executor set during the previous invocation of the method,
	 * because single event instance is fired for different tenants if context should be cleared before execution so
	 * that it could be set by the other decorator. For parallel processing the cleaning of the secure executor is not
	 * enough so new event instance need to be fired. The copy is provided by calling
	 * {@link SecureEventAllTenants#copy()}
	 *
	 * @param <T>
	 *            the generic type
	 * @param event
	 *            the event
	 * @return the input argument or copy of the event instance
	 */
	@SuppressWarnings("unchecked")
	private static <T extends EmfEvent> T prepareEvent(T event) {
		SecureEventAllTenants secureEvent = (SecureEventAllTenants) event;
		if (secureEvent.allowParallel()) {
			// create copy only if needed
			SecureEvent copy = secureEvent.copy();
			if (copy == null) {
				throw new IllegalArgumentException(
						"Cannot fire parallel multitenant event without providing a correct implementation of the method SecureEventAllTenants.copy()");
			}
			return (T) copy;
		}
		secureEvent.setSecureExecutor(null);
		return event;
	}

	@Override
	public void fireNextPhase(TwoPhaseEvent event, Annotation... qualifiers) {
		if (shouldDecorate(event)) {
			SecureEventAllTenants secureEventAllTenants = (SecureEventAllTenants) event;
			tenantManager.getActiveTenantsInfo(secureEventAllTenants.allowParallel()).forEach(
					info -> fireInternal(info, delegate::fireNextPhase, event, qualifiers));
			return;
		}
		delegate.fireNextPhase(event, qualifiers);
	}

}
