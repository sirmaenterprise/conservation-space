package com.sirma.itt.seip.domain.search.event;

import com.sirma.itt.seip.domain.event.OperationEvent;

/**
 * Executed when a basic or advanced search is performed.
 *
 * @author nvelkov
 */
public class SearchExecutedEvent implements OperationEvent {

	private static final String DEFAULT = "";

	/** The operation id. */
	private String operationId;

	/**
	 * Default constructor.
	 */
	public SearchExecutedEvent() {
		this(DEFAULT);
	}

	/**
	 * Instantiates a new search executed event.
	 *
	 * @param operationId
	 *            the operation id
	 */
	public SearchExecutedEvent(String operationId) {
		this.operationId = operationId;
	}

	@Override
	public String getOperationId() {
		return operationId;
	}

}
