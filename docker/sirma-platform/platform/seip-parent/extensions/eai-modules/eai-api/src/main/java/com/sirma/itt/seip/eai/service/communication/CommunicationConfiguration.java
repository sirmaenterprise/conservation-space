package com.sirma.itt.seip.eai.service.communication;

import com.sirma.itt.seip.eai.model.SealedModel;

/**
 * The CommunicationConfiguration holds system+tenant specific communication configurations. The set of supported
 * services are stored and uri to the desired service could be obtained by
 * {@link #getRequestServiceURI(EAIServiceIdentifier, String)}
 *
 * @author bbanchev
 */
public class CommunicationConfiguration extends SealedModel {
	// simple configuration
}
