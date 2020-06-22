package com.sirma.itt.seip.synchronization;

/**
 * Configuration object to be passed during synchronization process.
 *
 * @author BBonev
 */
public class SyncRuntimeConfiguration {

	private boolean forceSynchronization;
	private boolean allowDelete = false;

	/**
	 * Enable force synchronization. When this is enabled any entities that are found not equal will forcefully marked
	 * for modified.
	 *
	 * @return the current instance
	 */
	public SyncRuntimeConfiguration enableForceSynchronization() {
		forceSynchronization = true;
		return this;
	}

	/**
	 * Checks if is force synchronization enabled. By default is not enabled
	 *
	 * @return true, if is force synchronization enabled
	 */
	public boolean isForceSynchronizationEnabled() {
		return forceSynchronization;
	}

	/**
	 * Allow delete of resource that are not generally deleted.
	 * 
	 * @return the current instance
	 */
	public SyncRuntimeConfiguration allowDelete() {
		allowDelete = true;
		return this;
	}

	/**
	 * Checks if delete is allowed.
	 *
	 * @return true, if delete is allowed
	 */
	public boolean isDeleteAllowed() {
		return allowDelete;
	}
}
