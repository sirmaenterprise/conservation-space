package com.sirma.itt.emf.search.event;

import com.sirma.itt.emf.event.AuditableOperationEvent;

/**
 * Executed when a basic or advanced search is performed.
 * 
 * @author nvelkov
 */
public class SearchExecutedEvent implements AuditableOperationEvent {

	/** The operation id. */
	private String operationId;
	
	/**
	 * Instantiates a new search executed event.
	 *
	 * @param operationId the operation id
	 */
	public SearchExecutedEvent(String operationId) {
		super();
		this.operationId = operationId;
	}


	@Override
	public String getOperationId() {
		return operationId;
	}

}
