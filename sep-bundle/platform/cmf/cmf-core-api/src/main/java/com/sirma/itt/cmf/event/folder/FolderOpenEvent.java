package com.sirma.itt.cmf.event.folder;

import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.emf.event.instance.InstanceOpenEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired on folder open operation
 * 
 * @author BBonev
 */
@Documentation("Event fired on folder open operation")
public class FolderOpenEvent extends InstanceOpenEvent<FolderInstance> {

	/**
	 * Instantiates a new folder open event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public FolderOpenEvent(FolderInstance instance) {
		super(instance);
	}

}
