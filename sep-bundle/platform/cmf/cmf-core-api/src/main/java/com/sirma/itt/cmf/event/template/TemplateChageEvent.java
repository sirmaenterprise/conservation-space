package com.sirma.itt.cmf.event.template;

import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.template.TemplateInstance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired on template instance change. The event is fired before instance persist every type on save.
 *
 * @author BBonev
 */
@Documentation("Event fired on template instance change. The event is fired before instance persist every type on save.")
public class TemplateChageEvent extends InstanceChangeEvent<TemplateInstance> {

	/**
	 * Instantiates a new template chage event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public TemplateChageEvent(TemplateInstance instance) {
		super(instance);
	}

}
