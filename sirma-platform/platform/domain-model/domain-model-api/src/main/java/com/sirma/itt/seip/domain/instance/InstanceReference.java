package com.sirma.itt.seip.domain.instance;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;

import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Defines a interface that represents a reference to a concrete instance. The instance is represented with identifier
 * (it may not be the same identifier that is used in the instance) and a type definition.
 * <p>
 *
 * @author BBonev
 */
public interface InstanceReference extends Identity, Serializable {

	/**
	 * Constant that should be used as parent on the last instance in the hierarchy to indicate that the hierarchy is
	 * resolved and there is no other instances above
	 */
	InstanceReference ROOT_REFERENCE = new RootReference();

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
	 * Gets the parent instance reference if any.
	 *
	 * @return the parent reference or null
	 */
	InstanceReference getParent();

	/**
	 * Sets the parent reference
	 *
	 * @param parentReference
	 *            the new parent
	 */
	void setParent(InstanceReference parentReference);

	/**
	 * Checks if the current instance is the last in the hierarchy.
	 * <p>
	 * The default implementation returns <code>false</code>.
	 *
	 * @return <code>true</code>, if is root and <code>false</code> if not root
	 */
	default boolean isRoot() {
		return false;
	}

	/**
	 * Gets the root instance in the hierarchy. If the current instance is root it will be returned. If the hierarchy is
	 * not resolved <code>null</code> will be returned
	 *
	 * @return the root or <code>null</code> if not resolved
	 */
	default InstanceReference getRoot() {
		if (getParent() == null || isRoot()) {
			// parent path not resolved
			return null;
		}
		InstanceReference parentRef = getParent();
		InstanceReference current = this;
		while (!parentRef.isRoot()) {
			current = parentRef;
			parentRef = parentRef.getParent();
		}
		return current;
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
			((Instance) newInstance).setId(getIdentifier());
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
	 * {@link #getIdentifier()} and {@link #getReferenceType()} return non <code>null</code> objects.
	 *
	 * @param reference
	 *            the reference to check
	 * @return true, if is valid
	 */
	static boolean isValid(InstanceReference reference) {
		return reference != null && reference.getIdentifier() != null && reference.getReferenceType() != null;
	}

	/**
	 * {@link InstanceReference} implementation that represent a reference used to represent the root
	 * {@link InstanceReference} for resolved instance hierarchy. This will be the top reference in the hierarchy.
	 *
	 * @author BBonev
	 */
	class RootReference implements InstanceReference {
		private static final long serialVersionUID = 4364898932356149469L;

		private RootReference() {
			// should not be instantiated more than once
		}

		@Override
		public String getIdentifier() {
			return "emf:rootReference";
		}

		@Override
		public void setIdentifier(String identifier) {
			// not supported
		}

		@Override
		public DataTypeDefinition getReferenceType() {
			return null;
		}

		@Override
		public void setReferenceType(DataTypeDefinition referenceType) {
			// not supported
		}

		@Override
		public InstanceReference getParent() {
			return null;
		}

		@Override
		public void setParent(InstanceReference parentReference) {
			// not supported
		}

		@Override
		public Instance toInstance() {
			return null;
		}

		@Override
		public int hashCode() {
			return 31 + getIdentifier().hashCode();
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
			return nullSafeEquals(getIdentifier(), other.getIdentifier());
		}

		@Override
		public InstanceReference getRoot() {
			return null;
		}

		@Override
		public boolean isRoot() {
			return true;
		}

		@Override
		public String toString() {
			return RootReference.class.getSimpleName();
		}
	}
}

