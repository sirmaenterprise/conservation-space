package com.sirmaenterprise.sep.eai.spreadsheet.service.rest;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.actions.ActionRequest;

/**
 * Read request for spreadsheet processing. Encapsulates the spreadsheet content instance as
 * {@link #getTargetReference()}.
 * 
 * @author bbanchev
 */
public class SpreadSheetReadRequest extends ActionRequest {
	private static final long serialVersionUID = 744249286294022393L;
	/** import action id. */
	public static final String OPERATION_NAME = "EAISpreadsheetValidate";
	private InstanceReference context;

	/**
	 * Gets the set context.
	 *
	 * @return the context
	 */
	public InstanceReference getContext() {
		return context;
	}

	/**
	 * Sets the context for the operation.
	 *
	 * @param context
	 *            the context to set
	 */
	public void setContext(InstanceReference context) {
		this.context = context;
	}

	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}

}
