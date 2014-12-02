package com.sirma.itt.cmf.event.section;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.event.instance.BeforeInstanceDeleteEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before deletion of a {@link SectionInstance}.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before deletion of a {@link SectionInstance}.")
public class BeforeSectionDeleteEvent extends
		BeforeInstanceDeleteEvent<SectionInstance, AfterSectionDeleteEvent> {

	/**
	 * Instantiates a new before section delеte event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeSectionDeleteEvent(SectionInstance instance) {
		super(instance);
	}

	@Override
	protected AfterSectionDeleteEvent createNextEvent() {
		return new AfterSectionDeleteEvent(getInstance());
	}

}
