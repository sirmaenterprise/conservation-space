package com.sirma.itt.seip.template.actions;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Carries the data required for the template deactivation action.
 *
 * @author Vilizar Tsonev
 */
public class DeactivateTemplateActionRequest extends ActionRequest {

	private static final long serialVersionUID = -6363894037322478623L;

	protected static final String OPERATION_NAME = "deactivateTemplate";

	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}
}
