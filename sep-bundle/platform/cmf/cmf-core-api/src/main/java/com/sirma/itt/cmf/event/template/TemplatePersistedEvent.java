package com.sirma.itt.cmf.event.template;

import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.template.TemplateInstance;
import com.sirma.itt.emf.util.Documentation;

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
