package com.sirma.itt.seip.template.actions;

import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Carries the data required for the template rule editing.
 *
 * @author Vilizar Tsonev
 */
public class EditTemplateRuleActionRequest extends ActionRequest {

	private static final long serialVersionUID = -9141491715204212635L;

	protected static final String OPERATION_NAME = "editTemplateRule";

	private String rule;

	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}
}
