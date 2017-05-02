package com.sirma.itt.seip.template.actions;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Carries the data required for the template activation action.
 *
 * @author Vilizar Tsonev
 */
public class ActivateTemplateActionRequest extends ActionRequest {

	private static final long serialVersionUID = -7196248185133377777L;
	protected static final String OPERATION_NAME = "activateTemplate";

	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}
}
