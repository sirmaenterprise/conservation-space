package com.sirma.itt.semantic.configuration;

import com.sirma.itt.emf.configuration.Configuration;
import com.sirma.itt.emf.util.Documentation;

/**
 * Contains configuration properties related to the semantic database functionality.
 *
 * @author Adrian Mitev
 */
@Documentation("Configurations for the semantc functionality")
public interface SemanticConfigurationProperties extends Configuration {

	/** Semantic repository URL */
	@Documentation("URL of the semantic db server. I.e http://localhost:8080/openrdf-sesame")
	String SEMANTIC_DB_URL = "semantic.db.url";

	/** The system user display name. */
	@Documentation("Name of the semantic repository that will be used")
	String SEMANTIC_DB_REPOSITORY_NAME = "semantic.db.repository.name";

	/** Username for establishing connection to the repository */
	@Documentation("Username for establishing connection to the repository")
	String SEMANTIC_DB_CONNECTION_USER_NAME = "semantic.db.connection.user.name";

	/** Password for establishing connection to the repository */
	@Documentation("Password for establishing connection to the repository")
	String SEMANTIC_DB_CONNECTION_PASSWORD = "semantic.db.connection.password";

	/** Name of the context in the repository that will be used */
	@Documentation("Name of the context in the repository that will be used")
	String SEMANTIC_DB_CONTEXT_NAME = "semantic.db.context.name";

	/** Namespace registry re-initialization schedule setting [minutes]. */
	@Documentation("Namespace registry re-initialization schedule setting [minutes]. The namespace registry cache is to be re-initialized on every minute divisible by this setting value. <B>Default value is: 15</B> ")
	String NAMESPACE_REGISTRY_REINIT_PERIOD = "semantic.namespace.registry.reinitperiod";

	/** List of all persistent instance classes which properties are be stored in the semantic repository via {@link com.sirma.itt.emf.db.DbDao} service  */
	@Documentation("List of all persistent instance classes which properties are be stored in the semantic repository. <B>Default values are: caseinstance, documentinstance, projectinstance, sectioninstance, standalonetaskinstance, taskinstance, workflowinstancecontext</B>")
	String SEMANTIC_PERSISTENT_CLASSES = "semantic.persistent.instances";

	/** Name of the full text search index that will be used in the search. The name must be in form of a short URI like: luc:ftsearch. Depending on the preffix a diffenret search engine in the semantic server will be used. Available preffixes: luc:Lucene search and solr:Solr search */
	@Documentation("Name of the full text search index that will be used in the search. The name must be in form of a short URI like: luc:ftsearch. Depending on the preffix a diffenret search engine in the semantic server will be used. Available preffixes: luc:Lucene search and solr:Solr search")
	String FULL_TEXT_SEARCH_INDEX_NAME = "semantic.search.ftsearch.name";

	/** Semantic Operation debug log enabled flag - activates or deactivates the log of all queries and operations that are executed on the semantic repository */
	@Documentation("Semantic Operation debug log enabled flag - activates or deactivates the log of all queries and operations that are executed on the semantic repository.")
	String SEMANTIC_OPERATION_DEBUG_LOG_ENABLED = "semantic.debug.log.enabled";

	@Documentation("Comma separated list of prefix:name properties that are considered to be case insensitive during order")
	String SEARCH_CASE_INSENITIVE_ORDERBY_LIST = "semantic.search.case.ignored.properties";
}
