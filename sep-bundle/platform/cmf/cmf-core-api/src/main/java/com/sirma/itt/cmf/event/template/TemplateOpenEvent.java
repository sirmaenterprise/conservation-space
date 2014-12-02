package com.sirma.itt.cmf.event.template;

import com.sirma.itt.emf.event.instance.InstanceOpenEvent;
import com.sirma.itt.emf.template.TemplateInstance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when template content has been loaded and is going to be returned to the user.
 * 
 * @author BBonev
 */
@Documentation("Event fired when template content has been loaded and is going to be returned to the user.")
public class TemplateOpenEvent extends InstanceOpenEvent<TemplateInstance> {

	/**
	 * Instantiates a new template open event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public TemplateOpenEvent(TemplateInstance instance) {
		super(instance);
	}

}
