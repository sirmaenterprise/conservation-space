package com.sirma.itt.emf.sequence;

import com.sirma.itt.seip.domain.Identity;

/**
 * Defines an interface that represents a sequence object. The object should have at least a name and sequence value.
 *
 * @author BBonev
 */
public interface Sequence extends Identity {

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	Long getValue();
}
