package com.sirma.itt.emf.semantic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.definition.event.LoadSemanticDefinitions;
import com.sirma.itt.emf.domain.model.Uri;
import com.sirma.itt.emf.scheduler.SchedulerActionAdapter;
import com.sirma.itt.emf.scheduler.SchedulerConfiguration;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.scheduler.SchedulerEntryType;
import com.sirma.itt.emf.scheduler.SchedulerService;
import com.sirma.itt.semantic.ConnectionFactory;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.configuration.SemanticConfigurationProperties;

/**
 * <p>
 * Namespace registry service implementation holding a cache of all defined namespaces and their
 * prefixes in the underlying semantic repository
 * </p>
 * <p>
 * The cache is eagerly loaded during application initialization. Subsequent updates of the
 * namespace cache are defined by the configuration setting
 * {@link com.sirma.itt.semantic.configuration.SemanticSyncConfigurationProperties#NAMESPACE_REGISTRY_REINIT_PERIOD}
 * </p>
 * <p>
 * Depends on PatchDbService, because the semantic repository has to be initialized for the registry
 * to work properly.
 * </p>
 * 
 * @see {@link com.sirma.itt.semantic.NamespaceRegistryService}
 * @author Valeri Tishev
 * @author BBonev
 */
@ApplicationScoped
@Named("namespaceRegistry")
public class NamespaceRegistry extends SchedulerActionAdapter implements NamespaceRegistryService {

	private static final int MAPPING_CACHE_SIZE = 1024;
	private static final Logger LOGGER = LoggerFactory.getLogger(NamespaceRegistry.class);
	private static final int SHORT_URI_LENGHT = 40;

	@Inject
	private ConnectionFactory connectionFactory;

	private AtomicReference<Map<String, String>> namespaces;
	private AtomicReference<Map<String, String>> prefixes;
	private AtomicReference<String> namespacePrefixes;

	/**
	 * Temporary cache for storing properties mapping for full URI to short one with maximum size of
	 * 1024 elements
	 */
	private Map<String, String> propFull2Short;
	/**
	 * Temporary cache for storing properties mapping for short URI to full one with maximum size of
	 * 1024 elements
	 */
	private Map<String, String> propShort2Full;

	@Inject
	@Config(name = SemanticConfigurationProperties.NAMESPACE_REGISTRY_REINIT_PERIOD, defaultValue = "0 0/15 * ? * *")
	private String namespaceReinitPeriod;

	/** Pattern that is used to split URIs by the short and/or full delimiter */
	private static final Pattern URI_SPLIT_PATTERN = Pattern.compile(SHORT_URI_DELIMITER + "|"
			+ FULL_URI_DELITIMER);

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	/** The scheduler service. */
	@Inject
	private SchedulerService schedulerService;
	/** The value factory. */
	@Inject
	private ValueFactory valueFactory;
	/** The context name. */
	@Inject
	@Config(name = SemanticConfigurationProperties.SEMANTIC_DB_CONTEXT_NAME, defaultValue = "http://ittruse.ittbg.com/data/enterpriseManagementFramework")
	private String contextName;
	/** The context. */
	private URI context;

	/**
	 * Initializes and schedules the re-initialization of the namespace registry cache
	 */
	@PostConstruct
	protected void initAndSchedule() {
		propFull2Short = new HashMap<String, String>(MAPPING_CACHE_SIZE);
		propShort2Full = new HashMap<String, String>(MAPPING_CACHE_SIZE);

		initNamespaces();

		SchedulerConfiguration configuration = schedulerService
				.buildEmptyConfiguration(SchedulerEntryType.CRON);
		configuration.setIdentifier("REFRESH_NAMESPACE_REGISTRY");
		configuration.setCronExpression(namespaceReinitPeriod);
		// execute in 15 seconds
		schedulerService.schedule(NamespaceRegistry.class, configuration);
	}

