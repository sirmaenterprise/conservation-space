package com.sirmaenterprise.sep.bpm.camunda.transitions.model;

import com.sirma.itt.seip.definition.rest.DefinitionModelObject;

/**
 * Wrapper object for {@link DefinitionModelObject} used to trigger specific json serialization.
 * 
 * @author bbanchev
 */
public class BPMDefinitionModelObject {
	private DefinitionModelObject model;

	/**
	 * Instantiates a new BPM definition model object.
	 *
	 * @param model
	 *            the wrapped model containing the definition model and the instance
	 */
	public BPMDefinitionModelObject(DefinitionModelObject model) {
		this.model = model;
	}

	/**
	 * Gets the wrapped instance.
	 *
	 * @return the {@link DefinitionModelObject} instance
	 */
	public DefinitionModelObject getModel() {
		return model;
	}

}
