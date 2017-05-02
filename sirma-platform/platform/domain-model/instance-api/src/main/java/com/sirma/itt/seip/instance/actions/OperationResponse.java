package com.sirma.itt.seip.instance.actions;

import com.sirma.itt.seip.Pair;

/**
 * A pair containing a {@link OperationStatus} and any Object. Used with services when a custom status is needed.
 *
 * @author nvelkov
 */
public class OperationResponse extends Pair<OperationStatus, Object> {

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
	public OperationResponse(OperationStatus first, Object second) {
		super(first, second);
	}

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public OperationStatus getStatus() {
		return getFirst();
	}

	/**
	 * Sets the status.
	 *
	 * @param status
	 *            the new status
	 */
	public void setStatus(OperationStatus status) {
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
