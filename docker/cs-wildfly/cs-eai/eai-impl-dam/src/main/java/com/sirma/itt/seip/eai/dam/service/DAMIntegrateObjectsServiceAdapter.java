package com.sirma.itt.seip.eai.dam.service;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.eai.cs.model.internal.CSResolvableInstance;
import com.sirma.itt.seip.eai.cs.service.CSIntegrateObjectsServiceAdapter;
import com.sirma.itt.seip.eai.dam.configuration.DAMIntegrationConfigurationProvider;
import com.sirma.itt.seip.eai.model.internal.ResolvableInstance;
import com.sirma.itt.seip.eai.service.IntegrateObjectsServiceAdapter;
import com.sirma.itt.seip.plugin.Extension;

/**
 * DAM adapter for {@link IntegrateObjectsServiceAdapter} with implementation of abstract methods based on
 * {@link CSIntegrateObjectsServiceAdapter} implementation
 *
 * @author bbanchev
 */
@ApplicationScoped
@Extension(target = IntegrateObjectsServiceAdapter.PLUGIN_ID, order = 11)
public class DAMIntegrateObjectsServiceAdapter extends CSIntegrateObjectsServiceAdapter {

	/**
	 * Check if the namespace matches {@value DAMIntegrationConfigurationProvider#NAMESPACE}
	 */
	@Override
	public boolean isResolveSupported(ResolvableInstance resovable) {
		if (super.isResolveSupported(resovable)) {
			CSResolvableInstance csResolvableInstance = (CSResolvableInstance) resovable;
			return csResolvableInstance.getNamespace() != null && csResolvableInstance
					.getNamespace()
						.equalsIgnoreCase(DAMIntegrationConfigurationProvider.NAMESPACE);
		}
		return false;
	}

	@Override
	public String getName() {
		return DAMIntegrationConfigurationProvider.SYSTEM_ID;
	}

}