package com.sirma.itt.cmf.event.cases;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.emf.event.AbstractInstanceEvent;
import com.sirma.itt.emf.event.OperationEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before case closing
 * 
 * @author BBonev
 */
@Documentation("Event fired before case closing")
public class CaseCloseEvent extends AbstractInstanceEvent<CaseInstance> implements OperationEvent {

	/**
	 * Instantiates a new case close event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public CaseCloseEvent(CaseInstance instance) {
		super(instance);
	}

	@Override
	public String getOperationId() {
		return ActionTypeConstants.COMPLETE;
	}

}
