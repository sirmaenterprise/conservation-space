package com.sirma.itt.seip.template.event;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.instance.event.InstancePersistedEvent;
import com.sirma.itt.seip.template.TemplateInstance;

/**
 * Event fired after every persist of a template instance.
 *
 * @author BBonev
 */
@Documentation("Event fired after every persist of a template instance.")
public class TemplatePersistedEvent extends InstancePersistedEvent<TemplateInstance> {

	/**
	 * Instantiates a new template persisted event.
	 *
	 * @param instance
	 *            the instance
	 * @param operationId
	 *            the operation id
	 */
	public TemplatePersistedEvent(TemplateInstance instance, String operationId) {
		super(instance, null, operationId);
	}

}
