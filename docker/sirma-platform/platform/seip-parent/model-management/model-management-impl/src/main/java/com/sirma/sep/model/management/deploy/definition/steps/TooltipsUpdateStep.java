package com.sirma.sep.model.management.deploy.definition.steps;

import javax.inject.Inject;

import com.sirma.sep.model.management.definition.DefinitionModelAttributes;
import com.sirma.sep.model.management.definition.LabelProvider;

/**
 * Update step for {@link DefinitionModelAttributes#TOOLTIP} {@link com.sirma.sep.model.management.ModelAttribute}.
 *
 * @author Mihail Radkov
 */
public class TooltipsUpdateStep extends DefinitionNodeLabelsUpdateStep {

	@Inject
	public TooltipsUpdateStep(LabelProvider labelProvider) {
		super(labelProvider);
	}

	@Override
	protected String getLabelAttributeName() {
		return DefinitionModelAttributes.TOOLTIP;
	}

	@Override
	protected String getLabelAttributeKey() {
		return DefinitionModelAttributes.TOOLTIP_ID;
	}
}
