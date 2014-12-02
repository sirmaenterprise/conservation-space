package com.sirma.itt.cmf.event.cases;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when direct child is detached from the case.
 * 
 * @author BBonev
 */
@Documentation("Event fired when direct child is detached from the case.")
public class DetachedChildToCaseEvent extends InstanceDetachedEvent<CaseInstance> {

	/**
	 * Instantiates a new detached child to case event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public DetachedChildToCaseEvent(CaseInstance target, Instance child) {
		super(target, child);
	}

}
