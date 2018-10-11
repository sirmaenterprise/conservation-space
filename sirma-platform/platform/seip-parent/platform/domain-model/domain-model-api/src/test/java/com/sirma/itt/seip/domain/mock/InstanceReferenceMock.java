package com.sirma.itt.seip.domain.mock;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReferenceImpl;
import com.sirma.itt.seip.domain.instance.InstanceType;

/**
 * The Class InstanceReferenceMock.
 */
public class InstanceReferenceMock extends InstanceReferenceImpl {

	private static final long serialVersionUID = -2475591073949350144L;

	/**
	 * Instantiates a new reference mock.
	 *
	 * @param id
	 *            the identifier
	 */
	public InstanceReferenceMock(String id) {
		super(id, null);
	}

	/**
	 * Instantiates a new reference mock.
	 *
	 * @param id
	 *            the id
	 * @param referenceType
	 *            the reference type
	 */
	public InstanceReferenceMock(String id, DataTypeDefinition referenceType) {
		super(id, referenceType, null, null);
	}

	/**
	 * Instantiates a new reference mock using full constructor
	 *
	 * @param id
	 *            the identifier
	 * @param referenceType
	 *            the reference type
	 * @param type
	 *            the type
	 * @param emfInstance
	 *            the emf instance
	 */
	public InstanceReferenceMock(String id, DataTypeDefinition referenceType, InstanceType type, Instance emfInstance) {
		super(id, referenceType, type, emfInstance);
	}

}