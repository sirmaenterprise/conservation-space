package com.sirma.itt.seip.eai.service.communication.response;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.eai.exception.EAIException;
import com.sirma.itt.seip.eai.exception.EAIRuntimeException;
import com.sirma.itt.seip.eai.model.communication.ResponseInfo;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;

/**
 * The EAIResponseReader service delegates the model transformation on the registered {@link EAIResponseReaderAdapter}.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class EAIResponseReader {

	@Inject
	@ExtensionPoint(value = EAIResponseReaderAdapter.PLUGIN_ID)
	private Plugins<EAIResponseReaderAdapter> responseReaderAdapters;

	/**
	 * Each type of response generates potentially different type of the parsed response. Actual parsing is delegated to
	 * {@link EAIResponseReaderAdapter} for the specific subsystem
	 * 
	 * @param <T>
	 *            controls the returned type.
	 * @param response
	 *            is the response wrapper
	 * @return the parsed response instance
	 * @throws EAIException
	 *             if adapter generate an error during parsing
	 */
	public <T> T parseResponse(ResponseInfo response) throws EAIException {
		EAIResponseReaderAdapter responseReader = responseReaderAdapters
				.get(response.getSystemId())
					.orElseThrow(() -> new EAIRuntimeException(
							"Not implemented " + EAIResponseReaderAdapter.class.getSimpleName() + " for system "
									+ response.getSystemId()));
		return responseReader.parseResponse(response);
	}

}
