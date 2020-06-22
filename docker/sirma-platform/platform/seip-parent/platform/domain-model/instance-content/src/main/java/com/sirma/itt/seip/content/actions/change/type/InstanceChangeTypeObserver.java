package com.sirma.itt.seip.content.actions.change.type;

import java.lang.invoke.MethodHandles;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.state.BeforeOperationExecutedEvent;
import com.sirma.itt.semantic.model.vocabulary.EMF;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentStore;
import com.sirma.sep.content.ContentStoreProvider;
import com.sirma.sep.content.InstanceContentService;

/**
 * Observer that listens for change type operation event in order to move the instance content if needed. <br>
 * The needs to move the content are as follows:
 * <ul>
 * <li>The instance type changed from non Image to Image type so we should move the file to image store to
 * enable advanced features like deep zoom and annotations, if applicable</li>
 * <li>The type is no loner an Image so we should remove it from the image store and store it in the default
 * content store</li>
 * </ul>
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/02/2019
 */
public class InstanceChangeTypeObserver {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private InstanceService instanceService;
	@Inject
	private InstanceContentService contentService;
	@Inject
	private ContentStoreProvider contentStoreProvider;

	void onInstanceTypeChange(@Observes BeforeOperationExecutedEvent event) {
		Instance updatedInstance = event.getInstance();
		if (!updatedInstance.isUploaded()) {
			// no content - nothing to do
			return;
		}
		InstanceType newType = updatedInstance.type();
		Instance currentInstance = instanceService.loadByDbId(updatedInstance.getId());
		if (currentInstance == null) {
			// during instance create
			return;
		}
		InstanceType currentType = currentInstance.type();
		if (newType.equals(currentType)) {
			// no type change nothing to do
			return;
		}
		if (newType.instanceOf(EMF.IMAGE) || currentType.instanceOf(EMF.IMAGE)) {
			// there are 2 possible scenarios:
			// 1. the instance type changed from non Image to Image type so we should move the file
			// to image store to enable advanced features like deep zoom and annotations, if applicable
			// 2. the type is no loner an image so we should remove it from the image store
			// and store it in the default content store
			moveToProperContentStore(updatedInstance);
		}
		// else not image related file, nothing to do
	}

	private void moveToProperContentStore(Instance instance) {
		ContentInfo contentInfo = contentService.getContent(instance.getId(), Content.PRIMARY_CONTENT);
		if (!contentInfo.exists()) {
			LOGGER.warn("Could not load the {} of an instance {} to be moved to proper store", Content.PRIMARY_CONTENT, instance.getId());
			// strange, could not find any content for the instance
			return;
		}
		Content content = Content.createFrom(contentInfo)
				.setContent(contentInfo)
				.setVersionable(true)
				.disableContentStoreEnforcingOnVersionUpdate();
		ContentStore newStore = contentStoreProvider.getStore(instance, content);
		if (contentInfo.getRemoteSourceName().equals(newStore.getName())) {
			// no store change, nothing to do
			return;
		}
		LOGGER.info("Going to move the instance {} content {} to store {}", instance.getId(),
				contentInfo.getContentId(), newStore.getName());
		contentService.saveContent(instance, content);
		// when moving content from image store -> local store/alfresco content preview should not exists
		// when moving content from local store -> image store, if such content exists it should be removed, as the
		// image store handles the previews directly without the need for external referring
		ContentInfo preview = contentService.getContent(instance.getId(), Content.PRIMARY_CONTENT_PREVIEW);
		if (preview.exists()) {
			contentService.deleteContent(instance.getId(), Content.PRIMARY_CONTENT_PREVIEW);
		}
	}
}
