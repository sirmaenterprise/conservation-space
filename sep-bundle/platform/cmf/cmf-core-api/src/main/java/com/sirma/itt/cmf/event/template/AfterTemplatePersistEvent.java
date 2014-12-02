package com.sirma.itt.cmf.event.template;

import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.template.TemplateInstance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after first persist of a template instance.
 * 
 * @author BBonev
 */
@Documentation("Event fired after first persist of a template instance.")
public class AfterTemplatePersistEvent extends
		AfterInstancePersistEvent<TemplateInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new after template persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterTemplatePersistEvent(TemplateInstance instance) {
		super(instance);
	}

}
