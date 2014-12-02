package com.sirma.itt.cmf.event.folder;

import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.emf.event.TwoPhaseEvent;
import com.sirma.itt.emf.event.instance.AfterInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired after {@link FolderInstance} is persisted for the first time.
 * 
 * @author BBonev
 */
@Documentation("Event fired after {@link FolderInstance} is persisted for the first time.")
public class AfterFolderPersistEvent extends
		AfterInstancePersistEvent<FolderInstance, TwoPhaseEvent> {

	/**
	 * Instantiates a new before folder persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public AfterFolderPersistEvent(FolderInstance instance) {
		super(instance);
	}

}
