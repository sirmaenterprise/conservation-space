/**
 *
 */
package com.sirma.itt.seip.tx;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.annotation.Documentation;

/**
 * Internal event fired to trigger after transaction invocations.
 *
 * @author BBonev
 */
@Documentation("Internal event fired to trigger after transaction invocations.")
public class BeforeTransactionEvent extends BaseTransactionEvent {

	/**
	 * Instantiates a new before transaction event.
	 *
	 * @param executable
	 *            the executable
	 */
	public BeforeTransactionEvent(Executable executable) {
		super(executable);
	}

}
