package com.sirma.itt.seip.tx;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.security.util.AbstractSecureEvent;

/**
 * Base event for events that execute logic at transaction phases.
 *
 * @author BBonev
 */
public abstract class BaseTransactionEvent extends AbstractSecureEvent {

	protected final Executable executable;

	/**
	 * Instantiates a new base transaction event.
	 *
	 * @param executable
	 *            the executable
	 */
	public BaseTransactionEvent(Executable executable) {
		this.executable = executable;
	}

	/**
	 * Invoke.
	 */
	public void invoke() {
		execute(executable);
	}

}