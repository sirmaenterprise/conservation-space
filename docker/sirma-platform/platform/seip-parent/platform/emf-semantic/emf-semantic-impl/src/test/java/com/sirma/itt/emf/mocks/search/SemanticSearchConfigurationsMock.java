/**
 *
 */
package com.sirma.itt.emf.mocks.search;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.emf.semantic.SemanticSearchConfigurations;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Initializes the configurations for the search with the default values of the configurations
 *
 * @author kirq4e
 */
public class SemanticSearchConfigurationsMock extends SemanticSearchConfigurations {

	public SemanticSearchConfigurationsMock() {
		ReflectionUtils.setFieldValue(this, "listOfCaseInsensitiveProperties",
				new ConfigurationPropertyMock<>("dcterms:title"));
		ReflectionUtils.setFieldValue(this, "sortResultsInGdb", new ConfigurationPropertyMock<>(Boolean.TRUE));
		ReflectionUtils.setFieldValue(this, "processResultsInParallel", new ConfigurationPropertyMock<>(Boolean.FALSE));
		ReflectionUtils.setFieldValue(this, "forbiddenRoleUri", new ConfigurationPropertyMock<>("conc:SecurityRoleTypes-Forbidden"));
		ReflectionUtils.setFieldValue(this, "writeRoleUri", new ConfigurationPropertyMock<>("conc:SecurityRoleTypes-Read-Write "));
		ReflectionUtils.setFieldValue(this, "ignoreInstanceTypes", new ConfigurationPropertyMock<>(
				"sectioninstance, classinstance, commoninstance, topicinstance, commentinstance, user, group"));

	}

}
