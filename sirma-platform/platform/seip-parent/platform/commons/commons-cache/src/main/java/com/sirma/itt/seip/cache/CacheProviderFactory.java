package com.sirma.itt.seip.cache;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationConverter;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.configuration.convert.ConverterContext;
import com.sirma.itt.seip.util.CDI;

/**
 * A factory for creating CacheProvider objects. The factory reads the configuration property
 * {@code cache.provider.class} to create the proper provider. If no provider is specified then the default will be
 * used: {@link #DEFAULT_CACHE_PROVIDER_CLASS}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class CacheProviderFactory {

	private static final String CANNOT_INSTANTIATE_CACHE_PROVIDER_IMPLEMENTATION = "Cannot instantiate CacheProvider implementation";

	/** Cache provider that stores everything in in-memory maps. */
	public static final String IN_MEMORY_CACHE_PROVIDER_CLASS = "com.sirma.itt.seip.cache.InMemoryCacheProvider";

	/** No cache provider. Disables internal cache. DO NOT USE IN PRODUCTION! */
	public static final String NULL_CACHE_PROVIDER_CLASS = "com.sirma.itt.seip.cache.NullCacheProvider";

	/** The Constant DEFAULT_CACHE_PROVIDER_CLASS. */
	public static final String DEFAULT_CACHE_PROVIDER_CLASS = IN_MEMORY_CACHE_PROVIDER_CLASS;

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheProviderFactory.class);

	/** The cache provider class. */
	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "cache.provider.class", type = CacheProvider.class, defaultValue = DEFAULT_CACHE_PROVIDER_CLASS, sensitive = true, system = true, label = "Cache provider class. "
			+ "<br>Default cache provider of no other is specified. Other possible option is "
			+ "<ul><li> com.sirma.itt.seip.cache.NullCacheProvider - No cache"
			+ "<li> com.sirma.itt.seip.cache.InMemoryCacheProvider - Default value if nothing is specified. This is non tansactional, no limit memory cache. If used for a long time the could lead to memory leaks and OutOfMemory exceptions"
			+ "<li> com.sirma.itt.cmf.cache.InfinispanCacheProvider - transactional, clustered and configurable cache. This needs additional module: cmf-cache-infinispan</ul><br>Default value: com.sirma.itt.cmf.cache.InfinispanCacheProvider")
	private ConfigurationProperty<CacheProvider> cacheProvider;

	/**
	 * Gets the provider.
	 *
	 * @return the provider
	 */
	@Produces
	@Default
	public CacheProvider getProvider() {
		cacheProvider.requireConfigured(CANNOT_INSTANTIATE_CACHE_PROVIDER_IMPLEMENTATION);
		return cacheProvider.get();
	}

	@ConfigurationConverter
	static CacheProvider buildCacheProvider(ConverterContext context, BeanManager beanManager) {
		String cacheProviderClass = context.getRawValue();
		try {
			LOGGER.info("Beginning initialization of cache provider: {}", cacheProviderClass);
			// this check here is invalid due to the fact the current is a reference value and
			// cannot be modified when the method is called

			Class<?> providerClass = Class.forName(cacheProviderClass);
			String cacheProviderId = "";
			CacheProviderType type = providerClass.getAnnotation(CacheProviderType.class);
			if (type != null) {
				cacheProviderId = type.value();
			}

			CacheProvider providerInstance = createCdiInstance(providerClass, cacheProviderId, beanManager);

			if (providerInstance != null) {
				// initialize provider here if needed
				LOGGER.info("Created cache provider successfully");
			}
			return providerInstance;
		} catch (Exception e) {
			Throwable t = e;
			while (t.getCause() != null) {
				t = t.getCause();
			}
			LOGGER.error("Failed to load cache provider class: {}", cacheProviderClass, e);
			return null;
		}
	}

	/**
	 * Creates a new CacheProvider object.
	 *
	 * @param providerClass
	 *            the provider class
	 * @param cacheProviderId
	 *            the cache provider id
	 * @param beanManager
	 * @return the cache provider
	 */
	private static CacheProvider createCdiInstance(Class<?> providerClass, String cacheProviderId,
			BeanManager beanManager) {
		// lookup for the class into the list of beans and create it via
		// CDI API
		Object beanInstance = CDI.instantiateBean(providerClass, beanManager,
				new AnnotationLiteralExtension(cacheProviderId));
		if (beanInstance instanceof CacheProvider) {
			return (CacheProvider) beanInstance;
		}

		LOGGER.error("Failed to locate cache provider: {}", providerClass);
		return null;
	}

	/**
	 * Cache provider literal.
	 *
	 * @author BBbonev
	 */
	@SuppressWarnings("serial")
	private static final class AnnotationLiteralExtension extends AnnotationLiteral<CacheProviderType>
			implements CacheProviderType {

		/** The value. */
		private String value;

		/**
		 * Instantiates a new annotation literal extension.
		 *
		 * @param value
		 *            the value
		 */
		public AnnotationLiteralExtension(String value) {
			this.value = value;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String value() {
			return value;
		}
	}
}
