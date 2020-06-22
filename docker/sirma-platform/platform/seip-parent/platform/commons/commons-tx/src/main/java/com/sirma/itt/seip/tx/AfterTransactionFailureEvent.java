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
public class AfterTransactionFailureEvent extends BaseTransactionEvent {

	/**
	 * Instantiates a new after transaction failure event.
	 *
	 * @param executable
	 *            the executable
	 */
	public AfterTransactionFailureEvent(Executable executable) {
		super(executable);
	}

}
