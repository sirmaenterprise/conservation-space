package com.sirma.itt.seip.eai.cms.service.communication;

import javax.inject.Singleton;

import com.sirma.itt.seip.eai.cms.configuration.CMSIntegrationConfigurationProvider;
import com.sirma.itt.seip.eai.cs.service.communication.CSClientCommunicationAdapter;
import com.sirma.itt.seip.eai.service.communication.EAICommunicationServiceAdapter;
import com.sirma.itt.seip.plugin.Extension;

/**
 * The CMS client adapter holding specific logic related to CMS requests.
 *
 * @author bbanchev
 */
@Singleton
@Extension(target = EAICommunicationServiceAdapter.PLUGIN_ID, order = 10)
public class CMSHttpClientCommunicationServiceAdapter extends CSClientCommunicationAdapter {

	@Override
	public String getName() {
		return CMSIntegrationConfigurationProvider.SYSTEM_ID;
	}

}
