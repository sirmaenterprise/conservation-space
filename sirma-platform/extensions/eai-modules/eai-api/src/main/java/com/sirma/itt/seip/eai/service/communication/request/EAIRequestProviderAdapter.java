/*
 * 
 */
package com.sirma.itt.seip.eai.service.communication.request;

import java.util.Collection;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.model.ServiceRequest;
import com.sirma.itt.seip.eai.service.communication.BaseEAIServices;
import com.sirma.itt.seip.eai.service.communication.EAIServiceIdentifier;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Adapter for building a specific and optimized request for a particular system. Each integrated system should have
 * such adapter with the system id registered to {@link #getName()}
 *
 * @author bbanchev
 */
public interface EAIRequestProviderAdapter extends Plugin, Named { // NOSONAR
	/** The PLUGIN_ID. */
	String PLUGIN_ID = "EAIRequestProviderAdapter";

	/**
	 * Each type of service generates different type of the service request.
	 * 
	 * @param service
	 *            is the service identifier to generate request for
	 * @param sourceArgument
	 *            is generic type of arguments used by different services. Example usage is {@link SearchArguments}
	 *            instance for {@link BaseEAIServices#SEARCH_QUERY} service, {@link Collection} of String for the
	 *            {@link BaseEAIServices#RETRIEVE}, etc.
	 * @return the generated ready to execute request
	 * @throws EAIException
	 *             during model generation of request or during read of sourceArgument
	 */
	<R extends ServiceRequest> R buildRequest(EAIServiceIdentifier service, Object sourceArgument) throws EAIException;

}
