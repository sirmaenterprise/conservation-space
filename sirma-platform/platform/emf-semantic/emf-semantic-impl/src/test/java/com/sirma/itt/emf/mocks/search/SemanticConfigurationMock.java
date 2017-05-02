/**
 *
 */
package com.sirma.itt.emf.mocks.search;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.semantic.configuration.SemanticConfigurationImpl;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * @author BBonev
 */
public class SemanticConfigurationMock extends SemanticConfigurationImpl {

	public SemanticConfigurationMock() {
		ReflectionUtils.setField(this, "ftsIndexName", new ConfigurationPropertyMock<>("solr:ftsearch"));
	}
}
