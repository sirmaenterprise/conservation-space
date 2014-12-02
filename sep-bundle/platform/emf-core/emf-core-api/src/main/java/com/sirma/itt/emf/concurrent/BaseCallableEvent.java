package com.sirma.itt.emf.concurrent;

import java.util.concurrent.Callable;

import com.sirma.itt.emf.event.EmfEvent;
import com.sirma.itt.emf.security.context.SecurityContext;
import com.sirma.itt.emf.util.Documentation;

/**
 * Base event that can be used for asynchronous task execution on the EJB thread pool. The benefit
 * the possible use of full transaction support for the executed operation if needed. For concrete
 * use see sub classes if this.
 * 
 * @author BBonev
 */
@Documentation("Base event that can be used for asynchronous task execution on the EJB thread pool. The benefit"
		+ " the possible use of full transaction support for the executed operation if needed. For concrete use see sub classes if this.")
public abstract class BaseCallableEvent implements EmfEvent {

	/** The callable. */
	private final Callable<?> callable;

	/** The security context. */
	private final SecurityContext securityContext;

	/**
	 * Instantiates a new base callable event.
	 * 
	 * @param callable
	 *            the callable
	 */
	public BaseCallableEvent(Callable<?> callable) {
		this(callable, null);
	}

	/**
	 * Instantiates a new base callable event.
	 * 
	 * @param callable
	 *            the callable
	 * @param securityContext
	 *            the security context
	 */
	public BaseCallableEvent(Callable<?> callable, SecurityContext securityContext) {
		this.callable = callable;
		this.securityContext = securityContext;
	}

	/**
	 * Getter method for callable.
	 *
	 * @return the callable
	 */
	public Callable<?> getCallable() {
		return callable;
	}

	/**
	 * Getter method for securityContext.
	 *
	 * @return the securityContext
	 */
	public SecurityContext getSecurityContext() {
		return securityContext;
	}
}
