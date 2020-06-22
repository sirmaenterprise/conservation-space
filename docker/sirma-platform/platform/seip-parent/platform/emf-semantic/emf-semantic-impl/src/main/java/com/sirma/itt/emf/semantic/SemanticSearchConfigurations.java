/**
 *
 */
package com.sirma.itt.emf.semantic;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Semantic search configuration options
 *
 * @author kirq4e
 */
@Singleton
public class SemanticSearchConfigurations {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "semantic.search.case.ignored.properties", defaultValue = "dcterms:title", label = "Comma separated list of prefix:name properties that are considered to be case insensitive during order")
	private ConfigurationProperty<String> listOfCaseInsensitiveProperties;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.sort.results.in.gdb", defaultValue = "false", type = Boolean.class, label = "Boolean flag that allows search results to be sorted by the semantic database. If this flag is false then the results are sorted by a logic in the SEIP Application")
	private ConfigurationProperty<Boolean> sortResultsInGdb;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "search.parallel.process.of.results", defaultValue = "false", type = Boolean.class, label = "Boolean flag that allows the processing of the query results to be executed in parallel")
	private ConfigurationProperty<Boolean> processResultsInParallel;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "semantic.search.ignore.instance.types", defaultValue = "sectioninstance, classinstance, commoninstance, topicinstance, commentinstance, annotation", label = "Comma separated list of instance types that to be ignored when executing search in the semantic repository")
	private ConfigurationProperty<String> ignoreInstanceTypes;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "semantic.search.forbidden.role.uri", defaultValue = "conc:SecurityRoleTypes-Forbidden", label = "URI of the Permission role Forbidden in the semantic repository", sensitive = true)
	private ConfigurationProperty<String> forbiddenRoleUri;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "semantic.search.write.role.uri", defaultValue = "conc:SecurityRoleTypes-Read-Write", label = "URI of the Permission role Read-Write in the semantic repository", sensitive = true)
	private ConfigurationProperty<String> writeRoleUri;

	/**
	 * @return the listOfCaseInsensitiveProperties
	 */
	public ConfigurationProperty<String> getListOfCaseInsensitiveProperties() {
		return listOfCaseInsensitiveProperties;
	}

	/**
	 * @return the sortResultsInDb
	 */
	public ConfigurationProperty<Boolean> getSortResultsInGdb() {
		return sortResultsInGdb;
	}

	/**
	 * @return the processResultsInParallel
	 */
	public ConfigurationProperty<Boolean> getProcessResultsInParallel() {
		return processResultsInParallel;
	}

	/**
	 * @return the ignoreInstanceTypes
	 */
	public ConfigurationProperty<String> getIgnoreInstanceTypes() {
		return ignoreInstanceTypes;
	}

	/**
	 * @return the forbiddenRoleUri
	 */
	public ConfigurationProperty<String> getForbiddenRoleUri() {
		return forbiddenRoleUri;
	}

	/**
	 * @return the writeRoleUri
	 */
	public ConfigurationProperty<String> getWriteRoleUri() {
		return writeRoleUri;
	}
}
