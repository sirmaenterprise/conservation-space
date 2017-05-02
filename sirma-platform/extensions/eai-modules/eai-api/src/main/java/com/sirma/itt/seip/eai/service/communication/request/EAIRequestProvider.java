/*
 * 
 */
package com.sirma.itt.seip.eai.service.communication.request;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.ServiceRequest;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.error.LoggingDTO;
import com.sirma.itt.seip.eai.service.communication.BaseEAIServices;
import com.sirma.itt.seip.eai.service.communication.EAIServiceIdentifier;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * The service is responsible to delegate generation of {@link ServiceRequest} to the registered
 * {@link EAIRequestProviderAdapter} based on the systemId
 * 
 * @author bbanchev
 */
@ApplicationScoped
public class EAIRequestProvider {
	@Inject
	@ExtensionPoint(value = EAIRequestProviderAdapter.PLUGIN_ID)
	private Plugins<EAIRequestProviderAdapter> requestProviderAdapters;

	/**
	 * Provide request wrapper based on the service id, source argument and systemId. Potentially response would wrap
	 * different data for each system/service
	 *
	 * @param systemId
	 *            the system id to generate for
	 * @param serviceId
	 *            the service identifier for the generated request
	 * @param sourceArgument
	 *            the source argument to use during request generation
	 * @return the generated request wrapped in {@link RequestInfo}
	 * @throws EAIException
	 *             during error on request generation or sourceArgument parsing
	 */
	public RequestInfo provideRequest(String systemId, EAIServiceIdentifier serviceId, Object sourceArgument)
			throws EAIException {
		EAIRequestProviderAdapter requestAdapter = requestProviderAdapters
				.get(systemId.toUpperCase())
					.orElseThrow(() -> new EAIRuntimeException("Not implemented "
							+ EAIRequestProviderAdapter.class.getSimpleName() + " for system " + systemId));
		if (BaseEAIServices.LOGGING.equals(serviceId)) {
			return new RequestInfo(systemId, serviceId, buildDefaultLogRequest(sourceArgument));
		}
		return new RequestInfo(systemId, serviceId, requestAdapter.buildRequest(serviceId, sourceArgument));
	}

	private static ServiceRequest buildDefaultLogRequest(Object sourceArgument) {

		if (sourceArgument instanceof Throwable) {
			return createExceptionLogger((Throwable) sourceArgument);
		}
		throw new EAIRuntimeException("Unsupported logging source information: " + sourceArgument);
	}

	private static ServiceRequest createExceptionLogger(Throwable sourceArgument) {
		String origin = null;
		if (sourceArgument instanceof EAIReportableException) {
			origin = ((EAIReportableException) sourceArgument).getOrigin();
		}
		LoggingDTO informationWrapper = new LoggingDTO(sourceArgument.getMessage(), "-");
		informationWrapper.setOrigin(origin);
		return informationWrapper;
	}

}
