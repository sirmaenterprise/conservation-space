/**
 *
 */
package com.sirma.itt.seip.runtime;

import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.runtime.boot.StartupPhase;

/**
 * Provides information about the current startup process. And if the process of startup is complete or not.
 *
 * @author BBonev
 */
public class RuntimeInfo implements Sealable {
	private static final RuntimeInfo INSTANCE = new RuntimeInfo();

	/** Used to seal the object after the startup is complete. */
	private boolean sealed = false;
	private StartupPhase phase;

	/**
	 * Singleton instance.
	 *
	 * @return the runtime info
	 */
	public static synchronized RuntimeInfo instance() {
		return INSTANCE;
	}

	/**
	 * Gets the current phase at which the application is during startup.
	 *
	 * @return the current phase
	 */
	public StartupPhase getPhase() {
		return phase;
	}

	/**
	 * Checks if application is started. This means the phase is {@link StartupPhase#STARTUP_COMPLETE}
	 *
	 * @return true, if is started
	 */
	public static boolean isStarted() {
		return instance().getPhase() == StartupPhase.STARTUP_COMPLETE;
	}

	/**
	 * Sets the phase.
	 *
	 * @param phase
	 *            the new phase
	 */
	void setPhase(StartupPhase phase) {
		if (isSealed()) {
			return;
		}
		this.phase = phase;
	}

	/**
	 * Checks if info is not mutable.
	 *
	 * @return true, if is immutable and <code>false</code> if is still mutable
	 */
	@Override
	public boolean isSealed() {
		return sealed;
	}

	/**
	 * Mark the startup for completed and prevents any more modifications
	 */
	@Override
	public void seal() {
		setPhase(StartupPhase.STARTUP_COMPLETE);
		sealed = true;
	}

}
