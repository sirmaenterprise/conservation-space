package com.sirma.itt.cmf.event.folder;

import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.emf.event.instance.InstanceChangeEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when folder or sub element has been modified and the folder need to be updated and
 * saved.
 * 
 * @author BBonev
 */
@Documentation("Event fired when folder or sub element has been modified and the folder need to be updated and saved.")
public class FolderChangeEvent extends InstanceChangeEvent<FolderInstance> {

	/**
	 * Instantiates a new folder change event.
	 * 
	 * @param instance
	 *            the instance
	 */
	public FolderChangeEvent(FolderInstance instance) {
		super(instance);
	}

}
