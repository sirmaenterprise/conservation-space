package com.sirma.itt.cmf.event.cases;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before case deletion.
 * 
 * @author BBonev
 */
@Documentation("Event fired before case deletion.")
public class CaseDeleteEvent extends AbstractInstanceEvent<CaseInstance> implements OperationEvent {

	/**
	 * Instantiates a new case delete event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public CaseDeleteEvent(CaseInstance instance) {
		super(instance);
	}

	@Override
	public String getOperationId() {
		return ActionTypeConstants.DELETE;
	}

}
