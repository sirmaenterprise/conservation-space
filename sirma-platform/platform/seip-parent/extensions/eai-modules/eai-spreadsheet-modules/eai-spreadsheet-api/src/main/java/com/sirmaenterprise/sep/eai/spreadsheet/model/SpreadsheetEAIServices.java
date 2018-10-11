package com.sirmaenterprise.sep.eai.spreadsheet.model;

import com.sirma.itt.seip.eai.service.communication.EAIServiceIdentifier;

/**
 * Defines the supported services by the EAI spreadsheet API.
 * 
 * @author bbanchev
 */
public enum SpreadsheetEAIServices implements EAIServiceIdentifier {
	/** Search, validate and retrieve objects from spreadsheet. */
	PREPARE,
	/** Search and retrieve single full object from spreadsheet. */
	RETRIEVE;

	@Override
	public String getServiceId() {
		return name();
	}
}