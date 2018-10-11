package com.sirma.itt.seip.search;

import com.sirma.itt.seip.Resettable;

/**
 * Provider for {@link AdvancedSearchConfiguration}.
 *
 * @author Mihail Radkov
 */
public interface AdvancedSearchConfigurationProvider extends Resettable {

	/**
	 * Provides the advanced search configuration object.
	 *
	 * @return the configuration object
	 */
	AdvancedSearchConfiguration getConfiguration();
}
