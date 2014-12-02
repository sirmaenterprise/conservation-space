package com.sirma.itt.cmf.event.section;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when direct child is detached from the section instance.
 * 
 * @author BBonev
 */
@Documentation("Event fired when direct child is detached from the section instance.")
public class DetachedChildToSectionEvent extends InstanceDetachedEvent<SectionInstance> {

	/**
	 * Instantiates a new detached child to section event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public DetachedChildToSectionEvent(SectionInstance target, Instance child) {
		super(target, child);
	}

}
