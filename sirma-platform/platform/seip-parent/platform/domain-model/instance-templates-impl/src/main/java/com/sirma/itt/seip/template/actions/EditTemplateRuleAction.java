package com.sirma.itt.seip.template.actions;

import static com.sirma.itt.seip.template.TemplateProperties.FOR_OBJECT_TYPE;
import static com.sirma.itt.seip.template.TemplateProperties.TEMPLATE_RULE;
import static com.sirma.itt.seip.template.TemplateProperties.TEMPLATE_RULE_DESCRIPTION;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.Action;
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
	private TemplateRuleTranslator ruleTranslator;

	@Override
	public String getName() {
		return EditTemplateRuleActionRequest.OPERATION_NAME;
	}

	@Override
	public Object perform(EditTemplateRuleActionRequest request) {
		String rule = StringUtils.trimToNull(request.getRule());
		Instance templateInstance = request.getTargetReference().toInstance();
		templateInstance.add(TEMPLATE_RULE, rule);

		String templateRuleDescription = null;
		if (rule != null) {
			templateRuleDescription = ruleTranslator.translate(rule, templateInstance.getString(FOR_OBJECT_TYPE));
		}
		templateInstance.add(TEMPLATE_RULE_DESCRIPTION, templateRuleDescription);

		InstanceSaveContext saveContext = InstanceSaveContext.create(templateInstance, request.toOperation());
		return domainInstanceService.save(saveContext).getId();
	}
}
