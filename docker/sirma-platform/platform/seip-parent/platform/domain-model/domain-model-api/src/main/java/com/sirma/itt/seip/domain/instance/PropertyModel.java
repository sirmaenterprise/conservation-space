package com.sirma.itt.seip.domain.instance;

import com.sirma.itt.seip.Properties;
import com.sirma.itt.seip.domain.PathElement;

/**
 * The Interface PropertyModel. Specifies common access to object properties
 *
 * @author Borislav Bonev
 */
public interface PropertyModel extends Properties, PathElement {

	/**
	 * Gets the revision of the property model.
	 *
	 * @return the revision
	 */
	Long getRevision();
}
