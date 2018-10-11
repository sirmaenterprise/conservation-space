package com.sirma.itt.seip.eai.dam.service.communication.request;

import javax.inject.Singleton;

import com.sirma.itt.seip.eai.cs.service.communication.request.CSRequestProviderAdapter;
import com.sirma.itt.seip.eai.dam.configuration.DAMIntegrationConfigurationProvider;
import com.sirma.itt.seip.eai.service.communication.request.EAIRequestProviderAdapter;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Provides customized requests to be consumed by DAM communication adapter
 * 
 * @author bbanchev
 */
@Singleton
@Extension(target = EAIRequestProviderAdapter.PLUGIN_ID, order = 11)
public class DAMRequestProviderAdapter extends CSRequestProviderAdapter {

	@Override
	public String getName() {
		return DAMIntegrationConfigurationProvider.SYSTEM_ID;
	}

}
