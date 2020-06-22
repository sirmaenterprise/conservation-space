package com.sirma.sep.model.management.deploy.definition.steps;

import javax.inject.Inject;

import com.sirma.sep.model.management.definition.DefinitionModelAttributes;
import com.sirma.sep.model.management.definition.LabelProvider;

/**
 * Update step for {@link DefinitionModelAttributes#CONFIRMATION} {@link com.sirma.sep.model.management.ModelAttribute}.
 *
 * @author Boyan Tonchev.
 */
public class ConfirmationMessageUpdateStep extends DefinitionNodeLabelsUpdateStep {

	@Inject
	public ConfirmationMessageUpdateStep(LabelProvider labelProvider) {
		super(labelProvider);
	}

	@Override
	protected String getLabelAttributeName() {
		return DefinitionModelAttributes.CONFIRMATION;
	}

	@Override
	protected String getLabelAttributeKey() {
		return DefinitionModelAttributes.CONFIRMATION_ID;
	}
}
