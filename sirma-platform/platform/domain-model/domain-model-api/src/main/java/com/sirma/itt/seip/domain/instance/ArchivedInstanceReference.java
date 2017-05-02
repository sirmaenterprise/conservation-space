package com.sirma.itt.seip.domain.instance;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * {@link InstanceReference} implementation for archived instances. This reference contains the id of the archived
 * instance and the id of the original instance from which the archived instance was created.
 *
 * @author A. Kunchev
 */
public class ArchivedInstanceReference implements InstanceReference {

	private static final long serialVersionUID = 6252001242625064447L;

	private String idenfifier;

	private DataTypeDefinition referenceType;

	private Instance instance;

	private InstanceType type;

	private InstanceReference parent;

	/**
	 * Default constructor for this class.
	 *
	 * @param identifier
	 *            the id of the archived instance
	 * @param referenceType
	 *            the data type for the instance
	 */
	public ArchivedInstanceReference(String identifier, DataTypeDefinition referenceType) {
		this.idenfifier = identifier;
		this.referenceType = referenceType;
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
		this(identifier, referenceType);
		this.type = type;
	}

	@Override
	public Instance toInstance() {
		if (instance == null && StringUtils.isNotNullOrEmpty(getIdentifier()) && getReferenceType() != null) {
			instance = TypeConverterUtil.getConverter().convert(InitializedInstance.class, this).getInstance();
		}
		return instance;
	}

	@Override
	public String getIdentifier() {
		return idenfifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		idenfifier = identifier;
	}

	@Override
	public DataTypeDefinition getReferenceType() {
		return referenceType;
	}

	@Override
	public void setReferenceType(DataTypeDefinition referenceType) {
		this.referenceType = referenceType;
	}

	@Override
	public InstanceReference getParent() {
		return parent;
	}

	@Override
	public void setParent(InstanceReference parent) {
		this.parent = parent;
	}

	@Override
	public void setType(InstanceType type) {
		this.type = type;
	}

	@Override
	public InstanceType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		int result = 1;
		final int prime = 31;
		result = prime * result + (referenceType == null ? 0 : referenceType.hashCode());
		result = prime * result + (idenfifier == null ? 0 : idenfifier.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof InstanceReference)) {
			return false;
		}
		InstanceReference other = (InstanceReference) obj;
		if (!EqualsHelper.nullSafeEquals(idenfifier, other.getIdentifier())) {
			return false;
		}
		return EqualsHelper.nullSafeEquals(referenceType, other.getReferenceType());
	}

}
