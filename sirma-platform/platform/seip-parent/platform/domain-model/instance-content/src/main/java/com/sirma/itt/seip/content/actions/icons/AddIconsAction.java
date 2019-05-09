package com.sirma.itt.seip.content.actions.icons;

import java.io.Serializable;
import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.seip.content.ContentResourceManagerService;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Add icons action executor.
 *
 * @author Nikolay Ch
 */
@Extension(target = Action.TARGET_NAME, enabled = true, order = 110)
public class AddIconsAction implements Action<AddIconsRequest> {

	@Inject
	private ContentResourceManagerService managerService;

	@Override
	public String getName() {
		return AddIconsRequest.OPERATION_NAME;
	}

	@Override
	public Object perform(AddIconsRequest request) {
		Serializable classId = request.getTargetId();
		Map<Serializable, String> contentMapping = request.getPurposeIconMapping();
		managerService.uploadContent(classId, contentMapping);
		return null;
	}
}
