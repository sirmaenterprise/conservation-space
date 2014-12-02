package com.sirma.itt.cmf.event.folder;

import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstanceDeleteEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after deletion of a {@link FolderInstance}.
 * 
 * @author BBonev
 */
@Documentation("Event fired after deletion of a {@link FolderInstance}.")
public class AfterFolderDeleteEvent extends AfterInstanceDeleteEvent<FolderInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new after section delete event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterFolderDeleteEvent(FolderInstance instance) {
		super(instance);
	}

}
