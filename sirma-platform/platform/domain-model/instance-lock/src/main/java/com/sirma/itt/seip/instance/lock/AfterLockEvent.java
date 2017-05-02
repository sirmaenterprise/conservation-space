/**
 *
 */
package com.sirma.itt.seip.instance.lock;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.EmfEvent;

/**
 * Event fired after performing the instance lock operation.
 *
 * @author BBonev
 */
@Documentation("Event fired after performing the instance lock operation.")
public class AfterLockEvent implements EmfEvent {

	private final LockInfo lockInfo;

	/**
	 * Instantiates a new lock event.
	 *
	 * @param lockInfo
	 *            the lock info
	 */
	public AfterLockEvent(LockInfo lockInfo) {
		this.lockInfo = lockInfo;
	}

	/**
	 * Gets the lock info.
	 *
	 * @return the lock info
	 */
	public LockInfo getLockInfo() {
		return lockInfo;
	}
}
