package com.sirmaenterprise.sep.eai.spreadsheet.model.request;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import com.sirmaenterprise.sep.eai.spreadsheet.model.internal.SpreadsheetEntryId;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.arg.IntegrationRequestArgument;
import com.sirmaenterprise.sep.eai.spreadsheet.service.SpreadsheetParser;

/**
 * The {@link SpreadsheetIntegrationServiceRequest} extends the {@link SpreadsheetReadServiceRequest} using the
 * {@link IntegrationRequestArgument} as source argument
 */
public class SpreadsheetIntegrationServiceRequest extends SpreadsheetReadServiceRequest {

	private static final long serialVersionUID = 8610667099406161755L;
	private Map<String, Collection<String>> requested;

	/**
	 * Instantiate new {@link SpreadsheetIntegrationServiceRequest}
	 * 
	 * @param request
	 *            is the source argument
	 */
	public SpreadsheetIntegrationServiceRequest(IntegrationRequestArgument request) {
		super(request);
	}

	@Override
	protected IntegrationRequestArgument getRequest() {
		return (IntegrationRequestArgument) request;
	}

	/**
	 * Return requested entries model as the expected in
	 * {@link SpreadsheetParser#parseEntries(com.sirma.itt.seip.content.ContentInfo, Map)}
	 * 
	 * @return the mapped entries
	 */
	public Map<String, Collection<String>> getRequestedEntries() {
		if (requested == null) {
			requested = new LinkedHashMap<>();
			for (SpreadsheetEntryId spreadsheetEntryId : getRequest().getEntries()) {
				requested.computeIfAbsent(spreadsheetEntryId.getSheetId(), k -> new LinkedList<>())
						.add(spreadsheetEntryId.getExternalId());
			}
		}
		return requested;
	}

	@Override
	public String toString() {
		return "Integration request: " + getRequest();
	}

}
