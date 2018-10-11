package com.sirma.itt.seip.instance.editoffline.actions;

import java.io.File;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.definition.label.LabelProvider;
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
import com.sirma.sep.content.rest.ContentDownloadService;

/**
 * Executes the edit offline check out action. The {@link #perform(EditOfflineCheckOutRequest)} method streams back with
 * which the content of the given instance.
 *
 * @author T. Dossev
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 125)
public class EditOfflineCheckOutAction implements Action<EditOfflineCheckOutRequest> {

	@Inject
	private CheckOutCheckInService checkOutCheckInService;

	@Inject
	private InstanceAccessEvaluator accessEvaluator;

	@Inject
	private LockService lockService;

	@Inject
	private ContentDownloadService downloadService;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Override
	public String getName() {
		return EditOfflineCheckOutRequest.EDIT_OFFLINE_CHECK_OUT;
	}

	@Override
	public void validate(EditOfflineCheckOutRequest request) {
		if (!accessEvaluator.canWrite(request.getTargetId())) {
			throw new NoPermissionsException(request.getTargetId(), "No permission to access instance's content");
		}

		InstanceReference instanceReference = instanceTypeResolver.resolveReference(request.getTargetId()).orElseThrow(
				() -> new InstanceNotFoundException(labelProvider.getValue("document.checkin.deleted.instance")));

		if (!lockService.isAllowedToModify(instanceReference)) {
			LockInfo status = lockService.lockStatus(instanceReference);
			throw new LockException(status, "Instance already locked");
		}
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(EditOfflineCheckOutRequest request) {
		// conflicts with action logic
		return false;
	}

	@Override
	public Object perform(EditOfflineCheckOutRequest request) {
		File file = checkOutCheckInService.checkOut(request.getTargetId());
		downloadService.sendFile(file, request.getRange(), request.getForDownload(), request.getResponse(), null, null);
		return null;
	}

}
