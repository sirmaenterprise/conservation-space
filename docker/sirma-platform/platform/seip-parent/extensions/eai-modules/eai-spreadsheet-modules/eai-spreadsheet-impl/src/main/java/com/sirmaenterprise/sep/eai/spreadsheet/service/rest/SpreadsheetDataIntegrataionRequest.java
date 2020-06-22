package com.sirmaenterprise.sep.eai.spreadsheet.service.rest;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.eai.model.internal.DataIntegrationRequest;
import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetEntryId;

/**
 * {@link SpreadsheetDataIntegrataionRequest} extends the {@link DataIntegrationRequest} API request wrapped by adding
 * the source and the storage context as parameters.
 * 
 * @author bbanchev
 */
public class SpreadsheetDataIntegrataionRequest extends DataIntegrationRequest<SpreadsheetEntryId> {
	private static final long serialVersionUID = 6717814639787382709L;
	private InstanceReference context;
	private InstanceReference report;
	/** import action id. */
	public static final String OPERATION_NAME = "EAISpreadsheet" + ActionTypeConstants.IMPORT;

	/**
	 * Getter method for context.
	 *
	 * @return the context
	 */
	public InstanceReference getContext() {
		return context;
	}

	/**
	 * Setter method for context.
	 *
	 * @param context
	 *            the context to set
	 */
	public void setContext(InstanceReference context) {
		this.context = context;
	}

	/**
	 * Gets the set report instance.
	 *
	 * @return the report
	 */
	public InstanceReference getReport() {
		return report;
	}

	/**
	 * Sets the report generated during the validation phase.
	 *
	 * @param report
	 *            the report to set
	 */
	public void setReport(InstanceReference report) {
		this.report = report;
	}

	@Override
	public String getOperation() {
		return SpreadsheetDataIntegrataionRequest.OPERATION_NAME;
	}
}
