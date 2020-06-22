package com.sirma.itt.seip.domain.instance;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;

/**
 * {@link InstanceReference} implementation for archived instances. This reference contains the id of the archived
 * instance and the id of the original instance from which the archived instance was created.
 *
 * @author A. Kunchev
 */
public class ArchivedInstanceReference extends InstanceReferenceImpl {

	private static final long serialVersionUID = 6252001242625064447L;

	/**
	 * Default constructor for this class.
	 *
	 * @param identifier
	 *            the id of the archived instance
	 * @param referenceType
	 *            the data type for the instance
	 */
	public ArchivedInstanceReference(String identifier, DataTypeDefinition referenceType) {
		super(identifier, referenceType);
	}

	/**
	 * Default constructor for this class.
	 *
	 * @param identifier
	 *            the id of the archived instance
	 * @param referenceType
	 *            the data type for the instance
	 * @param type
	 *            the type of the instance
	 */
	public ArchivedInstanceReference(String identifier, DataTypeDefinition referenceType, InstanceType type) {
		super(identifier, referenceType, type);
	}
}
