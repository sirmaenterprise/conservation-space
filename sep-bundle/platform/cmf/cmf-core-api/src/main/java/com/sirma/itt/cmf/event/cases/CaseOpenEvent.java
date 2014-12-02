package com.sirma.itt.cmf.event.cases;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.event.instance.InstanceOpenEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired on case open operation.
 * 
 * @author BBonev
 */
@Documentation("Event fired on case open operation")
public class CaseOpenEvent extends InstanceOpenEvent<CaseInstance> {

	/**
	 * Instantiates a new case open event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public CaseOpenEvent(CaseInstance instance) {
		super(instance);
	}

}
