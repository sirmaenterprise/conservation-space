package com.sirma.itt.seip.domain.filter;

import java.util.Set;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.domain.Identity;

/**
 * Defines a filer definition
 *
 * @author BBonev
 */
public interface Filter extends Entity<Long>, Identity {

	/**
	 * Gets the filtering mode. Possible values are INCLUDE, EXCLUDE.<br>
	 * Default value: INCLUDE
	 *
	 * @return the mode
	 */
	String getMode();

	/**
	 * Filter values.
	 *
	 * @return the list
	 */
	Set<String> getFilterValues();

}
