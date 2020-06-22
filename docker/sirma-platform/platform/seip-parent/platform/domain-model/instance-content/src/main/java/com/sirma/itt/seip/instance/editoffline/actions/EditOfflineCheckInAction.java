package com.sirma.itt.seip.instance.editoffline.actions;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.content.CheckOutCheckInService;
import com.sirma.itt.seip.instance.lock.LockInfo;
import com.sirma.itt.seip.instance.lock.LockService;
import com.sirma.itt.seip.instance.lock.exception.LockException;
import com.sirma.itt.seip.permissions.InstanceAccessEvaluator;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.exception.NoPermissionsException;
import com.sirma.sep.content.upload.UploadRequest;

/**
 * Executes the edit offline check in action. The {@link #perform(UploadRequest)} method streams back with which the
 * content of the given instance.
 *
 * @author T. Dossev
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 126)
public class EditOfflineCheckInAction implements Action<UploadRequest> {

	@Inject
	private InstanceAccessEvaluator instanceAccessEvaluator;

	@Inject
	private CheckOutCheckInService checkOutCheckInService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private LockService lockService;

	@Inject
	private LabelProvider labelProvider;

	@Override
	public String getName() {
		return UploadRequest.UPLOAD;
	}

	@Override
	// TODO Tony: bundle exception messages or use codes which could be interpreted by the web
	public void validate(UploadRequest request) {
		if (!instanceAccessEvaluator.canWrite(request.getTargetId())) {
			throw new NoPermissionsException(request.getTargetId(),
					"You don't have permissions to save the document in SEP.");
		}

		InstanceReference instanceReference = instanceTypeResolver.resolveReference(request.getTargetId()).orElseThrow(
				() -> new InstanceNotFoundException(labelProvider.getValue("document.checkin.deleted.instance")));

		LockInfo lockStatus = lockService.lockStatus(instanceReference);

		if (!lockStatus.isLocked()) {
			throw new LockException(lockStatus,
					"The document is unlocked in SEP. You cannot save the current version.");
		}

		if (!lockStatus.isLockedByMe()) {
			throw new LockException(lockStatus,
					"The document is locked by another user in SEP. You cannot save the current version.");
		}

		String instanceVersion = instanceReference.toInstance().getAsString(DefaultProperties.VERSION);
		if (!instanceVersion.equals(request.getInstanceVersion())) {
			throw new BadRequestException(
					"You are trying to save a different version of the document. You cannot save the current version.");
		}

		request.setTargetReference(instanceReference);
	}

	@Override
	public Object perform(UploadRequest uploadRequest) {
		return checkOutCheckInService.checkIn(uploadRequest, uploadRequest.getTargetId().toString());
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(UploadRequest request) {
		// conflicts with the action logic
		return false;
	}
}
