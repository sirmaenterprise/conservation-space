package com.sirma.itt.cmf.event.cases;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.event.instance.InstanceCreateEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after case instance creation but before method completion.
 * 
 * @author BBonev
 */
@Documentation("Event fired after case instance creation but before method completion.")
public class CaseCreateEvent extends InstanceCreateEvent<CaseInstance> implements OperationEvent {

	/**
	 * Instantiates a new case create event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public CaseCreateEvent(CaseInstance instance) {
		super(instance);
	}

	@Override
	public String getOperationId() {
		return ActionTypeConstants.CREATE_CASE;
	}

}
