package com.sirma.itt.seip.template.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.instance.event.InstanceOpenEvent;
import com.sirma.itt.seip.template.TemplateInstance;

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