	/**
	 * The method uses {@link org.openrdf.repository.RepositoryConnection} in order to get all
	 * declared namespaces in the underlying semantic repository and cache them
	 */
	private void initNamespaces() {
		// ensure caches not to become a memory leak by clearing them if they become too big
		if (propShort2Full.size() > MAPPING_CACHE_SIZE) {
			propShort2Full.clear();
		}
		if (propFull2Short.size() > MAPPING_CACHE_SIZE) {
			propFull2Short.clear();
		}

		if (namespaces == null) {
			namespaces = new AtomicReference<>();
		}
		if (prefixes == null) {
			prefixes = new AtomicReference<>();
		}
		if (namespacePrefixes == null) {
			namespacePrefixes = new AtomicReference<>();
		}
		try {
			RepositoryConnection connection = connectionFactory.produceConnection();
			RepositoryResult<Namespace> currentNamespaces = connection.getNamespaces();


			Map<String, String> updatedNamespaces = new HashMap<>();
			Map<String, String> updatedPrefixes = new HashMap<>();
			StringBuilder updatedNamespacePrefixes = new StringBuilder();
			while (currentNamespaces.hasNext()) {
				Namespace namespace = currentNamespaces.next();
				updatedNamespaces.put(namespace.getPrefix(), namespace.getName());
				if (updatedPrefixes.containsKey(namespace.getName())) {
					LOGGER.warn(
							"\n===========================================\n    Found namespace with multiple prefixes: namespace={} {} and {} "
									+ "\n===========================================",
							namespace.getName(), namespace.getPrefix(),
							updatedPrefixes.get(namespace.getName()));
					continue;
				}
				updatedPrefixes.put(namespace.getName(), namespace.getPrefix());

				updatedNamespacePrefixes.append("PREFIX ").append(namespace.getPrefix())
						.append(SHORT_URI_DELIMITER).append("<").append(namespace.getName())
						.append(">").append("\n");

			}

			currentNamespaces.close();
			connectionFactory.disposeConnection(connection);

			// lock for write while update is executing
			lock.writeLock().lock();
			try {
				namespaces.set(updatedNamespaces);
				prefixes.set(updatedPrefixes);
				namespacePrefixes.set(updatedNamespacePrefixes.toString());
			} finally {
				lock.writeLock().unlock();
			}
		} catch (RepositoryException e) {
			LOGGER.error("Failed initializing namespace registry", e);
		}
	}

	@Override
	public void execute(SchedulerContext ctx) throws Exception {
		initNamespaces();
		LOGGER.debug("Finished re-initialization of namespace registry");
		LOGGER.debug("Cache statistic: short to full URI {}, full to short URI {}",
				propShort2Full.size(), propFull2Short.size());
	}

