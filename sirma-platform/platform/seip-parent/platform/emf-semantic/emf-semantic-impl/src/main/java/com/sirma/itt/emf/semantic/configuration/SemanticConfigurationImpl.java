package com.sirma.itt.emf.semantic.configuration;

import java.lang.invoke.MethodHandles;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.manager.RemoteRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.semantic.debug.DebugRepository;
import com.sirma.itt.emf.semantic.info.SemanticOperationLogger;
import com.sirma.itt.seip.DestroyObservable;
import com.sirma.itt.seip.Destroyable;
import com.sirma.itt.seip.configuration.ConfigurationException;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationGroupDefinition;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.GroupConverterContext;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.semantic.configuration.SemanticConfiguration;

/**
 * Semantic configurations implementation.
 * 
 * @author BBonev
 */
@Singleton
public class SemanticConfigurationImpl implements SemanticConfiguration, Destroyable {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	/** Semantic repository URL */
	@ConfigurationPropertyDefinition(sensitive = true, label = "URL of the semantic db server. I.e http://localhost:8080/graphdb-workbench")
	private static final String SEMANTIC_DB_URL = "semantic.db.url";

	@ConfigurationPropertyDefinition(sensitive = true, label = "Semantic repository name. Default to tenantName")
	private static final String REPOSITORY_NAME = "semantic.db.repository.name";

	/** Username for establishing connection to the repository */
	@ConfigurationPropertyDefinition(sensitive = true, label = "Username for establishing connection to the repository")
	private static final String SEMANTIC_DB_CONNECTION_USER_NAME = "semantic.db.connection.user.name";

	/** Password for establishing connection to the repository */
	@ConfigurationPropertyDefinition(sensitive = true, password = true, label = "Password for establishing connection to the repository")
	private static final String SEMANTIC_DB_CONNECTION_PASSWORD = "semantic.db.connection.password";

	/** Semantic repository maximum connections count */
	@ConfigurationPropertyDefinition(sensitive = true, type = Integer.class, defaultValue = "100", label = "Semantic connection pool maximum connections count")
	private static final String SEMANTIC_DB_MAX_CONNECTION_COUNT = "semantic.db.max.connections.count";

	/** Semantic repository read timeout */
	@ConfigurationPropertyDefinition(sensitive = true, type = Integer.class, defaultValue = "-1", label = "Defines the socket timeout (SO_TIMEOUT) in milliseconds,\n"
			+ " which is the timeout for waiting for data or, put differently,\n"
			+ " a maximum period inactivity between two consecutive data packets).\n"
			+ " A timeout value of zero is interpreted as an infinite timeout.\n"
			+ " A negative value is interpreted as undefined (system default)")
	private static final String SEMANTIC_SOCKET_TIMEOUT = "semantic.db.socketTimeout";

	/**
	 * Semantic Operation debug log enabled flag - activates or deactivates the log of all queries
	 * and operations that are executed on the semantic repository
	 */
	@ConfigurationPropertyDefinition(sensitive = true, type = Boolean.class, defaultValue = "false", label = "Semantic Operation debug log enabled flag - activates or deactivates the log of all queries and operations that are executed on the semantic repository.")
	private static final String SEMANTIC_OPERATION_DEBUG_LOG_ENABLED = "semantic.debug.log.enabled";

	/**
	 * Semantic Operation debug log flush on count - count of operations on which to flush the
	 * operations log
	 */
	@ConfigurationPropertyDefinition(sensitive = true, type = Integer.class, defaultValue = "10000", label = "Semantic Operation debug log flush on count - count of operations on which to flush the operations log")
	private static final String SEMANTIC_OPERATION_DEBUG_LOG_FLUSH_ON_COUNT = "semantic.debug.log.flush.count";

