package com.sirma.itt.seip.template.actions;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Carries the data required for the set template as primary action.
 *
 * @author Vilizar Tsonev
 */
public class SetTemplateAsPrimaryActionRequest extends ActionRequest {

	private static final long serialVersionUID = -1604538687096075567L;
	protected static final String OPERATION_NAME = "setTemplateAsPrimary";

	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}
}
