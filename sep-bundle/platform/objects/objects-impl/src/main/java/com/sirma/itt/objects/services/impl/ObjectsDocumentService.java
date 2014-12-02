package com.sirma.itt.objects.services.impl;

import javax.ejb.Stateless;
import javax.enterprise.inject.Specializes;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.services.impl.DocumentServiceImpl;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileAndPropertiesDescriptor;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.adapter.DMSInstanceAdapterService;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.objects.constants.ObjectProperties;
import com.sirma.itt.objects.domain.model.ObjectInstance;

/**
 * Overrides the document service to work for object views.
 *
 * @author BBonev
 */
@Stateless
@Specializes
public class ObjectsDocumentService extends DocumentServiceImpl {

	@Inject
	private DMSInstanceAdapterService genericAdapterService;

	/**
	 * Upload to dms.
	 *
	 * @param documentInstance
	 *            the document instance
	 * @param descriptor
	 *            the descriptor
	 * @return the map
	 * @throws DMSException
	 *             the dMS exception
	 */
	@Override
	protected FileAndPropertiesDescriptor uploadToDms(DocumentInstance documentInstance,
			FileDescriptor descriptor) throws DMSException {
		// FIXME: this here enters when the document is not object view but attached to the object
		// should add better type separation
		InstanceReference owningReference = documentInstance.getOwningReference();
		if (((owningReference != null) && (owningReference.getReferenceType() != null) && owningReference
				.getReferenceType().getJavaClass().equals(ObjectInstance.class))
				|| (documentInstance.getOwningInstance() instanceof ObjectInstance)) {
			return genericAdapterService.attachDocumentToInstance(documentInstance, descriptor,
					ObjectProperties.OBJECT_VIEW);
		}
		return super.uploadToDms(documentInstance, descriptor);
	}
}
