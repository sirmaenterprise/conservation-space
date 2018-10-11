package com.sirma.itt.seip.template.actions;

import javax.inject.Inject;

import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.template.TemplateService;

/**
 * Sets the given template as primary and demotes the existing active primary with the same rule/no rule.
 *
 * @author Vilizar Tsonev
 */
@Extension(target = Action.TARGET_NAME, order = 347)
public class SetTemplateAsPrimaryAction implements Action<SetTemplateAsPrimaryActionRequest> {

	@Inject
	private TemplateService templateService;

	@Override
	public String getName() {
		return SetTemplateAsPrimaryActionRequest.OPERATION_NAME;
	}

	@Override
	public Object perform(SetTemplateAsPrimaryActionRequest request) {
		String instanceId = request.getTargetId().toString();
		templateService.setAsPrimaryTemplate(instanceId);
		return instanceId;
	}
}
