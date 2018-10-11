package com.sirma.itt.seip.eai.service.communication;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIReportableException;
import com.sirma.itt.seip.eai.model.ServiceRequest;
import com.sirma.itt.seip.eai.model.ServiceResponse;
import com.sirma.itt.seip.eai.model.communication.RequestInfo;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Adapter for communication with subsystem that is independent of the protocols used
 * 
 * @author bbanchev
 */
public interface EAICommunicationServiceAdapter extends Plugin, Named { // NOSONAR
	/** The Plugin name. */
	String PLUGIN_ID = "EAICommunicationServiceAdapter";

	/**
	 * Invoke a service request.
	 *
	 * @param request
	 *            the request to execute. Any subclass of {@link ServiceRequest} suitable for the service
	 * @return the external service response
	 * @throws EAIException
	 *             on error during request processing
	 * @throws EAIReportableException
	 *             on response parsing or during communication with the cause wrapped
	 */
	ServiceResponse invoke(RequestInfo request) throws EAIException;

}
