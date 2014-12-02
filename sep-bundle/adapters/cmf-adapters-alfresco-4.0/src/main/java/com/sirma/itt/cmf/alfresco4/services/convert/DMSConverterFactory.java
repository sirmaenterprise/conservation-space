package com.sirma.itt.cmf.alfresco4.services.convert;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.hash.HashCalculator;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.ExtensionPoint;

/**
 * A factory for creating converter objects.
 */
@ApplicationScoped
public class DMSConverterFactory {

	/** The dictionary service. */
	@Inject
	private DictionaryService dictionaryService;
	/** The type converter. */
	@Inject
	private TypeConverter typeConverter;
	/** The evaluator manager. */
	@Inject
	private ExpressionsManager evaluatorManager;

	/** The properties. */
	@Inject
	@ExtensionPoint(value = ConverterProperties.TARGET_NAME)
	private Iterable<ConverterProperties> properties;
	@Inject
	@ExtensionPoint(value = ConverterRegistry.TARGET_NAME)
	private Iterable<ConverterRegistry> converters;
	/** The cache context. */
	@Inject
	private EntityLookupCacheContext cacheContext;
	@Inject
	private HashCalculator hashCalculator;

	@Inject
	private Logger logger;
	/** The cached converters. */
	private Map<String, DMSTypeConverter> cachedConverters = new HashMap<String, DMSTypeConverter>();

	private Properties convertorProperties;

	/**
	 * Gets the converter by type name. Possible values are at
	 *
	 * @param p
	 *            the jee injection point
	 * @return the converter
	 *         {@link com.sirma.itt.cmf.alfresco4.services.convert.DMSTypeConverter.ConvertibleModels#getId()}
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
	public void init() throws Exception {
		convertorProperties = new Properties();
		for (ConverterProperties config : properties) {
			convertorProperties.putAll(config.getInternalProperties());
		}
		logger.debug("Loading internal properties : " + convertorProperties);
		for (ConverterProperties config : properties) {
			convertorProperties.putAll(config.getExternalProperties());
		}
		logger.debug("All loaded internal/external converter properties : " + convertorProperties);
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
		if (StringUtils.isNullOrEmpty(key)) {
			return null;
		}
		// get the ready instance
		if (cachedConverters.containsKey(key)) {
			return cachedConverters.get(key);
		}
		DMSTypeConverter.ConvertibleModels convertibleModel = new DMSTypeConverter.ConvertibleModels(
				key);
		logger.debug("Loading converter for model: " + key);
		DMSTypeConverter converter = new DMSTypeConverter(typeConverter, dictionaryService,
				evaluatorManager, convertibleModel, cacheContext, convertorProperties);
		converter.setHashCalculator(hashCalculator);
		cachedConverters.put(key, converter);
		return converter;
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
		return null;

	}
}