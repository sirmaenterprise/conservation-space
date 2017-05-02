package com.sirma.itt.seip.content.extensions;

import javax.inject.Inject;

import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentAccessProvider;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.ContentService;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.Extension;

/**
 * {@link ContentAccessProvider} that integrates the new {@link InstanceContentService} into the old
 * {@link ContentService}. This is before all other providers in order to avoid errors in them for missing content.
 *
 * @author BBonev
 */
@Extension(target = ContentAccessProvider.TARGET_NAME, order = 5)
public class DefaultContentAccessProvider implements ContentAccessProvider {
	@Inject
	private InstanceContentService instanceContentService;

	@Override
	public String getContentURI(Instance instance) {
		if (instance != null && instance.getId() != null) {
			return "/share/content/" + instance.getId();
		}
		return null;
	}

	@Override
	public FileDescriptor getDescriptor(Instance instance) {

		ContentInfo primaryView = instanceContentService.getContent(instance, Content.PRIMARY_VIEW);
		// for UI2 instance if there is no view then the instance is not managed by the service
		if (!primaryView.exists()) {
			return null;
		}
		ContentInfo primaryContent = instanceContentService.getContent(instance, Content.PRIMARY_CONTENT);
		if (primaryContent.exists()) {
			return primaryContent;
		}
		return primaryView;
	}

}
