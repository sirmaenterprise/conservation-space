package com.sirma.itt.emf.semantic;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
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
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
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
import com.sirma.itt.semantic.ReadOnly;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final int MAPPING_CACHE_SIZE = 1024;
	private static final int SHORT_URI_LENGHT = 40;
	private static final String UNDEFINED_URI = "Undefined uri";

	@Inject
	@ReadOnly
	private RepositoryConnection repositoryConnection;

	@Inject
	private Contextual<NamespaceCache> cache;

	@Inject
	private Contextual<NamespacePropertiesCache> propertiesCache;

	/** Pattern that is used to split URIs by the short and/or full delimiter */
	private static final Pattern URI_SPLIT_PATTERN = Pattern.compile(SHORT_URI_DELIMITER + "|" + FULL_URI_DELITIMER);
	/** The value factory. */
	@Inject
	private ValueFactory valueFactory;

	/** The context name. */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "semantic.db.context.name", type = IRI.class, defaultValue = "http://ittruse.ittbg.com/data/enterpriseManagementFramework", label = "Name of the context in the repository that will be used")
	private ConfigurationProperty<IRI> context;

	/**
	 * Initializes and schedules the re-initialization of the namespace registry cache
	 */
	@PostConstruct
	protected void initAndSchedule() {
		cache.initializeWith(() -> new NamespaceCache(repositoryConnection));
		propertiesCache.initializeWith(NamespacePropertiesCache::new);
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
	static IRI convertStringToUri(ConverterContext converterContext, ValueFactory factory) {
		return factory.createIRI(converterContext.getRawValue());
	}

	/**
	 * The method uses {@link org.eclipse.rdf4j.repository.RepositoryConnection} in order to get all declared namespaces
	 * in the underlying semantic repository and cache them
	 */
	@RunAsAllTenantAdmins
	@OnTenantAdd
	@Startup(phase = StartupPhase.BEFORE_APP_START, order = 1000000, transactionMode = TransactionMode.NOT_SUPPORTED)
	@Schedule(identifier = "REFRESH_NAMESPACE_REGISTRY", transactionMode = TransactionMode.NOT_SUPPORTED, system = false)
	@ConfigurationPropertyDefinition(name = "semantic.namespace.registry.reinitperiod", defaultValue = "0 0/15 * ? * *", sensitive = true, system = true, label = "Namespace registry re-initialization schedule setting [minutes]. The namespace registry cache is to be re-initialized on every minute divisible by this setting value. ")
	void initNamespaces() {
		cache.getContextValue().reload();
		propertiesCache.reset();
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
		LOGGER.trace("Building uri for raw value: {} ", shortUri);
		if (StringUtils.isBlank(shortUri)) {
			throw new IllegalArgumentException(UNDEFINED_URI);
		}
		// probably full
		if (shortUri.startsWith("http")) {
			return shortUri;
		}
		NamespacePropertiesCache propCache = propertiesCache.getContextValue();
		if (isProperty(shortUri)) {
			String fullUri = propCache.getFullIri(shortUri);
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
			propCache.addProperty(shortUri, string);
		}
		return string;
	}

	@Override
	public String getShortUri(IRI fullUri) {
		if (fullUri == null) {
			throw new IllegalArgumentException(UNDEFINED_URI);
		}
		// to string method does not build anything so it's OK to call it without to worry about new
		// object creation, while the methods getNamespace() and getLocalName() substring from the
		// full IRI when returning the result
		String full = fullUri.toString();
		NamespacePropertiesCache propCache = propertiesCache.getContextValue();
		String shortUri = propCache.getShortIri(full);
		if (shortUri != null) {
			return shortUri;
		}

		String namespacePreffix = cache.getContextValue().getPrefix(fullUri.getNamespace());
		shortUri = namespacePreffix + SHORT_URI_DELIMITER + fullUri.getLocalName();
		if (isProperty(shortUri)) {
			propCache.addProperty(shortUri, full);
		}
		return shortUri;
	}

	@Override
	public String getShortUri(Uri fullUri) {
		if (fullUri == null) {
			throw new IllegalArgumentException(UNDEFINED_URI);
		}
		// to string method does not build anything so it's OK to call it without to worry about new
		// object creation, while the methods getNamespace() and getLocalName() substring from the
		// full IRI when returning the result
		NamespacePropertiesCache propCache = propertiesCache.getContextValue();
		String full = fullUri.toString();
		String shortUri = propCache.getShortIri(full);
		if (shortUri != null) {
			return shortUri;
		}

		String namespacePreffix = cache.getContextValue().getPrefix(fullUri.getNamespace());
		shortUri = namespacePreffix + SHORT_URI_DELIMITER + fullUri.getLocalName();
		if (isProperty(shortUri)) {
			propCache.addProperty(shortUri, full);
		}
		return shortUri;
	}

	@Override
	public String getShortUri(String fullUri) {
		if (fullUri == null) {
			throw new IllegalArgumentException(UNDEFINED_URI);
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
		NamespacePropertiesCache propCache = propertiesCache.getContextValue();
		String shortUri = propCache.getShortIri(fullUri);
		if (shortUri != null) {
			return shortUri;
		}

		String namespaceUri = fullUri.substring(0, lastIndexOf + 1);
		String namespacePreffix = cache.getContextValue().getPrefix(namespaceUri);

		shortUri = namespacePreffix + SHORT_URI_DELIMITER + fullUri.substring(lastIndexOf + 1);

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
	 * Checks this short IRI if it's a property.
	 *
	 * @param shortUri
	 *            the short uri
	 * @return true, if is property
	 */
	private static boolean isProperty(String shortUri) {
		return SHORT_URI_LENGHT > shortUri.length();
	}

	@Override
	public IRI buildUri(String uri) {
		String fullUri = buildFullUriInternal(uri);
		return valueFactory.createIRI(fullUri);
	}

	@Override
	public IRI getDataGraph() {
		Serializable configuration = Options.USE_CUSTOM_GRAPH.get();
		if (configuration instanceof IRI) {
			return (IRI) configuration;
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

		private final RepositoryConnection repositoryConnection;

		private final Lock lock = new ReentrantLock();
		private volatile boolean isLoaded = false;
		private volatile boolean isReloading = false;

		/**
		 * Instantiates a new namespace cache.
		 *
		 * @param repositoryConnection
		 *            the repository connection
		 */
		public NamespaceCache(RepositoryConnection repositoryConnection) {
			this.repositoryConnection = repositoryConnection;
		}

		/**
		 * Reloads the supported namespaces.
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
			try (RepositoryResult<Namespace> currentNamespaces = repositoryConnection.getNamespaces()) {
				processNamespaces(updatedNamespaces, updatedPrefixes, updatedNamespacePrefixes, currentNamespaces);
			} catch (RepositoryException e) {
				throw new EmfRuntimeException("Could not reload namespaces", e);
			}
		}

		private static void processNamespaces(Map<String, String> updatedNamespaces,
				Map<String, String> updatedPrefixes, StringBuilder updatedNamespacePrefixes,
				RepositoryResult<Namespace> currentNamespaces) {
			LOG.debug("Loading namespaces...");
			while (currentNamespaces.hasNext()) {
				Namespace namespace = currentNamespaces.next();
				LOG.debug("{}", namespace);
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
			if (StringUtils.isBlank(namespace) && !failedToResolve.contains(prefix)) {
				reload();
				namespace = getNamespaces().get(prefix);
			}
			if (StringUtils.isBlank(namespace)) {
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
			if (StringUtils.isBlank(prefix) && !failedToResolve.contains(namespace)) {
				reload();
				prefix = getPrefixes().get(namespace);
			}
			if (StringUtils.isBlank(prefix)) {
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

	/**
	 * Namespace Properties cache.
	 *
	 * @author KPenev
	 */
	private static class NamespacePropertiesCache {
		/**
		 * Temporary cache for storing properties mapping for full IRI to short one with maximum size of 1024 elements
		 */
		private Map<String, String> propFull2Short;
		/**
		 * Temporary cache for storing properties mapping for short IRI to full one with maximum size of 1024 elements
		 */
		private Map<String, String> propShort2Full;

		public NamespacePropertiesCache() {
			// create self cleaning maps
			propFull2Short = Collections.synchronizedMap(new FixedSizeMap<>(MAPPING_CACHE_SIZE));
			propShort2Full = Collections.synchronizedMap(new FixedSizeMap<>(MAPPING_CACHE_SIZE));
		}

		/**
		 * Adds property IRI mapping to the cache
		 * 
		 * @param shortIri
		 *            Property Short IRI
		 * @param fullIri
		 *            Property Full IRI
		 */
		public void addProperty(String shortIri, String fullIri) {
			propFull2Short.put(fullIri, shortIri);
			propShort2Full.put(shortIri, fullIri);
		}

		/**
		 * Gets property`s full IRI by short IRI if it is in the cache
		 * 
		 * @param fullIri
		 *            Full IRI of the property
		 * @return Full IRI of the property or null if it isn't located in the cache
		 */
		public String getShortIri(String fullIri) {
			return propFull2Short.get(fullIri);
		}

		/**
		 * Gets full IRI of the property by given short IRI
		 * 
		 * @param shortIri
		 *            Short IRI of the property
		 * @return Short IRI of the property or null if the property isn't located in the cache
		 */
		public String getFullIri(String shortIri) {
			return propShort2Full.get(shortIri);
		}

	}

}
