package com.sirma.itt.emf.instance.model;

import java.io.Serializable;

import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.properties.model.PropertyModel;

/**
 * Interface that combines the common interfaces implemented by the different instance objects.
 *
 * @author BBonev
 */
public interface Instance extends Serializable, PropertyModel, PathElement, Entity<Serializable> {

	/**
	 * Sets the revision.
	 *
	 * @param revision
	 *            the new revision
	 */
	void setRevision(Long revision);

	/**
	 * Gets a instance reference that represents the given instance. The method could return
	 * <code>null</code> if not supported. Note: if the {@link Instance#getId()} is
	 * <code>null</code> then the {@link InstanceReference#getIdentifier()} will be
	 * <code>null</code> also.
	 * <p>
	 * Multiple calls to the same method on the same instance will result in the same object to be
	 * returned. The method of the {@link InstanceReference#toInstance()} should return the initial
	 * instance only if the reference is created via the current method.
	 * 
	 * @return the instance reference
	 */
	InstanceReference toReference();
}
