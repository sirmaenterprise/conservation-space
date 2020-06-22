package com.sirma.itt.cmf.alfresco4.services.convert;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverterImpl.ConvertibleModels;
import com.sirma.itt.seip.CachingSupplier;
import com.sirma.itt.seip.MutationObservable;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Resettable;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.context.Contextual;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * A factory for creating converter objects.
 */
@ApplicationScoped
public class DMSConverterFactory implements Resettable {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private DefinitionService definitionService;
	@Inject
	private TypeConverter typeConverter;
	@Inject
	private ExpressionsManager evaluatorManager;

	@Inject
	@ExtensionPoint(value = ConverterProperties.TARGET_NAME)
	private Iterable<ConverterProperties> propertiesExtensions;

	@Inject
	@ExtensionPoint(value = ConverterRegistry.TARGET_NAME)
	private Iterable<ConverterRegistry> converters;

	@Inject
	private EntityLookupCacheContext cacheContext;

	@Inject
	private HashCalculator hashCalculator;

	@Inject
	private ContextualMap<String, DMSTypeConverter> cachedConverters;

	@Inject
	private Contextual<Properties> convertorProperties;

	private Collection<Resettable> resettableCache = new LinkedList<>();

	/**
	 * Gets the converter by type name. Possible values are at
	 *
	 * @param p
	 *            the jee injection point
	 * @return the converter
	 *         {@link com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverterImpl.ConvertibleModels#getId()}
	 */
	@Produces
	@Converter
	public DMSTypeConverter getConverter(InjectionPoint p) {
		Converter annotation = p.getAnnotated().getAnnotation(Converter.class);
		String key = annotation.name().toLowerCase();
		return getConverterByName(key);
	}

	/**
	 * Init of factory.
	 */
	@PostConstruct
	public void init() {
		convertorProperties.initializeWith(this::initializeProperties);
		MutationObservable.registerToAll(propertiesExtensions, this::reset);
	}

	Properties initializeProperties() {
		Properties properties = new Properties();
		for (ConverterProperties config : propertiesExtensions) {
			properties.putAll(config.getInternalProperties());
		}
		LOGGER.debug("Loading internal properties : {}", properties);
		for (ConverterProperties config : propertiesExtensions) {
			properties.putAll(config.getExternalProperties());
		}
		LOGGER.debug("All loaded internal/external converter properties : {}", properties);
		return properties;
	}

	/**
	 * Internal method to get converter for given prefix config name.
	 *
	 * @param key
	 *            is the key to use
	 * @return the converter (cached if not new)
	 */
	private DMSTypeConverter getConverterByName(String key) {
		// fail silently
		if (StringUtils.isBlank(key)) {
			return null;
		}
		Supplier<DMSTypeConverter> converterSupplier = () -> cachedConverters.computeIfAbsent(key, k -> {
			DMSTypeConverterImpl.ConvertibleModels convertibleModel = new DMSTypeConverterImpl.ConvertibleModels(k);
			LOGGER.debug("Loading converter for model: {}", k);
			return new DMSTypeConverterImpl(typeConverter, definitionService, evaluatorManager, convertibleModel,
					cacheContext, convertorProperties::getContextValue, baseModelProvider(convertibleModel.getId()),
					hashCalculator);
		});
		DMSTypeConverterProxy converterProxy = new DMSTypeConverterProxy(converterSupplier);
		resettableCache.add(converterProxy);
		return converterProxy;
	}

	/**
	 * Gets the model converter by class of model. Different providers could be associated with
	 * {@link ConverterRegistry} extension
	 *
	 * @param model
	 *            is the model instance class to get converter for
	 * @return the found converter or null
	 */
	public DMSTypeConverter getConverter(Class<? extends Instance> model) {
		for (ConverterRegistry converter : converters) {
			String nameForSupportedType = converter.getNameForSupportedType(model);
			if (nameForSupportedType != null) {
				return getConverterByName(nameForSupportedType);
			}
		}
		return getConverterByName(ConverterConstants.GENERAL.toLowerCase());
	}

	/**
	 * Creates lazy initialized caching supplier that provides base definition model for the given
	 * {@link ConvertibleModels}.
	 *
	 * @param modelId
	 *            the model id
	 * @return the supplier
	 */
	Supplier<DefinitionModel> baseModelProvider(String modelId) {
		Supplier<DefinitionModel> provider = () -> fetchBaseModelById(modelId);
		CachingSupplier<DefinitionModel> cachingSupplier = new CachingSupplier<>(provider);
		resettableCache.add(cachingSupplier);
		return cachingSupplier;
	}

