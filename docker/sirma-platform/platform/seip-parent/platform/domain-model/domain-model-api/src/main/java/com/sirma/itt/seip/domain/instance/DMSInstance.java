package com.sirma.itt.seip.domain.instance;

import com.sirma.itt.seip.domain.DmsAware;

/**
 * The Interface DMSInstance marks an instance that has a DMS representation. The interface forces the implementation to
 * support the {@link Instance} interface also.
 *
 * @author BBonev
 */
public interface DMSInstance extends Instance, DmsAware {
	// nothing to add
	/**
	 * Sets the dms id to the given target object only if the given object implements the {@link DMSInstance} interface.
	 *
	 * @param target
	 *            the target object to set the id to
	 * @param newId
	 *            the new id to set
	 */
	static void setDmsId(Object target, String newId) {
		if (target instanceof DMSInstance) {
			((DmsAware) target).setDmsId(newId);
		}
	}
}
