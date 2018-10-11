package com.sirma.itt.seip.eai.service.communication;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.service.communication.response.EAIResponseReader;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * The base communication service that should be invoked directly by other services. It is responsible to route the
 * request to the specific subsystem adapter.
 * 
 * @author bbanchev
 */
@ApplicationScoped
public class EAICommunicationService {
	@Inject
	@ExtensionPoint(value = EAICommunicationServiceAdapter.PLUGIN_ID)
	private Plugins<EAICommunicationServiceAdapter> communicationAdapters;

	/**
	 * Invoke a request and generates a response ready to be parsed by {@link EAIResponseReader}
	 *
	 * @param request
	 *            the request wrapper to execute
	 * @return the response wrapper holding the result
	 * @throws EAIException
	 *             on error during request processing
	 * @throws EAIReportableException
	 *             on response parsing or during communication
	 */
	public ResponseInfo invoke(RequestInfo request) throws EAIException {
		try {
			EAICommunicationServiceAdapter serviceAdapter = communicationAdapters
					.get(request.getSystemId())
						.orElseThrow(() -> new EAIRuntimeException(
								"Missing communication adapter for : " + request.getSystemId()));
			return new ResponseInfo(request, serviceAdapter.invoke(request));
		} catch (EAIException e) {
			throw e;
		} catch (Exception e) {
			throw new EAIException("Generic failure during excution of request: " + request.getRequest(), e);
		}
	}

}
