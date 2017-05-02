package com.sirma.itt.cmf.alfresco4.content;

import java.io.Serializable;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.domain.instance.DMSInstance;
import com.sirma.itt.seip.instance.integration.InstanceDispatcher;
import com.sirma.itt.seip.plugin.Extension;

/**
 * {@link InstanceDispatcher} implementation that resolves current content to DMS for instances of type
 * {@link DMSInstance}.
 *
 * @author BBonev
 */
@Extension(target = InstanceDispatcher.TARGET_NAME, order = 50)
public class DmsInstanceDispatcher implements InstanceDispatcher {

	@Override
	public String getContentManagementSystem(Serializable instance, Content content) {
		if (instance instanceof DMSInstance) {
			return Alfresco4ContentStore.STORE_NAME;
		}
		return null;
	}

	@Override
	public String getViewManagementSystem(Serializable instance, Content content) {
		if (instance instanceof DMSInstance) {
			return Alfresco4ViewContentStore.VIEW_STORE_NAME;
		}
		return null;
	}

	@Override
	public String getDataSourceSystem(Serializable instance) {
		return null;
	}

}
