package com.sirma.itt.cmf.event.cases;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when {@link CaseInstance} has been persisted.
 * 
 * @author BBonev
 */
@Documentation("Event fired when {@link CaseInstance} has been persisted.")
public class CasePersistedEvent extends InstancePersistedEvent<CaseInstance> {

	/**
	 * Instantiates a new case persisted event.
	 * 
	 * @param instance
	 *            the instance
	 * @param old
	 *            the old
	 * @param operationId
	 *            the operation id
	 */
	public CasePersistedEvent(CaseInstance instance, CaseInstance old, String operationId) {
		super(instance, old, operationId);
	}

}
