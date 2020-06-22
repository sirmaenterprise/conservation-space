package com.sirma.itt.seip.adapters.iiif;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;

import java.io.Serializable;

import javax.inject.Inject;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.integration.InstanceDispatcher;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.content.Content;

/**
 * Extension for {@link InstanceDispatcher} that forces images to be stored to a special image server.
 *
 * @author BBonev
 */
@Extension(target = InstanceDispatcher.TARGET_NAME, order = 10)
public class ImageContentDispatcher implements InstanceDispatcher {

	@Inject
	private ImageServerConfigurations imageServerConfigurations;
	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Override
	public String getContentManagementSystem(Serializable instance, Content content) {
		// if configuration is disabled does not route to the IIIF server
		if (!imageServerConfigurations.isImageServerEnabled().get().booleanValue()) {
			return null;
		}
		if (instance instanceof Instance && isSupportedInstanceType((Instance) instance, content)) {
			return IiifImageContentStore.STORE_NAME;
		}
		return null;
	}

	private static boolean isSupportedMimetype(Instance instance, Content content) {
		String mimeType = instance.getString(DefaultProperties.MIMETYPE, content.getMimeType());
		return mimeType != null && mimeType.contains("image");
	}

	private boolean isSupportedInstanceType(Instance instance, Content content) {
		String type = instance.getAsString(SEMANTIC_TYPE);
		if (type == null) {
			// if not type is defined we can try by mimetype instead
			return isSupportedMimetype(instance, content);
		}
		// return true if the current class belongs to image class hierarchy
		for (String parentType : semanticDefinitionService.getHierarchy(type)) {
			if (parentType.toLowerCase().contains("image")) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getViewManagementSystem(Serializable instance, Content content) {
		return null;
	}

	@Override
	public String getDataSourceSystem(Serializable instance) {
		return null;
	}

}
