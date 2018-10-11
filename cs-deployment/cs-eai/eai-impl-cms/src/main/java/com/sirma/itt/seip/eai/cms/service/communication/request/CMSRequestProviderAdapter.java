package com.sirma.itt.seip.eai.cms.service.communication.request;

import javax.inject.Singleton;

import com.sirma.itt.seip.eai.cms.configuration.CMSIntegrationConfigurationProvider;
import com.sirma.itt.seip.eai.cs.service.communication.request.CSRequestProviderAdapter;
import com.sirma.itt.seip.eai.service.communication.request.EAIRequestProviderAdapter;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provides customized requests to be consumed by CMS communication adapter
 * 
 * @author bbanchev
 */
@Singleton
@Extension(target = EAIRequestProviderAdapter.PLUGIN_ID, order = 10)
public class CMSRequestProviderAdapter extends CSRequestProviderAdapter {

	@Override
	public String getName() {
		return CMSIntegrationConfigurationProvider.SYSTEM_ID;
	}

}
