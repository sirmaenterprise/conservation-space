package com.sirmaenterprise.sep.eai.spreadsheet.model.error;

import com.sirma.itt.seip.eai.model.error.ErrorBuilderProvider;

/**
 * Extend {@link ErrorBuilderProvider} by including a message for success.
 * 
 * @author bbanchev
 */
public class SpreadsheetValidationReport extends ErrorBuilderProvider {
	/** Message for success. */
	public static final String MSG_SUCCESSFUL_VALIDATION = "Successful validation!";

	@Override
	public synchronized boolean hasErrors() {
		return super.hasErrors() && !(get().length() == MSG_SUCCESSFUL_VALIDATION.length()
				&& toString().trim().equals(MSG_SUCCESSFUL_VALIDATION));
	}
}
