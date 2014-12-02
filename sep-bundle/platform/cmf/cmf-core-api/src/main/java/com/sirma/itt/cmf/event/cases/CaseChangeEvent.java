package com.sirma.itt.cmf.event.cases;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when case or sub element of the case has been modified and the case need to be
 * updated and saved.
 * 
 * @author BBonev
 */
@Documentation("Event fired when case or sub element of the case has been modified and the case need to be updated and saved.")
public class CaseChangeEvent extends InstanceChangeEvent<CaseInstance> {

	/**
	 * Instantiates a new case change event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public CaseChangeEvent(CaseInstance instance) {
		super(instance);
	}

}
