package com.sirma.cmf.web.instance;

import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.emf.instance.InstanceMoveAction;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.OwnedModel;

/**
 * Implementation of move operation for document instance.
 * 
 * @author svelikov
 */
public class DocumentMoveAction implements InstanceMoveAction {

	@Inject
	private DocumentService documentService;

	@Override
	public boolean move(Instance instanceToMove, Instance source, Instance target) {
		// 'resolve' the current document context - if the document is attached to other case
		if ((instanceToMove instanceof OwnedModel) && (source != null)) {
			((OwnedModel) instanceToMove).setOwningInstance(source);
		}
		boolean moved = documentService.moveDocument((DocumentInstance) instanceToMove,
				(SectionInstance) target);
		return moved;
	}

	@Override
	public boolean canHandle(Class<?> type) {
		return type.isAssignableFrom(DocumentInstance.class);
	}
}
