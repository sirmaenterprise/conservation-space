package com.sirma.itt.seip.domain.definition;

import java.util.List;

/**
 * Defines a means of defining conditional rending of elements.
 *
 * @author BBonev
 */
public interface Conditional {

	/**
	 * Retrieves the list of conditions.
	 *
	 * @return the list of conditions
	 */
	List<Condition> getConditions();

}
