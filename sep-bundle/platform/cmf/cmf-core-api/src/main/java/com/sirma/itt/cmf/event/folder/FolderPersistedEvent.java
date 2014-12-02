package com.sirma.itt.cmf.event.folder;

import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when {@link FolderInstance} has been persisted.
 * 
 * @author BBonev
 */
@Documentation("Event fired when {@link FolderInstance} has been persisted.")
public class FolderPersistedEvent extends InstancePersistedEvent<FolderInstance> {

	/**
	 * Instantiates a new folder persisted event.
	 * 
	 * @param instance
	 *            the instance
	 * @param old
	 *            the old
	 * @param operationId
	 *            the operation id
	 */
	public FolderPersistedEvent(FolderInstance instance, FolderInstance old, String operationId) {
		super(instance, old, operationId);
	}

}
