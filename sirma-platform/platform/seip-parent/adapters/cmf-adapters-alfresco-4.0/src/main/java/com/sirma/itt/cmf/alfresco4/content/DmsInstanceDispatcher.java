package com.sirma.itt.cmf.alfresco4.content;

import java.io.Serializable;

import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.instance.integration.InstanceDispatcher;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;

/**
 * {@link InstanceDispatcher} implementation that resolves current content to DMS for instances of type
 * {@link DMSInstance}.
 *
 * @author BBonev
 */
@Extension(target = InstanceDispatcher.TARGET_NAME, order = 50)
public class DmsInstanceDispatcher implements InstanceDispatcher {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "content.store.alfresco4.enabled", defaultValue = "true", type = Boolean.class, sensitive = true, subSystem = "content", label = "Determines if instance primary content should go to Alfresco or to the Local content store")
	private ConfigurationProperty<Boolean> alfrescoStoreEnabled;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "content.store.alfresco4view.enabled", defaultValue = "true", type = Boolean.class, sensitive = true, subSystem = "content", label = "Determines if instance primary view should go to Alfresco or to the Local content store")
	private ConfigurationProperty<Boolean> alfrescoViewStoreEnabled;

	@Override
	public String getContentManagementSystem(Serializable instance, Content content) {
		if (instance instanceof DMSInstance && alfrescoStoreEnabled.get()) {
			return Alfresco4ContentStore.STORE_NAME;
		}
		return null;
	}

	@Override
	public String getViewManagementSystem(Serializable instance, Content content) {
		if (instance instanceof DMSInstance && alfrescoViewStoreEnabled.get()) {
			return Alfresco4ViewContentStore.VIEW_STORE_NAME;
		}
		return null;
	}

	@Override
	public String getDataSourceSystem(Serializable instance) {
		return null;
	}

}
