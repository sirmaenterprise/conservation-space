/**
 *
 */
package com.sirma.itt.seip.testutil.mocks;

import java.util.Objects;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.InitializedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Mock for {@link InstanceReference} that allow different initializations. The implementation will mimic the original
 * implementation in the most cases but it's not kryo serializable.
 *
 * @author BBonev
 */
public class InstanceReferenceMock implements InstanceReference {

	private static final long serialVersionUID = 363730152587158485L;

	/** The identifier. */
	private String identifier;

	/** The reference type. */
	private DataTypeDefinition referenceType;

	/** The instance. */
	private Instance instance;

	private InstanceType type;

	private InstanceReference parentReference;

	private InstanceReference root;

	/**
	 * Instantiates a new instance reference mock.
	 */
	public InstanceReferenceMock() {
		// nothing to do here
	}

	/**
	 * Instantiates a new instance reference mock.
	 *
	 * @param id
	 *            the id
	 * @param type
	 *            the type
	 */
	public InstanceReferenceMock(String id, Class<?> type) {
		identifier = id;
		referenceType = new DataTypeDefinitionMock(type, null);
	}

	/**
	 * Instantiates a new instance reference mock.
	 *
	 * @param id
	 *            the id
	 * @param type
	 *            the type
	 */
	public InstanceReferenceMock(Instance instance) {
		identifier = Objects.toString(instance.getId(), null);
		this.instance = instance;
		type = instance.type();
		referenceType = new DataTypeDefinitionMock(instance);
	}

	/**
	 * Instantiates a new instance reference mock.
	 *
	 * @param id
	 *            the id
	 * @param type
	 *            the type
	 */
	public InstanceReferenceMock(String id, DataTypeDefinition type) {
		identifier = id;
		referenceType = type;
	}

	/**
	 * Instantiates a new instance reference mock.
	 *
	 * @param id
	 *            the id
	 * @param type
	 *            the type
	 * @param instance
	 *            the instance
	 */
	public InstanceReferenceMock(String id, DataTypeDefinition type, Instance instance) {
		identifier = id;
		referenceType = type;
		this.instance = instance;
	}

	/**
	 * Instantiates a new instance reference mock.
	 *
	 * @param id
	 *            the id
	 * @param type
	 *            the type
	 * @param instance
	 *            the instance
	 * @param instanceType
	 *            the instance type
	 */
	public InstanceReferenceMock(String id, DataTypeDefinition type, Instance instance, InstanceType instanceType) {
		identifier = id;
		referenceType = type;
		this.instance = instance;
		this.type = instanceType;
	}

	/**
	 * Creates a generic instance reference for the given id. The returned reference will have a mock object for data
	 * type and the method {@link #toInstance()} will return an {@link EmfInstance} with set id and reference the
	 * returned reference. This way the methods {@link Instance#toReference()} and
	 * {@link InstanceReference#toInstance()} will work end return the same values
	 *
	 * @param id
	 *            the id to set for the instance and the reference
	 * @return the instance reference
	 */
	public static InstanceReferenceMock createGeneric(String id) {
		EmfInstance instance = new EmfInstance();
		ClassInstance type = new ClassInstance();
		type.setCategory("case");
		type.setId("emf:Case");
		instance.setType(type);
		instance.setId(id);
		InstanceReferenceMock reference = new InstanceReferenceMock(id,
				new DataTypeDefinitionMock(EmfInstance.class, null), instance);
		reference.setType(type);
		instance.setReference(reference);
		reference.setType(type);
		return reference;
	}

	/**
	 * Gets the identifier.
	 *
	 * @return the identifier
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Sets the identifier.
	 *
	 * @param identifier
	 *            the new identifier
	 */
	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Gets the reference type.
	 *
	 * @return the reference type
	 */
	@Override
	public DataTypeDefinition getReferenceType() {
		return referenceType;
	}

	/**
	 * Sets the reference type.
	 *
	 * @param referenceType
	 *            the new reference type
	 */
	@Override
	public void setReferenceType(DataTypeDefinition referenceType) {
		this.referenceType = referenceType;
	}

	@Override
	public InstanceReference getRoot() {
		if (this.root != null) {
			return this.root;
		}
		return InstanceReference.super.getRoot();
	}

	/**
	 * To instance.
	 *
	 * @return the instance
	 */
	@Override
	public Instance toInstance() {
		if (instance == null && StringUtils.isNotNullOrEmpty(getIdentifier()) && getReferenceType() != null) {
			// note if not found the first time will try to fetch it again every time when requested
			instance = TypeConverterUtil.getConverter().convert(InitializedInstance.class, this).getInstance();
		}
		return instance;
	}

	/**
	 * Sets the instance type.
	 *
	 * @param type
	 *            the new type
	 */
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
		final int prime = 31;
		int result = 1;
		result = prime * result + (identifier == null ? 0 : identifier.hashCode());
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
		return EqualsHelper.nullSafeEquals(identifier, other.getIdentifier());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InstanceReferenceMock [identifier=").append(identifier).append("]");
		return builder.toString();
	}

	@Override
	public InstanceReference getParent() {
		return parentReference;
	}

	@Override
	public void setParent(InstanceReference parentReference) {
		this.parentReference = parentReference;
	}

	/**
	 * Setter method for root.
	 *
	 * @param root
	 *            the root to set
	 */
	public void setRoot(InstanceReference root) {
		this.root = root;
	}
}
