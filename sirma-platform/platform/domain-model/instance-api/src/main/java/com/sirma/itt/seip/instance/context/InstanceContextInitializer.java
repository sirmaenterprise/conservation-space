package com.sirma.itt.seip.instance.context;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;

/**
 * Responsible for restoring of context hierarchy of a given instance or reference.
 *
 * @author svelikov
 * @author BBonev
 */
public interface InstanceContextInitializer {

	/**
	 * Restore hierarchy of the given instance. The hierarchy will be restored into the given instance.
	 *
	 * @param selectedInstance
	 *            the selected trough web link instance
	 */
	void restoreHierarchy(Instance selectedInstance);

	/**
	 * Restore hierarchy for the instance identified by the given reference.
	 *
	 * @param reference
	 *            that need to be updated after resolution
	 */
	void restoreHierarchy(InstanceReference reference);

}