	/**
	 * Repository instance
	 */
	@Inject
	@Configuration
	@ConfigurationGroupDefinition(name = "semantic.repository", properties = { SEMANTIC_DB_URL,
			SEMANTIC_DB_CONNECTION_USER_NAME, SEMANTIC_DB_CONNECTION_PASSWORD, SEMANTIC_OPERATION_DEBUG_LOG_ENABLED,
			REPOSITORY_NAME, SEMANTIC_DB_MAX_CONNECTION_COUNT, SEMANTIC_OPERATION_DEBUG_LOG_FLUSH_ON_COUNT,
			SEMANTIC_SOCKET_TIMEOUT }, type = Repository.class, label = "Semantic repository")
	private ConfigurationProperty<Repository> repository;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "semantic.search.ftsearch.name", defaultValue = "solr-inst:ftsearch", label = "Name of the full text search index that will be used in the search. The name must be in form of a short URI like: luc:ftsearch. Depending on the preffix a diffenret search engine in the semantic server will be used. Available preffixes: luc:Lucene search and solr:Solr search")
	private ConfigurationProperty<String> ftsIndexName;

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "semantic.class.root", defaultValue = "ptop:Entity", label = "The root class for objects in the system.")
	private ConfigurationProperty<String> rootClassName;

	@Inject
	@Configuration(SEMANTIC_DB_URL)
	private ConfigurationProperty<String> semanticDbURL;

	@Inject
	@Configuration(REPOSITORY_NAME)
	private ConfigurationProperty<String> repositoryName;

	@PostConstruct
	void initialize() {
		repository.addValueDestroyListener(SemanticConfigurationImpl::shutdownRepository);
		DestroyObservable.addObserver(repository, (ConfigurationProperty<Repository> repo) -> shutdownRepository(repo.get()));
	}

	@PreDestroy
	void onShutdown() {
		destroy();
	}

	@Override
	public void destroy() {
		Destroyable.destroy(repository);
	}

	/**
	 * Builds the repository.
	 *
	 * @param context
	 *            the context
	 * @param tempFileProvider
	 *            the temp file provider
	 * @param securityContext
	 *            the security context
	 * @return the repository or <code>null</code> if there are missing configuration
	 */
	@ConfigurationConverter
	static Repository buildRepository(GroupConverterContext context, TempFileProvider tempFileProvider,
			SecurityContext securityContext) {
		try {
			ConfigurationProperty<String> repositoryAddress = context.getValue(SEMANTIC_DB_URL);
			if (repositoryAddress.isNotSet()) {
				LOGGER.warn("Semantic connection is disabled. No semantic is configured!");
				return null;
			}

			String userName = context.get(SEMANTIC_DB_CONNECTION_USER_NAME);
			String password = context.get(SEMANTIC_DB_CONNECTION_PASSWORD);

			ConfigurationProperty<String> repoNameConfig = context.getValue(REPOSITORY_NAME);
			String repositoryName = repoNameConfig.computeIfNotSet(securityContext::getCurrentTenantId);

			LOGGER.debug("Initialize a semantic repository access for {} @ {} ..", repositoryName,
					repositoryAddress.get());

			Integer maxConnections = context.get(SEMANTIC_DB_MAX_CONNECTION_COUNT);
			Integer soTimeout = context.get(SEMANTIC_SOCKET_TIMEOUT);

			Repository repository = buildAndInitRepo(repositoryAddress, repositoryName, userName, password,
					maxConnections, soTimeout);

			Boolean debug = context.get(SEMANTIC_OPERATION_DEBUG_LOG_ENABLED);
			Integer flushOnCount = context.get(SEMANTIC_OPERATION_DEBUG_LOG_FLUSH_ON_COUNT);
			if (debug) {
				repository = enableRepositoryDebugging(repository, tempFileProvider, flushOnCount);
			}
			return repository;
		} catch (Exception e) {
			throw new ConfigurationException(e);
		}
	}

	private static Repository buildAndInitRepo(ConfigurationProperty<String> repositoryAddress, String repositoryName,
			String userName, String password, Integer maxConnections, int soTimeout) {
		RemoteRepositoryManager manager = RemoteRepositoryManager.getInstance(repositoryAddress.get());
		if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)) {
			manager.setUsernameAndPassword(userName, password);
		}

		manager.setHttpClient(
				HttpClients.custom()
						.setMaxConnPerRoute(maxConnections)
						.setMaxConnTotal(maxConnections * 2)
						.setDefaultRequestConfig(RequestConfig.custom().setSocketTimeout(soTimeout).build())
						.build());
		manager.initialize();
		Repository repo = manager.getRepository(repositoryName);
		repo.initialize();
		return repo;
	}

	private static Repository enableRepositoryDebugging(Repository repository, TempFileProvider tempFileProvider,
			Integer flushOnCount) {
		LOGGER.debug("Enabling semantic debug mode!");
		SemanticOperationLogger.setIsEnabled(true);
		SemanticOperationLogger.setFlushCount(flushOnCount);
		SemanticOperationLogger.setTempDirectory(tempFileProvider.getTempDir().getAbsolutePath());
		return new DebugRepository(repository);
	}

	private static void shutdownRepository(Repository repository) {
		SemanticOperationLogger.shutdown();
		if (repository.isInitialized()) {
			try {
				repository.shutDown();
			} catch (RepositoryException e) {
				LOGGER.error("Failed to destroy repository connection due to: ", e);
			}
		}
	}

	@Override
	public ConfigurationProperty<Repository> getRepository() {
		return repository;
	}

	@Override
	public String getServerURLConfiguration() {
		return SEMANTIC_DB_URL;
	}

	@Override
	public String getRepositoryAccessUserNameConfiguration() {
		return SEMANTIC_DB_CONNECTION_USER_NAME;
	}

	@Override
	public String getRepositoryAccessUserPasswordConfiguration() {
		return SEMANTIC_DB_CONNECTION_PASSWORD;
	}

	@Override
	public String getRepositoryNameConfiguration() {
		return REPOSITORY_NAME;
	}

	@Override
	public ConfigurationProperty<String> getFtsIndexName() {
		return ftsIndexName;
	}

	@Override
	public ConfigurationProperty<String> getRootClassName() {
		return rootClassName;
	}

	@Override
	public ConfigurationProperty<String> getServerURL() {
		return semanticDbURL;
	}

	@Override
	public ConfigurationProperty<String> getRepositoryName() {
		return repositoryName;
	}
}
