package com.sirma.itt.emf.semantic;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;

import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.semantic.queries.SPARQLQueryHelper;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.collections.FixedSizeMap;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.definition.event.LoadSemanticDefinitions;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.runtime.boot.StartupPhase;
import com.sirma.itt.seip.security.annotation.OnTenantAdd;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.tasks.Schedule;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.TransactionMode;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.TransactionalRepositoryConnection;

/**
 * <p>
 * Namespace registry service implementation holding a cache of all defined namespaces and their prefixes in the
 * underlying semantic repository
 * </p>
 * <p>
 * The cache is eagerly loaded during application initialization. Subsequent updates of the namespace cache are defined
 * by the configuration setting
 * {@link com.sirma.itt.semantic.configuration.SemanticSyncConfigurationProperties#NAMESPACE_REGISTRY_REINIT_PERIOD}
 * </p>
 * <p>
 * Depends on PatchDbService, because the semantic repository has to be initialized for the registry to work properly.
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
	private static final int SHORT_URI_LENGHT = 40;

	@Inject
	private Instance<TransactionalRepositoryConnection> repositoryConnection;

	@Inject
	private Contextual<NamespaceCache> cache;

	/**
	 * Temporary cache for storing properties mapping for full URI to short one with maximum size of 1024 elements
	 */
	private Map<String, String> propFull2Short;
	/**
	 * Temporary cache for storing properties mapping for short URI to full one with maximum size of 1024 elements
	 */
	private Map<String, String> propShort2Full;

	private Map<String, URI> uriCache;

	/** Pattern that is used to split URIs by the short and/or full delimiter */
	private static final Pattern URI_SPLIT_PATTERN = Pattern.compile(SHORT_URI_DELIMITER + "|" + FULL_URI_DELITIMER);
	/** The value factory. */
	@Inject
	private ValueFactory valueFactory;

	/** The context name. */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "semantic.db.context.name", type = URI.class, defaultValue = "http://ittruse.ittbg.com/data/enterpriseManagementFramework", label = "Name of the context in the repository that will be used")
	private ConfigurationProperty<URI> context;

	/**
	 * Initializes and schedules the re-initialization of the namespace registry cache
	 */
	@PostConstruct
	protected void initAndSchedule() {
		// create self cleaning maps
		propFull2Short = new FixedSizeMap<>(MAPPING_CACHE_SIZE);
		propShort2Full = new FixedSizeMap<>(MAPPING_CACHE_SIZE);
		uriCache = new FixedSizeMap<>(MAPPING_CACHE_SIZE * 10);

		cache.initializeWith(() -> new NamespaceCache(repositoryConnection));
		SPARQLQueryHelper.setNamespaceRegistryService(this);
	}

	/**
	 * Convert string to uri.
	 *
	 * @param converterContext
	 *            the converter context
	 * @param factory
	 *            the factory
	 * @return the uri
	 */
	@ConfigurationConverter
	static URI convertStringToUri(ConverterContext converterContext, ValueFactory factory) {
		return factory.createURI(converterContext.getRawValue());
	}

	/**
	 * The method uses {@link org.openrdf.repository.RepositoryConnection} in order to get all declared namespaces in
	 * the underlying semantic repository and cache them
	 */
	@RunAsAllTenantAdmins
	@OnTenantAdd
	@Startup(phase = StartupPhase.BEFORE_APP_START, order = Double.MAX_VALUE - 100)
	@Schedule(identifier = "REFRESH_NAMESPACE_REGISTRY", transactionMode = TransactionMode.NOT_SUPPORTED)
	@ConfigurationPropertyDefinition(name = "semantic.namespace.registry.reinitperiod", defaultValue = "0 0/15 * ? * *", sensitive = true, system = true, label = "Namespace registry re-initialization schedule setting [minutes]. The namespace registry cache is to be re-initialized on every minute divisible by this setting value. ")
	void initNamespaces() {
		cache.getContextValue().reload();
	}

	@Override
	public String getNamespace(String prefix) {
		return cache.getContextValue().getNamespace(prefix);
	}

	@Override
	public String buildFullUri(String shortUri) {
		return buildFullUriInternal(shortUri);
	}

	@Override
	public Map<String, String> getProvidedNamespaces() {
		return cache.getContextValue().getNamespaces();
	}

	/**
	 * Builds the full uri internal.
	 * <p>
	 * NOTE: The method is added to skip EJB intercepting when invoked from internal class and increase performance.
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

		if (namespacePrefixAndPropertyName == null || namespacePrefixAndPropertyName.length != 2) {
			throw new IllegalArgumentException("Malformed uri [" + shortUri + "]");
		}

		String string = cache.getContextValue().getNamespace(namespacePrefixAndPropertyName[0])
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

		String namespacePreffix = cache.getContextValue().getPrefix(fullUri.getNamespace());
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

		String namespacePreffix = cache.getContextValue().getPrefix(fullUri.getNamespace());
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
		String namespacePreffix = cache.getContextValue().getPrefix(namespaceUri);

		shortUri = namespacePreffix + SHORT_URI_DELIMITER + fullUri.substring(lastIndexOf + 1);
		// update cache
		if (isProperty(shortUri)) {
			propFull2Short.put(fullUri, shortUri);
		}

		return shortUri;
	}

	@Override
	public String getNamespaces() {
		return cache.getContextValue().getNamespacePrefixes();
	}

	/**
	 * Observes for event for reloading of the cache of namespaces. Reloads the namespaces from the repository
	 *
	 * @param event
	 *            The event
	 */
	protected void observeReloadDefinitionEvent(@Observes LoadSemanticDefinitions event) {
		initNamespaces();
	}

	/**
	 * Checks this short URI if it's a property.
	 *
	 * @param shortUri
	 *            the short uri
	 * @return true, if is property
	 */
	private static boolean isProperty(String shortUri) {
		return SHORT_URI_LENGHT > shortUri.length();
	}

	@Override
	public URI buildUri(String uri) {
		URI cached = uriCache.get(uri);
		if (cached == null) {
			String fullUri = buildFullUriInternal(uri);
			cached = uriCache.get(fullUri);
			if (cached == null) {
				cached = valueFactory.createURI(fullUri);
				// added fullUri and argument to URI object cache to limit created objects
				uriCache.put(uri, cached);
				uriCache.put(fullUri, cached);
			}
		}
		return cached;
	}

	@Override
	public URI getDataGraph() {
		Serializable configuration = Options.USE_CUSTOM_GRAPH.get();
		if (configuration instanceof URI) {
			return (URI) configuration;
		}
		if (configuration instanceof String) {
			return buildUri((String) configuration);
		}
		return context.get();
	}

	/**
	 * Namespace cache.
	 *
	 * @author BBonev
	 */
	private static class NamespaceCache {
		private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
		private final AtomicReference<Map<String, String>> namespaces = new AtomicReference<>();
		private final AtomicReference<Map<String, String>> prefixes = new AtomicReference<>();
		private final AtomicReference<String> namespacePrefixes = new AtomicReference<>();
		private final Set<String> failedToResolve = new HashSet<>();

		private final Instance<TransactionalRepositoryConnection> repositoryConnection;

		private final Lock lock = new ReentrantLock();
		private volatile boolean isLoaded = false;
		private volatile boolean isReloading = false;

		/**
		 * Instantiates a new namespace cache.
		 *
		 * @param repositoryConnection
		 *            the repository connection
		 */
		public NamespaceCache(Instance<TransactionalRepositoryConnection> repositoryConnection) {
			this.repositoryConnection = repositoryConnection;
		}

		/**
		 * Reload.
		 */
		public void reload() {

			if (isLoaded && isReloading) {
				// no need to continue we are currently reloading any way
				return;
			}

			Map<String, String> updatedNamespaces = new HashMap<>();
			Map<String, String> updatedPrefixes = new HashMap<>();
			StringBuilder updatedNamespacePrefixes = new StringBuilder(2048);

			boolean loaded = isLoaded;

			try {
				lock.lock();
				if (loaded != isLoaded) {
					// the resources has been loading while we were waiting, no need to continue
					return;
				}
				isReloading = true;
				doReload(updatedNamespaces, updatedPrefixes, updatedNamespacePrefixes);

				namespaces.set(updatedNamespaces);
				prefixes.set(updatedPrefixes);
				namespacePrefixes.set(updatedNamespacePrefixes.toString());

				failedToResolve.removeAll(updatedNamespaces.keySet());
				failedToResolve.removeAll(updatedPrefixes.keySet());
			} finally {
				isReloading = false;
				lock.unlock();
			}
			isLoaded = true;
		}

		private void doReload(Map<String, String> updatedNamespaces, Map<String, String> updatedPrefixes,
				StringBuilder updatedNamespacePrefixes) {
			RepositoryResult<Namespace> currentNamespaces = null;
			try (TransactionalRepositoryConnection connection = repositoryConnection.get()) {
				currentNamespaces = connection.getNamespaces();
				processNamespaces(updatedNamespaces, updatedPrefixes, updatedNamespacePrefixes, currentNamespaces);
			} catch (RepositoryException e) {
				throw new EmfRuntimeException("Could not reload namespaces", e);
			} finally {
				if (currentNamespaces != null) {
					try {
						currentNamespaces.close();
					} catch (RepositoryException e) {
						LOG.warn("Could not close connection", e);
					}
				}
			}
		}

		private static void processNamespaces(Map<String, String> updatedNamespaces,
				Map<String, String> updatedPrefixes, StringBuilder updatedNamespacePrefixes,
				RepositoryResult<Namespace> currentNamespaces) throws RepositoryException {
			LOG.debug("Loading namespaces...");
			while (currentNamespaces.hasNext()) {
				Namespace namespace = currentNamespaces.next();
				LOG.debug(namespace.toString());
				updatedNamespaces.put(namespace.getPrefix(), namespace.getName());
				if (updatedPrefixes.containsKey(namespace.getName())) {
					LOG.warn(
							"\n===========================================\n    Found namespace with multiple prefixes: namespace={} {} and {} "
									+ "\n===========================================",
									namespace.getName(), namespace.getPrefix(), updatedPrefixes.get(namespace.getName()));
					continue;
				}
				updatedPrefixes.put(namespace.getName(), namespace.getPrefix());

				updatedNamespacePrefixes
				.append("PREFIX ")
				.append(namespace.getPrefix().trim())
				.append(SHORT_URI_DELIMITER)
				.append("<")
				.append(namespace.getName().trim())
				.append(">")
				.append("\n");

			}
		}

		/**
		 * Gets the namespaces.
		 *
		 * @return the namespaces
		 */
		public Map<String, String> getNamespaces() {
			if (!isLoaded) {
				reload();
			}
			return namespaces.get();
		}

		/**
		 * Gets the namespace for the given prefix
		 * <p>
		 * For input like {@code emf} you get {@code http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#}
		 * <p>
		 * Note that the method will throw an {@link IllegalStateException} if the given prefix is not recognized
		 *
		 * @param prefix
		 *            the prefix
		 * @return the namespace
		 */
		public String getNamespace(String prefix) {
			String namespace = getNamespaces().get(prefix);
			if (StringUtils.isNullOrEmpty(namespace) && !failedToResolve.contains(prefix)) {
				reload();
				namespace = getNamespaces().get(prefix);
			}
			if (StringUtils.isNullOrEmpty(namespace)) {
				failedToResolve.add(prefix);
				throw new IllegalStateException("Unknown namespace for prefix [" + prefix + "]");
			}

			return namespace;
		}

		/**
		 * Gets the prefixes.
		 *
		 * @return the prefixes
		 */
		public Map<String, String> getPrefixes() {
			if (!isLoaded) {
				reload();
			}
			return prefixes.get();
		}

		/**
		 * Gets the prefix for a given namespace
		 * <p>
		 * For input like {@code http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#} you get {@code emf}
		 * <p>
		 * Note that the method will throw an {@link IllegalStateException} if the given namespace is not recognized
		 *
		 * @param namespace
		 *            the namespace
		 * @return the prefix
		 */
		public String getPrefix(String namespace) {
			String prefix = getPrefixes().get(namespace);
			if (StringUtils.isNullOrEmpty(prefix) && !failedToResolve.contains(namespace)) {
				reload();
				prefix = getPrefixes().get(namespace);
			}
			if (StringUtils.isNullOrEmpty(prefix)) {
				failedToResolve.add(namespace);
				throw new IllegalStateException("Unknown prefix for namespace [" + namespace + "]");
			}

			return prefix;
		}

		/**
		 * Gets the namespace prefixes.
		 *
		 * @return the namespacePrefixes
		 */
		public String getNamespacePrefixes() {
			if (!isLoaded) {
				reload();
			}
			return namespacePrefixes.get();
		}
	}

}
