package com.sirma.itt.seip.instance.archive;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.event.AbstractInstanceEvent;

/**
 * Event fired to notify that new instance is scheduled for deletion. The instance is not deleted at the time of the
 * event. This event is not fired for every instance that is dependent to the current but only for the first.
 *
 * @author BBonev
 */

@Documentation("Event fired to notify that new instance is scheduled for deletion. "
		+ "The instance is not deleted at the time of the event. "
		+ "This event is not fired for every instance that is dependent to the current but only for the first.")
public class ArchivedInstanceAddedEvent extends AbstractInstanceEvent<Instance> {

	private final String transactionId;

	/**
	 * Instantiates a new archived instance added event.
	 *
	 * @param instance
	 *            the instance
	 * @param transactionId
	 *            the transaction id
	 */
	public ArchivedInstanceAddedEvent(Instance instance, String transactionId) {
		super(instance);
		this.transactionId = transactionId;
	}

	/**
	 * Getter method for transactionId.
	 *
	 * @return the transactionId
	 */
	public String getTransactionId() {
		return transactionId;
	}

}
