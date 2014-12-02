package com.sirma.itt.cmf.event.folder;

import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.emf.event.instance.InstanceAttachedEvent;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when new direct child is attached to the folder instance.
 * 
 * @author BBonev
 */
@Documentation("Event fired when new direct child is attached to the folder instance.")
public class AttachedChildToFolderEvent extends InstanceAttachedEvent<FolderInstance> {

	/**
	 * Instantiates a new attached child to folder event.
	 * 
	 * @param target
	 *            the target
	 * @param child
	 *            the child
	 */
	public AttachedChildToFolderEvent(FolderInstance target, Instance child) {
		super(target, child);
	}

}
