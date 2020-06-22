/**
 *
 */
package com.sirma.itt.emf.mocks.search;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.emf.semantic.configuration.SemanticConfigurationImpl;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * @author BBonev
 */
public class SemanticConfigurationMock extends SemanticConfigurationImpl {

	public SemanticConfigurationMock() {
		ReflectionUtils.setFieldValue(this, "ftsIndexName", new ConfigurationPropertyMock<>("solr:ftsearch"));
	}
}
