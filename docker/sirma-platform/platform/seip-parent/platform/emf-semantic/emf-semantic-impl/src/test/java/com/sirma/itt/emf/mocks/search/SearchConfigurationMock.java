/**
 *
 */
package com.sirma.itt.emf.mocks.search;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.seip.search.SearchConfigurationImpl;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * @author kirq4e
 *
 */
public class SearchConfigurationMock extends SearchConfigurationImpl {

	public SearchConfigurationMock() {
		ReflectionUtils.setFieldValue(this, "searchResultMaxSize",
				new ConfigurationPropertyMock<>(1000));
		ReflectionUtils.setFieldValue(this, "pagerPageSize", new ConfigurationPropertyMock<>(25));
		ReflectionUtils.setFieldValue(this, "dashletPageSize", new ConfigurationPropertyMock<>(25));
	}
}
