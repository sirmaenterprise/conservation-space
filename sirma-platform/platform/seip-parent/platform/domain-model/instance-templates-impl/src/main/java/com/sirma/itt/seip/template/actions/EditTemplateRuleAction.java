package com.sirma.itt.seip.template.actions;

import static com.sirma.itt.seip.template.TemplateProperties.FOR_OBJECT_TYPE;
import static com.sirma.itt.seip.template.TemplateProperties.TEMPLATE_RULE;
import static com.sirma.itt.seip.template.TemplateProperties.TEMPLATE_RULE_DESCRIPTION;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.template.rules.TemplateRuleTranslator;

/**
 * Edits a rule of a template.
 *
 * @author Vilizar Tsonev
 */
@Extension(target = Action.TARGET_NAME, order = 346)
public class EditTemplateRuleAction implements Action<EditTemplateRuleActionRequest> {

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	TemplateRuleTranslator ruleTranslator;

	@Override
	public String getName() {
		return EditTemplateRuleActionRequest.OPERATION_NAME;
	}

	@Override
	public Object perform(EditTemplateRuleActionRequest request) {
		String instanceId = request.getTargetId().toString();
		String rule = request.getRule();

		Instance templateInstance = domainInstanceService.loadInstance(instanceId);
		templateInstance.add(TEMPLATE_RULE, StringUtils.trimToNull(rule));

		String templateRuleDescription = null;
		if (StringUtils.isNotBlank(rule)) {
			templateRuleDescription = ruleTranslator.translate(rule, templateInstance.getString(FOR_OBJECT_TYPE));
		}
		templateInstance.add(TEMPLATE_RULE_DESCRIPTION, templateRuleDescription);

		InstanceSaveContext saveContext = InstanceSaveContext.create(templateInstance,
				new Operation(ActionTypeConstants.EDIT_DETAILS));
		return domainInstanceService.save(saveContext).getId();
	}
}
