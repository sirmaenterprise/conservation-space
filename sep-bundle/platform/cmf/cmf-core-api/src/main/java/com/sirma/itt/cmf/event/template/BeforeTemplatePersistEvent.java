package com.sirma.itt.cmf.event.template;

import com.sirma.itt.emf.event.instance.BeforeInstancePersistEvent;
import com.sirma.itt.emf.template.TemplateInstance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before first persist of a template instance.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before first persist of a template instance.")
public class BeforeTemplatePersistEvent extends
		BeforeInstancePersistEvent<TemplateInstance, AfterTemplatePersistEvent> {

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
