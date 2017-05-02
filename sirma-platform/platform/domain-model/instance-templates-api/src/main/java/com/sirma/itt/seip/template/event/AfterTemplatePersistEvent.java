package com.sirma.itt.seip.template.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.event.AbstractInstanceTwoPhaseEvent;
import com.sirma.itt.seip.event.TwoPhaseEvent;
import com.sirma.itt.seip.template.TemplateInstance;

/**
 * Event fired after first persist of a template instance.
 *
 * @author BBonev
 */
@Documentation("Event fired after first persist of a template instance.")
public class AfterTemplatePersistEvent extends AbstractInstanceTwoPhaseEvent<TemplateInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new after template persist event.
	 *
	 * @param instance
	 *            the instance
	 */
	public AfterTemplatePersistEvent(TemplateInstance instance) {
		super(instance);
	}

	@Override
	protected TwoPhaseEvent createNextEvent() {
		return null;
	}
}
