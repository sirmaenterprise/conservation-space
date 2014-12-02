package com.sirma.itt.cmf.event.folder;

import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.emf.event.instance.BeforeInstancePersistEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when {@link FolderInstance} is persisted for the first time.
 * 
 * @author BBonev
 */
@SuppressWarnings("unchecked")
@Documentation("Event fired when {@link FolderInstance} is persisted for the first time.")
public class BeforeFolderPersistEvent extends
		BeforeInstancePersistEvent<FolderInstance, AfterFolderPersistEvent> {

	/**
	 * Instantiates a new before folder persist event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public BeforeFolderPersistEvent(FolderInstance instance) {
		super(instance);
	}

	@Override
	protected AfterFolderPersistEvent createNextEvent() {
		return new AfterFolderPersistEvent(getInstance());
	}

}
