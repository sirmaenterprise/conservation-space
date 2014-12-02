package com.sirma.itt.cmf.event.folder;

import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.emf.event.instance.InstanceDetachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when direct child is detached from the folder instance.
 * 
 * @author BBonev
 */
@Documentation("Event fired when direct child is detached from the folder instance.")
public class DetachedChildToFolderEvent extends InstanceDetachedEvent<FolderInstance> {

	/**
	 * Instantiates a new detached child to folder event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public DetachedChildToFolderEvent(FolderInstance target, Instance child) {
		super(target, child);
	}

}