	/**
	 * Gets the namespaces holder map.
	 *
	 * @return the namespaces holder map
	 */
	private Map<String, String> getNamespacesHolderMap() {
		lock.readLock().lock();
		try {
			Map<String, String> map = namespaces.get();
			if (map == null) {
				LOGGER.error("The namespace registry has not been initialized so far.");
				return Collections.emptyMap();
			}
			return map;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Gets the prefixes holder map.
	 *
	 * @return the prefixes holder map
	 */
	private Map<String, String> getPrefixesHolderMap() {
		lock.readLock().lock();
		try {
			Map<String, String> map = prefixes.get();
			if (map == null) {
				LOGGER.error("The prefixes registry has not been initialized so far.");
				return Collections.emptyMap();
			}
			return map;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public String getNamespace(String prefix) {
		return getNamespacesHolderMap().get(prefix);
	}

	@Override
	public String buildFullUri(String shortUri) {
		return buildFullUriInternal(shortUri);
	}

	/**
	 * Builds the full uri internal.
	 * <p>
	 * NOTE: The method is added to skip EJB intercepting when invoked from internal class and
	 * increase performance.
	 * 
	 * @param shortUri
	 *            the short uri
	 * @return the string
	 */
	private String buildFullUriInternal(String shortUri) {
		if (StringUtils.isNullOrEmpty(shortUri)) {
			throw new IllegalArgumentException("Undefined uri");
		}
		// probably full
		if (shortUri.startsWith("http")) {
			return shortUri;
		}
		if (isProperty(shortUri)) {
			String fullUri = propShort2Full.get(shortUri);
			if (fullUri != null) {
				return fullUri;
			}
		}

		String[] namespacePrefixAndPropertyName = URI_SPLIT_PATTERN.split(shortUri, -1);

		if ((namespacePrefixAndPropertyName == null)
				|| (namespacePrefixAndPropertyName.length != 2)) {
			throw new IllegalArgumentException("Malformed uri [" + shortUri + "]");
		}
		if (StringUtils.isNullOrEmpty(getNamespacesHolderMap().get(
				namespacePrefixAndPropertyName[0]))) {
			throw new IllegalStateException("Unknown namespace with prefix ["
					+ namespacePrefixAndPropertyName[0] + "] for URI [" + shortUri + "]");
		}

		String string = getNamespacesHolderMap().get(namespacePrefixAndPropertyName[0])
				+ namespacePrefixAndPropertyName[1];
		// update cache if needed
		if (isProperty(shortUri)) {
			propShort2Full.put(shortUri, string);
		}
		return string;
	}

	@Override
	public String getShortUri(URI fullUri) {
		if (fullUri == null) {
			throw new IllegalArgumentException("Undefined uri");
		}
		// to string method does not build anything so it's OK to call it without to worry about new
		// object creation, while the methods getNamespace() and getLocalName() substring from the
		// full URI when returning the result
		String full = fullUri.toString();
		String shortUri = propFull2Short.get(full);
		if (shortUri != null) {
			return shortUri;
		}

		String namespacePreffix = getPrefixesHolderMap().get(fullUri.getNamespace());
		if (namespacePreffix == null) {
			throw new IllegalStateException("Unknown prefix of namespace ["
					+ fullUri.getNamespace() + "] for URI [" + fullUri.toString() + "]");
		}
		shortUri = namespacePreffix + SHORT_URI_DELIMITER + fullUri.getLocalName();
		if (isProperty(shortUri)) {
			propFull2Short.put(full, shortUri);
		}
		return shortUri;
	}

	@Override
	public String getShortUri(Uri fullUri) {
		if (fullUri == null) {
			throw new IllegalArgumentException("Undefined uri");
		}
		// to string method does not build anything so it's OK to call it without to worry about new
		// object creation, while the methods getNamespace() and getLocalName() substring from the
		// full URI when returning the result
		String full = fullUri.toString();
		String shortUri = propFull2Short.get(full);
		if (shortUri != null) {
			return shortUri;
		}

		String namespacePreffix = getPrefixesHolderMap().get(fullUri.getNamespace());
		if (namespacePreffix == null) {
			throw new IllegalStateException("Unknown prefix of namespace ["
					+ fullUri.getNamespace() + "] for URI [" + fullUri.toString() + "]");
		}
		shortUri = namespacePreffix + SHORT_URI_DELIMITER + fullUri.getLocalName();
		if (isProperty(shortUri)) {
			propFull2Short.put(full, shortUri);
		}
		return shortUri;
	}

	@Override
	public String getShortUri(String fullUri) {
		if (fullUri == null) {
			throw new IllegalArgumentException("Undefined uri");
		}
		// probably short uri
		if (!fullUri.startsWith("http")) {
			return fullUri;
		}
		int lastIndexOf = fullUri.lastIndexOf(FULL_URI_DELITIMER);
		if (lastIndexOf < 0) {
			// additional check for uries that are not separated with # but with /
			lastIndexOf = fullUri.lastIndexOf('/');
			if (lastIndexOf < 0) {
				// probably not full uri
				return fullUri;
			}
		}
		// check the cache first
		String shortUri = propFull2Short.get(fullUri);
		if (shortUri != null) {
			return shortUri;
		}

		String namespaceUri = fullUri.substring(0, lastIndexOf + 1);
		String namespacePreffix = getPrefixesHolderMap().get(namespaceUri);
		if (namespacePreffix == null) {
			throw new IllegalStateException("Unknown prefix of namespace [" + namespaceUri
					+ "] for URI [" + fullUri + "]");
		}

		shortUri = namespacePreffix + SHORT_URI_DELIMITER + fullUri.substring(lastIndexOf + 1);
		// update cache
		if (isProperty(shortUri)) {
			propFull2Short.put(fullUri, shortUri);
		}

		return shortUri;
	}

	@Override
	public String getNamespaces() {
		try {
			lock.readLock().lock();
			return new String(namespacePrefixes.get());
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Observes for event for reloading of the cache of namespaces. Reloads the namespaces from the
	 * repository
	 *
	 * @param event
	 *            The event
	 */
	@Override
	public void observeReloadDefinitionEvent(@Observes LoadSemanticDefinitions event) {
		initNamespaces();
	}

	/**
	 * Checks this short URI if it's a property.
	 * 
	 * @param shortUri
	 *            the short uri
	 * @return true, if is property
	 */
	private boolean isProperty(String shortUri) {
		return SHORT_URI_LENGHT > shortUri.length();
	}

	@Override
	public URI buildUri(String uri) {
		return valueFactory.createURI(buildFullUriInternal(uri));
	}

	@Override
	public URI getDataGraph() {
		// cache the build value
		if (context == null) {
			context = valueFactory.createURI(contextName);
		}
		return context;
	}

}
