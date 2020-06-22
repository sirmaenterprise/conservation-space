/**
 *
 */
package com.sirma.itt.seip.instance.lock;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.event.EmfEvent;

/**
 * Event fired before performing the instance unlock operation. The event is fired after performing any permissions
 * check and we are sure the instance could be unlocked.
 *
 * @author BBonev
 */
@Documentation("Event fired before performing the instance unlock operation. The event is fired after performing any permissions check and we are sure the instance could be unlocked.")
public class BeforeUnlockEvent implements EmfEvent {

	private final LockInfo lockInfo;

	/**
	 * Instantiates a new lock event.
	 *
	 * @param lockInfo
	 *            the lock info
	 */
	public BeforeUnlockEvent(LockInfo lockInfo) {
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