	private DefinitionModel fetchBaseModelById(String modelId) {
		try {
			Properties properties = convertorProperties.getContextValue();
			// load converter data
			String baseId = properties.getProperty(modelId + ".baseDefinitionId");
			// this here is for generic model
			if (baseId == null) {
				if (EqualsHelper.nullSafeEquals(ConverterConstants.GENERAL, modelId, true)) {
					return null;
				}
				throw new EmfRuntimeException(
						"Could not find definition class or base definition id for model " + modelId);
			}

			return definitionService.find(baseId);
		} catch (Exception e) {
			LOGGER.warn("Failed to find definition class for model: {}", modelId, e);
			throw new EmfRuntimeException(e);
		}
	}

	@Override
	public void reset() {
		convertorProperties.clearContextValue();
		// iterate a snapshot of the cache, because someone could modify the collection during iteration
		new ArrayList<>(resettableCache).forEach(r -> r.reset());
	}

	/**
	 * Proxy class for {@link DMSTypeConverter} that will be injected and will be initialized lazily
	 *
	 * @author BBonev
	 */
	private static class DMSTypeConverterProxy implements DMSTypeConverter {

		private final Supplier<DMSTypeConverter> delegate;

		/**
		 * Instantiates a new DMS type converter proxy.
		 *
		 * @param supplier
		 *            the supplier
		 */
		public DMSTypeConverterProxy(Supplier<DMSTypeConverter> supplier) {
			delegate = supplier;
		}

		@Override
		public void reset() {
			delegate.get().reset();
		}

		@Override
		public Map<String, Serializable> filterCMFProperties(Map<String, Serializable> properties,
				FieldProcessor level) {
			return delegate.get().filterCMFProperties(properties, level);
		}

		@Override
		public Map<String, Serializable> filterCMFProperties(DefinitionModel defModel,
				Map<String, Serializable> properties, FieldProcessor level) {
			return delegate.get().filterCMFProperties(defModel, properties, level);
		}

		@Override
		public Map<String, Serializable> filterDMSProperties(DefinitionModel defModel,
				Map<String, Serializable> properties, FieldProcessor level) throws JSONException {
			return delegate.get().filterDMSProperties(defModel, properties, level);
		}

		@Override
		public Map<String, Serializable> filterDMSProperties(DefinitionModel defModel, JSONObject properties,
				FieldProcessor level) throws JSONException {
			return delegate.get().filterDMSProperties(defModel, properties, level);
		}

		@Override
		public Pair<String, Serializable> convertCMFtoDMSProperty(String key, Serializable value,
				FieldProcessor processor) {
			return delegate.get().convertCMFtoDMSProperty(key, value, processor);
		}

		@Override
		public Pair<String, Serializable> convertCMFtoDMSProperty(String key, Serializable value, Instance instance,
				FieldProcessor processor) {
			return delegate.get().convertCMFtoDMSProperty(key, value, instance, processor);
		}

		@Override
		public Map<String, Serializable> convertCMFtoDMSProperties(Map<String, Serializable> properties,
				FieldProcessor processor) {
			return delegate.get().convertCMFtoDMSProperties(properties, processor);
		}

		@Override
		public Map<String, Serializable> convertCMFtoDMSProperties(Map<String, Serializable> properties,
				Instance instance, FieldProcessor level) {
			return delegate.get().convertCMFtoDMSProperties(properties, instance, level);
		}

		@Override
		public Map<String, Serializable> convertCMFtoDMSProperties(Map<String, Serializable> properties,
				Instance instance, DefinitionModel definition, FieldProcessor level) {
			return delegate.get().convertCMFtoDMSProperties(properties, instance, definition, level);
		}

		@Override
		public Map<String, Serializable> convertDMSToCMFProperties(JSONObject jsonObject, Instance instance,
				FieldProcessor level) throws JSONException {
			return delegate.get().convertDMSToCMFProperties(jsonObject, instance, level);
		}

		@Override
		public Map<String, Serializable> convertDMSToCMFProperties(Map<String, Serializable> props, Instance instance,
				FieldProcessor level) throws JSONException {
			return delegate.get().convertDMSToCMFProperties(props, instance, level);
		}

		@Override
		public Map<String, Serializable> convertDMSToCMFProperties(JSONObject jsonObject, FieldProcessor processor) {
			return delegate.get().convertDMSToCMFProperties(jsonObject, processor);
		}

		@Override
		public Pair<String, Serializable> convertDMSToCMFProperty(String dmsKey, Serializable objectValue,
				FieldProcessor level) {
			return delegate.get().convertDMSToCMFProperty(dmsKey, objectValue, level);
		}

		@Override
		public Map<String, Serializable> convertCMFtoDMSPropertiesByValue(Map<String, Serializable> properties,
				FieldProcessor processor) {
			return delegate.get().convertCMFtoDMSPropertiesByValue(properties, processor);
		}

		@Override
		public Map<String, Serializable> convertDMStoCMFPropertiesByValue(JSONObject properties, Instance instance,
				FieldProcessor level) throws JSONException {
			return delegate.get().convertDMStoCMFPropertiesByValue(properties, instance, level);
		}

		@Override
		public String getModelPrefix() {
			return delegate.get().getModelPrefix();
		}
	}
}