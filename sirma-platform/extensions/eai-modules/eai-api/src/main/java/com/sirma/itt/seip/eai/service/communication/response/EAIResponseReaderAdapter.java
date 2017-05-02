package com.sirma.itt.seip.eai.service.communication.response;

import com.sirma.itt.seip.Named;
import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.eai.model.internal.ProcessedInstanceModel;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Adapter for parsing a specific response for a particular system. The {@link #getName()} is the registered external
 * system
 * 
 * @author bbanchev
 */
public interface EAIResponseReaderAdapter extends Plugin, Named { // NOSONAR
	/** The response reader extension id. */
	String PLUGIN_ID = "EAIResponseReaderAdapter";

	/**
	 * Each type of response generates potentially different type of the parsed response.
	 * 
	 * @param response
	 *            is the response wrapper
	 * @return the parsed response instance
	 * @throws EAIException
	 *             during model generation or response parsing
	 */
	<R extends ProcessedInstanceModel> R parseResponse(ResponseInfo response) throws EAIException;

}
