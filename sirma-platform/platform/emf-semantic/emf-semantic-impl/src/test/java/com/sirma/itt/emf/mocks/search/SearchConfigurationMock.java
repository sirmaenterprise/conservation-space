/**
 *
 */
package com.sirma.itt.emf.mocks.search;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.search.SearchConfigurationImpl;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * @author kirq4e
 *
 */
public class SearchConfigurationMock extends SearchConfigurationImpl {

	public SearchConfigurationMock() {
		ReflectionUtils.setField(this, "searchResultMaxSize",
				new ConfigurationPropertyMock<>(1000));
		ReflectionUtils.setField(this, "pagerPageSize", new ConfigurationPropertyMock<>(25));
		ReflectionUtils.setField(this, "dashletPageSize", new ConfigurationPropertyMock<>(25));
	}
}
