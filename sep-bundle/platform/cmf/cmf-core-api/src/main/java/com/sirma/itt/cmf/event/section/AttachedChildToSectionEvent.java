package com.sirma.itt.cmf.event.section;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when new direct child is attached to the section instance.
 * 
 * @author BBonev
 */
@Documentation("Event fired when new direct child is attached to the section instance.")
public class AttachedChildToSectionEvent extends InstanceAttachedEvent<SectionInstance> {

	/**
	 * Instantiates a new attached child to section event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public AttachedChildToSectionEvent(SectionInstance target, Instance child) {
		super(target, child);
	}

}
