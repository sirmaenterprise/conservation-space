package com.sirma.itt.cmf.event.cases;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when new direct child is attached to the case.
 * 
 * @author BBonev
 */
@Documentation("Event fired when new direct child is attached to the case.")
public class AttachedChildToCaseEvent extends InstanceAttachedEvent<CaseInstance> {

	/**
	 * Instantiates a new attached child to case event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public AttachedChildToCaseEvent(CaseInstance target, Instance child) {
		super(target, child);
	}

}
