/**
 *
 */
package com.sirma.itt.emf.mocks.search;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.semantic.SemanticSearchConfigurations;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Initializes the configurations for the search with the default values of the configurations
 *
 * @author kirq4e
 */
public class SemanticSearchConfigurationsMock extends SemanticSearchConfigurations {

	public SemanticSearchConfigurationsMock() {
		ReflectionUtils.setField(this, "listOfCaseInsensitiveProperties",
				new ConfigurationPropertyMock<>("dcterms:title"));
		ReflectionUtils.setField(this, "sortResultsInGdb", new ConfigurationPropertyMock<>(Boolean.TRUE));
		ReflectionUtils.setField(this, "processResultsInParallel", new ConfigurationPropertyMock<>(Boolean.FALSE));
		ReflectionUtils.setField(this, "forbiddenRoleUri", new ConfigurationPropertyMock<>("conc:SecurityRoleTypes-Forbidden"));
		ReflectionUtils.setField(this, "writeRoleUri", new ConfigurationPropertyMock<>("conc:SecurityRoleTypes-Read-Write "));
		ReflectionUtils.setField(this, "ignoreInstanceTypes", new ConfigurationPropertyMock<>(
				"sectioninstance, classinstance, commoninstance, topicinstance, commentinstance, user, group"));

	}

}
