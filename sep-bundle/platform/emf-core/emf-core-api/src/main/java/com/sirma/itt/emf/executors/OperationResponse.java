package com.sirma.itt.emf.executors;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.scheduler.SchedulerEntryStatus;

/**
 * A pair containing a {@link SchedulerEntryStatus} and any Object. Used with services when a custom
 * status is needed.
 * 
 * @author nvelkov
 */
public class OperationResponse extends Pair<SchedulerEntryStatus, Object> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1654539892309011502L;

	/**
	 * Instantiates a new operation response.
	 * 
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 */
	public OperationResponse(SchedulerEntryStatus first, Object second) {
		super(first, second);
	}

	/**
	 * Gets the status.
	 * 
	 * @return the status
	 */
	public SchedulerEntryStatus getStatus() {
		return getFirst();
	}

	/**
	 * Sets the status.
	 * 
	 * @param status
	 *            the new status
	 */
	public void setStatus(SchedulerEntryStatus status) {
		setFirst(status);
	}

	/**
	 * Gets the response.
	 * 
	 * @return the response
	 */
	public Object getResponse() {
		return getSecond();
	}

	/**
	 * Sets the response.
	 * 
	 * @param response
	 *            the new response
	 */
	public void setResponse(Object response) {
		setSecond(response);
	}

}
