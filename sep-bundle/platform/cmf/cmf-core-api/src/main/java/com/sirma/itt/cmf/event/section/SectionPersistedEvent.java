package com.sirma.itt.cmf.event.section;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when {@link SectionInstance} has been persisted.
 * 
 * @author BBonev
 */
@Documentation("Event fired when {@link SectionInstance} has been persisted.")
public class SectionPersistedEvent extends InstancePersistedEvent<SectionInstance> {

	/**
	 * Instantiates a new section persisted event.
	 * 
	 * @param instance
	 *            the instance
	 * @param old
	 *            the old
	 * @param operationId
	 *            the operation id
	 */
	public SectionPersistedEvent(SectionInstance instance, SectionInstance old, String operationId) {
		super(instance, old, operationId);
	}

}
