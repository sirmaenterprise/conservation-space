package com.sirma.itt.seip.template.actions;

import javax.inject.Inject;

import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.template.TemplateService;

/**
 * Executes the template deactivate action.
 *
 * @author Vilizar Tsonev
 */
@Extension(target = Action.TARGET_NAME, order = 348)
public class DeactivateTemplateAction implements Action<DeactivateTemplateActionRequest> {

	@Inject
	private TemplateService templateService;

	@Override
	public String getName() {
		return DeactivateTemplateActionRequest.OPERATION_NAME;
	}

	@Override
	public Object perform(DeactivateTemplateActionRequest request) {
		String instanceId = request.getTargetId().toString();
		templateService.deactivate(instanceId);
		return instanceId;
	}
}
