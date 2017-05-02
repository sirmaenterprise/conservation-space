package com.sirma.itt.seip.domain.instance;

/**
 * Model interface to mark a node that it has a parent that owns him.
 *
 * @author BBonev
 */
public interface OwnedModel {

	/**
	 * Gets the owning/parent reference.
	 *
	 * @return the owning reference
	 */
	default InstanceReference getOwningReference() {
		return null;
	}

	/**
	 * Sets the owning reference.
	 *
	 * @param reference
	 *            the new owning reference
	 */
	default void setOwningReference(InstanceReference reference) {
		// nothing to do
	}

	/**
	 * Gets the owning instance. The method should return cached fully loaded instance that is represented by the
	 * {@link InstanceReference} returned by {@link #getOwningReference()}
	 *
	 * @return the owning instance or <code>null</code> if the {@link #getOwningReference()} returns <code>null</code>.
	 */
	default Instance getOwningInstance() {
		return null;
	}

	/**
	 * Sets the owning instance for the current model.
	 *
	 * @param instance
	 *            the new owning instance
	 */
	default void setOwningInstance(Instance instance) {
		// nothing to do
	}

	/**
	 * Sets the owned model fields using the methods {@link #setOwningInstance(Object, Instance)} and
	 * {@link #setOwningReference(Object, InstanceReference)}
	 * <p>
	 * The method is short for
	 *
	 * <pre>
	 * <code>
	 * OwnedModel.setOwningInstance(target, instanceToSet);
	 * OwnedModel.setOwningReference(target, instanceToSet.toReference());
	 * </code>
	 * </pre>
	 *
	 * @param <T>
	 *            the target type
	 * @param target
	 *            the target object to be tested if implements {@link OwnedModel}
	 * @param instanceToSet
	 *            the owning instance as owning instance to the target.
	 * @return the first argument of the method
	 */
	static <T> T setOwnedModel(T target, Instance instanceToSet) {
		setOwningInstance(target, instanceToSet);
		if (instanceToSet != null) {
			setOwningReference(target, instanceToSet.toReference());
		}
		return target;
	}

	/**
	 * Sets the given owning instance to the given target if target is of type {@link OwnedModel}, otherwise the method
	 * just returns it's first argument.
	 *
	 * @param <T>
	 *            the target type
	 * @param target
	 *            the target object to be tested if implements {@link OwnedModel}
	 * @param instanceToSet
	 *            the owning instance as owning instance to the target via the {@link #setOwningInstance(Instance)}
	 * @return the first argument of the method
	 */
	static <T> T setOwningInstance(T target, Instance instanceToSet) {
		if (target instanceof OwnedModel) {
			((OwnedModel) target).setOwningInstance(instanceToSet);
		}
		return target;
	}

	/**
	 * Sets the given owning reference to the given target if target is of type {@link OwnedModel}, otherwise the method
	 * just returns it's first argument.
	 *
	 * @param <T>
	 *            the target type
	 * @param target
	 *            the target object to be tested if implements {@link OwnedModel}
	 * @param referenceToSet
	 *            the reference to set as owning instance to the target via the
	 *            {@link #setOwningReference(InstanceReference)}
	 * @return the first argument of the method
	 */
	static <T> T setOwningReference(T target, InstanceReference referenceToSet) {
		if (target instanceof OwnedModel) {
			((OwnedModel) target).setOwningReference(referenceToSet);
		}
		return target;
	}
}
