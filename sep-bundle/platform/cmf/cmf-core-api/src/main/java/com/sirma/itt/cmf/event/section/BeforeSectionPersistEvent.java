package com.sirma.itt.cmf.event.section;

import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.emf.event.instance.BeforeInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when {@link SectionInstance} is persisted for the first time.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired when {@link SectionInstance} is persisted for the first time.")
public class BeforeSectionPersistEvent extends
		BeforeInstancePersistEvent<SectionInstance, AfterSectionPersistEvent> {

	/**
	 * Instantiates a new before section persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeSectionPersistEvent(SectionInstance instance) {
		super(instance);
	}

	@Override
	protected AfterSectionPersistEvent createNextEvent() {
		return new AfterSectionPersistEvent(getInstance());
	}

}
