package com.sirma.itt.seip.instance.content;

import java.io.File;
import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.content.event.CheckOutEvent;
import com.sirma.itt.seip.instance.editoffline.exception.FileNotSupportedException;
import com.sirma.itt.seip.instance.editoffline.updaters.AbstractMSOfficeCustomPropertyUpdater;
import com.sirma.itt.seip.instance.editoffline.updaters.CustomPropertyUpdater;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.lock.exception.UnlockException;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.upload.ContentUploader;
import com.sirma.sep.content.upload.UploadRequest;

/**
 * Control the updates of ms office document contents.
 *
 * @author nvelkov
 * @author Vilizar Tsonev
 */
@ApplicationScoped
public class CheckOutCheckInServiceImpl implements CheckOutCheckInService {

	@Inject
	private InstanceContentService instanceContentService;

	@Inject
	private LockService lockService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private EventService eventService;

	@Inject
	private ContentUploader contentUploader;

	@Inject
	private AuthorityService authorityService;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private DomainInstanceService instanceService;

	@Inject
	@ExtensionPoint(AbstractMSOfficeCustomPropertyUpdater.PLUGIN_NAME)
	private Plugins<CustomPropertyUpdater> customPropertyUpdaters;

	@Override
	@Deprecated
	public ContentInfo checkIn(Content content, Serializable contentId) {
		ContentInfo info = instanceContentService.getContent(contentId, "");
		if (!info.exists()) {
			throw new EmfApplicationException(labelProvider.getValue("document.checkin.content.not.found"));
		}

		InstanceReference instanceReference = instanceTypeResolver.resolveReference(info.getInstanceId()).orElseThrow(
				() -> new EmfApplicationException(labelProvider.getValue("document.checkin.deleted.instance")));

		Instance instance = instanceReference.toInstance();
		unlockInstance(instanceReference);

		if (!authorityService.isActionAllowed(instance, ActionTypeConstants.UPLOAD_NEW_VERSION, null)) {
			throw new EmfApplicationException(labelProvider.getValue("document.checkin.no.permission"));
		}

		ContentInfo updatedContentInfo = contentUploader.updateContent(instance, content, (String) contentId);

		// save any changes made to the instance during the operation
		instanceService
		.save(InstanceSaveContext.create(instance, new Operation(ActionTypeConstants.UPLOAD_NEW_VERSION)));
		return updatedContentInfo;
	}

	@Override
	public ContentInfo checkIn(UploadRequest uploadRequest, String instanceId) {
		ContentInfo info = instanceContentService.getContent(instanceId, Content.PRIMARY_CONTENT);
		if (!info.exists()) {
			throw new InstanceNotFoundException(labelProvider.getValue("document.checkin.content.not.found"));
		}

		InstanceReference instanceReference = instanceTypeResolver.resolveReference(info.getInstanceId()).orElseThrow(
				() -> new InstanceNotFoundException(labelProvider.getValue("document.checkin.deleted.instance")));

		unlockInstance(instanceReference);

		ContentInfo updatedContentInfo = contentUploader.uploadForInstance(uploadRequest, instanceId,
				Content.PRIMARY_CONTENT, true);

		Instance instance = instanceService.loadInstance(instanceReference.getId());
		instance.add(DefaultProperties.PRIMARY_CONTENT_ID, updatedContentInfo.getContentId());
		instanceService
				.save(InstanceSaveContext.create(instance, new Operation(ActionTypeConstants.UPLOAD_NEW_VERSION)));
		return updatedContentInfo;
	}

	/**
	 * Unlocks the instance. If it is already unlocked, or has been locked by another user,
	 * {@link EmfApplicationException} or {@link UnlockException} is thrown and the operation is aborted.
	 *
	 * @param instanceReference
	 *            is the instance reference
	 */
	private void unlockInstance(InstanceReference instanceReference) {
		LockInfo lockInfo = lockService.lockStatus(instanceReference);
		// if the instance is unlocked, check-in should be aborted to avoid concurrency issues when modifying
		if (!lockInfo.isLocked()) {
			throw new EmfApplicationException(labelProvider.getValue("document.checkin.unlocked.instance"));
		}
		lockService.unlock(instanceReference);
		Instance instance = instanceReference.toInstance();
		// this is made in order to refresh the instance's lock status before checking isActionAllowed
		instance.remove(DefaultProperties.LOCKED_BY);
	}

	@Override
	public File checkOut(Serializable instanceId) {
		ContentInfo info = instanceContentService.getContent(instanceId, Content.PRIMARY_CONTENT);
		if (info.exists() && info.getInstanceId() != null) {
			InstanceReference instanceReference = instanceTypeResolver
					.resolveReference(info.getInstanceId())
					.orElseThrow(() -> new EmfApplicationException(
							labelProvider.getValue("document.checkin.deleted.instance")));

			eventService.fire(new CheckOutEvent(instanceReference.toInstance()));

			String mimeType = info.getMimeType();

			for (CustomPropertyUpdater updater : customPropertyUpdaters) {
				if (updater.canUpdate(mimeType)) {
					return updater.update(instanceId);
				}
			}
		}
		throw new FileNotSupportedException(info.getMimeType(), "The mime type is unsupported: " + info.getMimeType());
	}
}
