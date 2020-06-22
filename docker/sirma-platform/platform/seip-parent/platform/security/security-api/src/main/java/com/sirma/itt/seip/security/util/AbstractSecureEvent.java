/**
 *
 */
package com.sirma.itt.seip.security.util;

/**
 * Base helper event class for classes that need basic secure event implementation. The class provides a field for
 * {@link SecureExecutor}.
 *
 * @author BBonev
 */
public abstract class AbstractSecureEvent implements SecureEvent {

	private SecureExecutor executor;

	@Override
	public SecureExecutor getSecureExecutor() {
		return executor;
	}

	@Override
	public void setSecureExecutor(SecureExecutor executor) {
		this.executor = executor;
	}

}
