package com.sirma.itt.seip.template.actions;

import javax.inject.Inject;

import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.template.TemplateService;

/**
 * Executes the template activation action.
 *
 * @author Vilizar Tsonev
 */
@Extension(target = Action.TARGET_NAME, order = 345)
public class ActivateTemplateAction implements Action<ActivateTemplateActionRequest> {

	@Inject
	private TemplateService templateService;

	@Override
	public String getName() {
		return ActivateTemplateActionRequest.OPERATION_NAME;
	}

	@Override
	public Object perform(ActivateTemplateActionRequest request) {
		String instanceId = request.getTargetId().toString();
		return templateService.activate(instanceId);
	}
}
