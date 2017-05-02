package com.sirmaenterprise.sep.eai.spreadsheet.service.communication.request;

import javax.inject.Singleton;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.model.ServiceRequest;
import com.sirma.itt.seip.eai.service.communication.EAIServiceIdentifier;
import com.sirma.itt.seip.eai.service.communication.request.EAIRequestProviderAdapter;
import com.sirma.itt.seip.plugin.Extension;
import com.sirmaenterprise.sep.eai.spreadsheet.configuration.SpreadsheetIntegrationConfigurationProvider;
import com.sirmaenterprise.sep.eai.spreadsheet.model.SpreadsheetEAIServices;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.SpreadsheetIntegrationServiceRequest;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.SpreadsheetReadServiceRequest;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.arg.IntegrationRequestArgument;
import com.sirmaenterprise.sep.eai.spreadsheet.model.request.arg.ReadRequestArgument;

/**
 * Simple request provider building a {@link SpreadsheetReadServiceRequest} with specific parameters based on the
 * service request. Implemented for services listed in {@link SpreadsheetEAIServices}
 * 
 * @author bbanchev
 */
@Singleton
@Extension(target = EAIRequestProviderAdapter.PLUGIN_ID, order = 5)
public class SpreadsheetInstanceRequestProvider implements EAIRequestProviderAdapter {

	@SuppressWarnings("unchecked")
	@Override
	public <R extends ServiceRequest> R buildRequest(EAIServiceIdentifier serviceId, Object sourceArgument)
			throws EAIException {
		if (SpreadsheetEAIServices.PREPARE.equals(serviceId)) {
			return (R) new SpreadsheetReadServiceRequest((ReadRequestArgument) sourceArgument);
		} else if (SpreadsheetEAIServices.RETRIEVE.equals(serviceId)) {
			return (R) new SpreadsheetIntegrationServiceRequest((IntegrationRequestArgument) sourceArgument);
		}
		throw new EAIException("Unsupported service request: " + serviceId);
	}

	@Override
	public String getName() {
		return SpreadsheetIntegrationConfigurationProvider.SYSTEM_ID;
	}

}
