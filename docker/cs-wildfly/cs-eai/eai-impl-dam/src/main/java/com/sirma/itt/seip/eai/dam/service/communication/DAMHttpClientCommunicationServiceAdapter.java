package com.sirma.itt.seip.eai.dam.service.communication;

import javax.inject.Singleton;

import com.sirma.itt.seip.eai.cs.model.internal.CSExternalInstanceId;
import com.sirma.itt.seip.eai.cs.service.communication.CSClientCommunicationAdapter;
import com.sirma.itt.seip.eai.dam.configuration.DAMIntegrationConfigurationProvider;
import com.sirma.itt.seip.eai.dam.service.communication.response.ContentDownloadServiceRequest;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.model.ServiceResponse;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.response.StreamingResponse;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationServiceAdapter;
import com.sirma.itt.seip.plugin.Extension;

/**
 * The DAM client adapter holding specific logic related to DAM requests. Added service support for
 * {@link DAMEAIServices#CONTENT}
 *
 * @author bbanchev
 */
@Singleton
@Extension(target = EAICommunicationServiceAdapter.PLUGIN_ID, order = 11)
public class DAMHttpClientCommunicationServiceAdapter extends CSClientCommunicationAdapter {

	/**
	 * Add additional service processing for {@link DAMEAIServices#CONTENT}
	 * 
	 * @param request
	 *            is the request to invoke
	 */
	@Override
	public ServiceResponse invoke(RequestInfo request) throws EAIException {
		if (DAMEAIServices.CONTENT.equals(request.getServiceId())) {
			return invokeContentImport(request);
		}
		return super.invoke(request);
	}

	private StreamingResponse invokeContentImport(RequestInfo request) throws EAIException {
		CSExternalInstanceId externalId = ((ContentDownloadServiceRequest) request.getRequest()).getInstanceId();
		return executeMethodWithResponse(
				createGetMethod(DAMEAIServices.CONTENT, externalId.getSourceSystemId(), externalId.getExternalId()),
				CSClientCommunicationAdapter::readStreamFromResponse);
	}

	@Override
	public String getName() {
		return DAMIntegrationConfigurationProvider.SYSTEM_ID;
	}

}
