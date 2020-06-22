package com.sirma.itt.seip.domain.instance;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Defines a interface that represents a reference to a concrete instance. The instance is represented with identifier
 * (it may not be the same identifier that is used in the instance) and a type definition.
 * <p>
 *
 * @author BBonev
 */
public interface InstanceReference extends Serializable, Entity<String> {

	/**
	 * Creates an {@link InstanceReference} using the provided id and type.<br>
	 *
	 * @deprecated use InstanceTypeResolver to fetch instance. This method might lead to wrong
	 *             {@link #getReferenceType()} initialization
	 * @param instanceId
	 *            the database id, as {@link #getId()}
	 * @param instanceType
	 *            the reference type as {@link #getReferenceType()}
	 * @return {@link Optional} of {@link InstanceReference} or {@link Optional#empty()} if instanceType is null or
	 *         empty. Might throw runtime exception if there is no such registered instanceType.
	 */
	@Deprecated
	static Optional<InstanceReference> create(Serializable instanceId, String instanceType) {
		String instanceIdValue = Objects.toString(instanceId, null);
		if (StringUtils.isNotBlank(instanceType)) {
			InstanceReference reference = TypeConverterUtil.getConverter().convert(InstanceReference.class,
					instanceType);
			reference.setId(instanceIdValue);
			return Optional.of(reference);
		} else if (StringUtils.isNotBlank(instanceIdValue)) {
			return Optional.of(new InstanceReferenceImpl(instanceIdValue, null));
		}
		return Optional.empty();
	}

	/**
	 * Gets the reference object type.
	 *
	 * @return the sourceType
	 */
	DataTypeDefinition getReferenceType();

	/**
	 * Set the reference object type.
	 *
	 * @param referenceType
	 *            the new reference type
	 */
	void setReferenceType(DataTypeDefinition referenceType);

	/**
	 * Gets the instance type or <code>null</code> if not loaded, yet. This should be the full semantic class name as
	 * string.
	 *
	 * @return the type
	 */
	default InstanceType getType() {
		return null;
	}

	/**
	 * Sets the instance type in the current reference.
	 *
	 * @param type
	 *            the new type
	 */
	default void setType(InstanceType type) {
		// nothing to do here
	}

	/**
	 * Converts the given instance reference to instance representation. The implementation could cache the created
	 * instance to performance. <br>
	 * The method could return <code>null</code> if the current reference is incomplete or the actual instance has been
	 * deleted.
	 * <p>
	 * <b>NOTE 1:</b> calling this method on large number of references could have performance penalty for loading
	 * instances one by one. For optimization use some of the utility methods for batch loading.
	 * <p>
	 * <b>NOTE 2: Chaining the method calls will result in new instance loading every time!</b> <br>
	 * The issue is displayed in the following code snippet: <br>
	 *
	 * <pre>
	 * <code>Instance instance1 = ...;
	 * InstanceReference reference = instance1.toReference();
	 * Instance instance2 = reference.toInstance();
	 * if (instance1 == instance2) {
	 *   &#47;&#47;equal - will never enter here
	 * } else {
	 *   &#47;&#47;not equal - always will enter here
	 * }
	 *  </code>
	 * </pre>
	 *
	 * <b>UPDATE:</b> The issue above is no longer valid if the initial reference is fetched using the
	 * {@link Instance#toReference()} method. The returned instance reference will hold the same instance that created
	 * the reference the first time.
	 *
	 * @return the instance represented by the current instance reference.
	 */
	Instance toInstance();

	/**
	 * Creates an empty instance that is initialized using it's default constructor and populated with id fetched from
	 * the current reference. If the {@link #getReferenceType()} returns <code>null</code> then the method will return
	 * <code>null</code>.
	 *
	 * @return the instance
	 */
	default Instance instantiate() {
		DataTypeDefinition type = getReferenceType();
		if (type == null || type.getJavaClass() == null) {
			return null;
		}
		Object newInstance = ReflectionUtils.newInstance(type.getJavaClass());
		if (newInstance instanceof Instance) {
			((Instance) newInstance).setId(getId());
			((Instance) newInstance).setType(getType());
			return (Instance) newInstance;
		}
		return null;
	}

	/**
	 * Instantiate the given instance reference if non <code>null</code>.
	 *
	 * @param reference
	 *            the reference to instantiate
	 * @return the instance or <code>null</code>
	 * @see #instantiate()
	 */
	static Instance instantiate(InstanceReference reference) {
		if (reference == null) {
			return null;
		}
		return reference.instantiate();
	}

	/**
	 * Checks if the given reference is valid. The method verifies that the given argument is non null and the methods
	 * {@link #getId()} and {@link #getReferenceType()} return non <code>null</code> objects.
	 *
	 * @param reference
	 *            the reference to check
	 * @return true, if is valid
	 */
	static boolean isValid(InstanceReference reference) {
		return reference != null && reference.getId() != null && reference.getReferenceType() != null;
	}
}
