package com.sirma.sep.model.management.deploy.definition.steps;

import javax.inject.Inject;

import com.sirma.sep.model.management.definition.DefinitionModelAttributes;
import com.sirma.sep.model.management.definition.LabelProvider;

/**
 * Update step for {@link DefinitionModelAttributes#LABEL} {@link com.sirma.sep.model.management.ModelAttribute}.
 *
 * @author Mihail Radkov
 */
public class LabelsUpdateStep extends DefinitionNodeLabelsUpdateStep {

	@Inject
	public LabelsUpdateStep(LabelProvider labelProvider) {
		super(labelProvider);
	}

	@Override
	protected String getLabelAttributeName() {
		return DefinitionModelAttributes.LABEL;
	}

	@Override
	protected String getLabelAttributeKey() {
		return DefinitionModelAttributes.LABEL_ID;
	}
}
