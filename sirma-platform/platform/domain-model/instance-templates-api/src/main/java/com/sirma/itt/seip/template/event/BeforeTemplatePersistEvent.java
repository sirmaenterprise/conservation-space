package com.sirma.itt.seip.template.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.seip.template.TemplateInstance;

/**
 * Event fired before first persist of a template instance.
 *
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before first persist of a template instance.")
public class BeforeTemplatePersistEvent
		extends AbstractInstanceTwoPhaseEvent<TemplateInstance, AfterTemplatePersistEvent> {

	/**
	 * Instantiates a new before template persist event.
	 *
	 * @param instance
	 *            the instance
	 */
	public BeforeTemplatePersistEvent(TemplateInstance instance) {
		super(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AfterTemplatePersistEvent createNextEvent() {
		return new AfterTemplatePersistEvent(getInstance());
	}

}
