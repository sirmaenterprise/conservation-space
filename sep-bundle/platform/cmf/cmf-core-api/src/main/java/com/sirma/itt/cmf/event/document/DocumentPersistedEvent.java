package com.sirma.itt.cmf.event.document;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.emf.event.instance.InstancePersistedEvent;
import com.sirma.itt.emf.util.Documentation;

/**
 * Event fired when {@link DocumentInstance} has been persisted.
 * 
 * @author BBonev
 */
@Documentation("Event fired when {@link DocumentInstance} has been persisted.")
public class DocumentPersistedEvent extends InstancePersistedEvent<DocumentInstance> {

	/**
	 * Instantiates a new document persisted event.
	 * 
	 * @param instance
	 *            the instance
	 * @param old
	 *            the old
	 * @param operationId
	 *            the operation id
	 */
	public DocumentPersistedEvent(DocumentInstance instance, DocumentInstance old,
			String operationId) {
		super(instance, old, operationId);
	}

}
