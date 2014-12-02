package com.sirma.itt.cmf.beans.model;

import java.io.Serializable;
import java.util.List;

import com.sirma.itt.emf.concurrent.UnitOfWork;

/**
 * Represents a single unit of documents to upload. The unit could contains one or more documents.
 * All documents in a unit are uploaded or none of them. Each unit has unique identifier to
 * distinguish it from other units.
 * 
 * @author BBonev
 */
public class UploadUnit extends UnitOfWork<DocumentInstance> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -6458621468607289023L;

	/**
	 * Instantiates a new upload unit.
	 * 
	 * @param unitId
	 *            the unit id
	 * @param instance
	 *            the instance
	 * @param documentInstances
	 *            the document instances
	 */
	public UploadUnit(Serializable unitId, DocumentInstance instance,
			DocumentInstance... documentInstances) {
		super(unitId, instance, documentInstances);
	}

	/**
	 * Instantiates a new upload unit.
	 * 
	 * @param unitId
	 *            the unit id
	 * @param unitData
	 *            the unit data
	 */
	public UploadUnit(Serializable unitId, List<DocumentInstance> unitData) {
		super(unitId, unitData);
	}
}
