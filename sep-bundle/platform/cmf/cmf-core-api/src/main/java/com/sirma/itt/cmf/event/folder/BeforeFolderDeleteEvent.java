package com.sirma.itt.cmf.event.folder;

import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.emf.event.instance.BeforeInstanceDeleteEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired before deletion of a {@link FolderInstance}.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired before deletion of a {@link FolderInstance}.")
public class BeforeFolderDeleteEvent extends
		BeforeInstanceDeleteEvent<FolderInstance, AfterFolderDeleteEvent> {

	/**
	 * Instantiates a new before section delеte event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeFolderDeleteEvent(FolderInstance instance) {
		super(instance);
	}

	@Override
	protected AfterFolderDeleteEvent createNextEvent() {
		return new AfterFolderDeleteEvent(getInstance());
	}

}
