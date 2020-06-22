package com.sirma.itt.seip.definition.rest;

import java.util.Collection;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Contains the data needed for conversion to JSON, primary used as response for definition model rest requests.
 *
 * @author A. Kunchev
 */
public class DefinitionModelObject {

	private Instance instance;

	private DefinitionModel definitionModel;

	private String operation;

	private Collection<String> requestedFields;

	public Instance getInstance() {
		return instance;
	}

	public DefinitionModelObject setInstance(Instance instance) {
		this.instance = instance;
		return this;
	}

	public DefinitionModel getDefinitionModel() {
		return definitionModel;
	}

	public DefinitionModelObject setDefinitionModel(DefinitionModel definitionModel) {
		this.definitionModel = definitionModel;
		return this;
	}

	public String getOperation() {
		return operation;
	}

	public DefinitionModelObject setOperation(String operation) {
		this.operation = operation;
		return this;
	}

	public Collection<String> getRequestedFields() {
		return requestedFields;
	}

	public DefinitionModelObject setRequestedFields(Collection<String> requestedFields) {
		this.requestedFields = requestedFields;
		return this;
	}

}
