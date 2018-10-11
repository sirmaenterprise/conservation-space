package com.sirma.itt.seip.definition.rest;

import java.util.Collection;

/**
 * Used to hold required data for loading instance models.
 *
 * @author A. Kunchev
 */
public class LoadModelsRequest {

	private Collection<String> instanceIds;

	private Collection<String> requestedFields;

	private String operation;

	public Collection<String> getInstanceIds() {
		return instanceIds;
	}

	public LoadModelsRequest setInstanceIds(Collection<String> instanceIds) {
		this.instanceIds = instanceIds;
		return this;
	}

	public Collection<String> getRequestedFields() {
		return requestedFields;
	}

	public LoadModelsRequest setRequestedFields(Collection<String> requestedFields) {
		this.requestedFields = requestedFields;
		return this;
	}

	public String getOperation() {
		return operation;
	}

	public LoadModelsRequest setOperation(String operation) {
		this.operation = operation;
		return this;
	}

}
