package com.sirma.itt.cmf.alfresco4.services.convert;

import static com.sirma.itt.seip.util.LoggingUtil.shortenToNull;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.adapters.remote.AlfrescoErrorReader;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.NoEntityLookup;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfConfigurationException;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * The Class DMSTypeConverter is custom converter for specific models. <br>
 * REVIEW: why not separate the converter into 2 one for CMF->DMS and one DMS->CMF so we can have more clear
 * implementation with common interface/abstract class
 */
@SuppressWarnings("unchecked")
public class DMSTypeConverterImpl implements AlfrescoCommunicationConstants, DMSTypeConverter {

	/**
	 * The class ConvertibleModels holds the possible models converters as dynamic reference to id of the model.
	 */
	static class ConvertibleModels {

		private String id;

		/**
		 * Instantiates a new convertible models.
		 *
		 * @param prefix
		 *            the prefix
		 */
		ConvertibleModels(String prefix) {
			setId(prefix);
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private TypeConverter typeConverter;

	private DefinitionService definitionService;

	private ConvertibleModels model;

	private ExpressionsManager evaluatorManager;

	private Supplier<Properties> convertorProperties;

	private Properties cmfTOdms;

	private Map<String, Set<String>> dmsTOcmf;

	private String modelPrefix;

	private String cacheName;

	private static final String CACHE_PREFIX = "DMS_TYPE_CACHE_FOR_";

	private EntityLookupCacheContext cacheContext;

	private HashCalculator hashCalculator;

	private Supplier<DefinitionModel> baseModelSupplier;

	/**
	 * Instantiates a new dMS type converter.
	 *
	 * @param typeConverter
	 *            the type converter
	 * @param definitionService
	 *            the definition service
	 * @param evaluatorManager
	 *            the evaluator manager
	 * @param model
	 *            the model
	 * @param cacheContext
	 *            the cache provider
	 * @param convertorProperties
	 *            is the localtion for convertor config
	 * @param baseModelSupplier
	 *            the base model supplier
	 * @param hashCalculator
	 *            the hash calculator
	 */
	@SuppressWarnings("squid:S00107")
	public DMSTypeConverterImpl(TypeConverter typeConverter, DefinitionService definitionService,
			ExpressionsManager evaluatorManager, ConvertibleModels model, EntityLookupCacheContext cacheContext,
			Supplier<Properties> convertorProperties, Supplier<DefinitionModel> baseModelSupplier,
			HashCalculator hashCalculator) {
		this.typeConverter = typeConverter;
		this.definitionService = definitionService;
		this.evaluatorManager = evaluatorManager;
		this.model = model;
		this.cacheContext = cacheContext;
		this.convertorProperties = convertorProperties;
		this.baseModelSupplier = baseModelSupplier;
		this.hashCalculator = hashCalculator;
		initializeCache();
	}

	/**
	 * Gets the mirror cache.
	 *
	 * @return the mirror cache
	 */
	private EntityLookupCache<String, Map<String, PropertyDefinition>, Serializable> getMirrorCache() {
		return cacheContext.getCache(cacheName);
	}

	/**
	 * Creates the default property.
	 *
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * @return the pair
	 */
	protected Pair<String, Serializable> createDefaultProperty(String name, Serializable value) {
		return new Pair<>(getModelPrefix() + name, value);
	}

	/**
	 * Convert dms to cmf property.
	 *
	 * @param definition
	 *            the definition
	 * @param value
	 *            the value to convert
	 * @return the pair of converted value and definition name as key
	 */
	protected Pair<String, Serializable> convertDMSToCMFPropertyInernal(PropertyDefinition definition,
			Serializable value) {

		Serializable valueReturned;
		if (definition.isMultiValued() && value instanceof Collection) {
			List<Object> valueReturnedList = new ArrayList<>(((Collection<Object>) value).size());
			for (Object next : (Collection<Object>) value) {
				valueReturnedList.add(typeConverter.convert(definition.getDataType().getJavaClass(), next));
			}
			valueReturned = (Serializable) valueReturnedList;
		} else {
			// convert to proper value type
			valueReturned = (Serializable) typeConverter.convert(definition.getDataType().getJavaClass(), value);
		}
		valueReturned = evaluatorManager.ruleConvertFrom(definition, valueReturned);
		return new Pair<>(definition.getName(), valueReturned);
	}

	/**
	 * Do convert internal to dms.
	 *
	 * @param definition
	 *            the definition
	 * @param value
	 *            the value
	 * @return the pair
	 */
	protected Pair<String, Serializable> doConvertInternalToDMS(PropertyDefinition definition, Serializable value) {
		int indexOfSkippable = definition.getDmsType().indexOf('-');
		if (indexOfSkippable == 0) {
			indexOfSkippable = 1;
		} else {
			indexOfSkippable = 0;
		}
		Pair<String, Serializable> pair = new Pair<>(definition.getDmsType().substring(indexOfSkippable), null);
		if (definition.isMultiValued()) {
			Serializable serializable = evaluatorManager.ruleConvertTo(definition, value);
			if (serializable instanceof Collection) {
				pair.setSecond(typeConverter.convert(String.class, serializable));
			} else {
				pair.setSecond(serializable);
			}
		} else {
			pair.setSecond(typeConverter.convert(String.class, evaluatorManager.ruleConvertTo(definition, value)));
		}
		return pair;
	}

	/**
	 * Load converter properties and initialize converter params.
	 */
	private void initializeCache() {
		// load the cache on valid model
		cacheName = CACHE_PREFIX + model.getId().toUpperCase();
		if (!cacheContext.containsCache(cacheName)) {
			cacheContext.createCache(cacheName, new NoEntityLookup());
		}
	}

	/**
	 * Loads the default mapping for cmf properties to dms properties. Throws {@link EmfConfigurationException} on any
	 * error or unexpected values
	 */
	private void loadPropertiesMappings() {
		Properties properties = convertorProperties.get();
		try {
			// do a loading mapping by mapping
			String[] propertiesFiles = properties.getProperty(model.getId() + ".propertiesFile").split("\\|");
			String[] propertiesFileLoaderClasses = properties
					.getProperty(model.getId() + ".propertiesFileLoaderClass")
						.split("\\|");
			if (propertiesFiles.length != propertiesFileLoaderClasses.length) {
				throw new EmfConfigurationException(Arrays.toString(propertiesFiles)
						+ " size does not match the loaders size: " + Arrays.toString(propertiesFileLoaderClasses));
			}
			Class<?> loaderClass;
			Properties collectiveProperties = new Properties();
			for (int i = 0; i < propertiesFiles.length; i++) {
				String nextProperties = propertiesFiles[i];
				// load next mapping - may override some values
				loaderClass = Class.forName(propertiesFileLoaderClasses[i]);
				collectiveProperties.putAll(ResourceLoadUtil.loadProperties(nextProperties, loaderClass));
			}

			Properties localCmfTOdms = new Properties();
			localCmfTOdms.putAll(collectiveProperties);
			LOGGER.debug("For model {} loaded mapping: {}", model.getId(), localCmfTOdms);
			Map<String, Set<String>> localDmsTOcmf = CollectionUtils.createHashMap(localCmfTOdms.size());
			for (Entry<Object, Object> entry : localCmfTOdms.entrySet()) {
				String value = entry.getValue().toString();
				if (!localDmsTOcmf.containsKey(value)) {
					localDmsTOcmf.put(value, new TreeSet<String>());
				} else {
					LOGGER.warn("Adding duplicate DMS->CMF value for: {} as: {}", value, entry.getKey());
				}
				localDmsTOcmf.get(value).add(entry.getKey().toString());
			}
			cmfTOdms = localCmfTOdms;
			dmsTOcmf = localDmsTOcmf;
			LOGGER.debug("For model {} loaded reverse mapping: {}", model.getId(), localDmsTOcmf);
		} catch (EmfConfigurationException e) {
			throw e;
		} catch (Exception e) {
			throw new EmfConfigurationException(e);
		}

	}

	/**
	 * Filter properties by using json and provided model. Only model elements with requiered read level passes. Default
	 * Model is used to filter
	 *
	 * @param properties
	 *            the properties
	 * @param level
	 *            the level
	 * @return the map
	 */
	@Override
	public Map<String, Serializable> filterCMFProperties(Map<String, Serializable> properties, FieldProcessor level) {
		return filterCMFProperties(getBaseModel(), properties, level);
	}

	/**
	 * Filter properties by using json and provided model. Only model elements with requiered read level passes.
	 *
	 * @param defModel
	 *            the model
	 * @param properties
	 *            the properties
	 * @param level
	 *            the level
	 * @return the map
	 */
	@Override
	public Map<String, Serializable> filterCMFProperties(DefinitionModel defModel, Map<String, Serializable> properties,
			FieldProcessor level) {
		if (defModel != null && defModel.getFields() != null) {
			// removed all non editable properties
			return defModel
					.fieldsStream()
						.map(propDef -> convertPropertyValueByFieldDefinition(properties, level, propDef))
						.filter(Pair.nonNull())
						.collect(Pair.toMap());
		}
		return new LinkedHashMap<>(1);

	}

	private Pair<String, Serializable> convertPropertyValueByFieldDefinition(Map<String, Serializable> properties,
			FieldProcessor level, PropertyDefinition propDef) {
		return processInternalProperty(propDef.getName(), properties.get(propDef.getName()), level, propDef);
	}

	/**
	 * Filter properties by using map and provided model. Only model elements with requiered read level passes.
	 *
	 * @param defModel
	 *            the model
	 * @param properties
	 *            the properties
	 * @param level
	 *            the level
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	@Override
	public Map<String, Serializable> filterDMSProperties(DefinitionModel defModel, Map<String, Serializable> properties,
			FieldProcessor level) throws JSONException {
		Map<String, Serializable> props = new LinkedHashMap<>();
		if (defModel != null && defModel.getFields() != null) {
			// removed all non editable properties
			filterDMSPropertiesInternal(defModel, properties, level, props);
			if (defModel instanceof RegionDefinitionModel) {
				for (RegionDefinition regionDefinition : ((RegionDefinitionModel) defModel).getRegions()) {
					filterDMSPropertiesInternal(regionDefinition, properties, level, props);
				}
			}
			return props;
		}
		return props;
	}

	/**
	 * Internal method. Filter properties by using map and provided model. Only model elements with required read level
	 * passes.
	 *
	 * @param defModel
	 *            the model
	 * @param properties
	 *            the properties
	 * @param level
	 *            the level
	 * @param props
	 *            the props
	 */
	private void filterDMSPropertiesInternal(DefinitionModel defModel, Map<String, Serializable> properties,
			FieldProcessor level, Map<String, Serializable> props) {
		for (PropertyDefinition definition : defModel.getFields()) {
			if (!level.hasRequiredReadLevel(definition)) {
				continue;
			}
			String preparedDMSKey = level.prepareDMSKey(definition.getDmsType());
			if (properties.containsKey(preparedDMSKey)) {
				Serializable objectValue = properties.get(preparedDMSKey);
				Pair<String, Serializable> value = processInternalDMSProperty(definition.getName(), objectValue, level,
						definition);
				if (value != null && value.getFirst() != null) {
					props.put(value.getFirst(), value.getSecond());
				}
			}
		}
	}

	/**
	 * Filter properties.
	 *
	 * @param defModel
	 *            the model
	 * @param properties
	 *            the properties
	 * @param level
	 *            the level
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	@Override
	public Map<String, Serializable> filterDMSProperties(DefinitionModel defModel, JSONObject properties,
			FieldProcessor level) throws JSONException {
		Map<String, Serializable> props = new LinkedHashMap<>();
		if (defModel != null) {
			filterDefinitionPropertiesInternal(defModel, properties, level, props);
			// if the model is of type region model then we should search for
			// the properties into the regions
			if (defModel instanceof RegionDefinitionModel) {
				RegionDefinitionModel regionModel = (RegionDefinitionModel) defModel;
				for (RegionDefinition regionDefinition : regionModel.getRegions()) {
					filterDefinitionPropertiesInternal(regionDefinition, properties, level, props);
				}
			}
			return props;
		}
		return props;
	}

	/**
	 * Filter definition properties for the given model and put them into the given map.
	 *
	 * @param defModel
	 *            the model
	 * @param properties
	 *            the properties
	 * @param level
	 *            the level
	 * @param props
	 *            the result properties
	 * @throws JSONException
	 *             the jSON exception
	 */
	private void filterDefinitionPropertiesInternal(DefinitionModel defModel, JSONObject properties,
			FieldProcessor level, Map<String, Serializable> props) throws JSONException {
		if (defModel == null || defModel.getFields() == null) {
			return;
		}
		LOGGER.trace("Filter DMStoCMFPropertiesByValue for '{}' on {}", defModel, properties);
		// removed all non editable properties
		for (PropertyDefinition definition : defModel.getFields()) {
			if (!level.hasRequiredReadLevel(definition)) {
				continue;
			}
			String preparedDMSKey = level.prepareDMSKey(definition.getDmsType());
			if (properties.has(preparedDMSKey) && !properties.isNull(preparedDMSKey)) {
				Object valueTemp = properties.get(preparedDMSKey);
				Pair<String, Serializable> value = processInternalDMSProperty(definition.getName(), valueTemp, level,
						definition);
				if (value != null && value.getFirst() != null) {
					props.put(value.getFirst(), value.getSecond());
				}
			}
		}
	}

	/**
	 * Process internal cmf property.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param level
	 *            the level
	 * @param definition
	 *            the definition
	 * @return the pair
	 */
	private Pair<String, Serializable> processInternalProperty(String key, Object value, FieldProcessor level,
			PropertyDefinition definition) {
		LOGGER.trace("CMF to DMS value: {} definition: {}", shortenToNull(value, 512), definition);
		if (!(value instanceof Serializable)) {
			return null;
		}
		if (definition != null) {
			if (level.hasRequiredLevel(definition)) {
				return level.processToDMSPassed(definition, (Serializable) value, this);
			}
			return level.processToDMSSkipped(definition, (Serializable) value, this);
		}
		return level.processToDmsNoDefinition(key, (Serializable) value, this);
	}

	/**
	 * Process internal cmf property. here field should be allowed for conversion.
	 *
	 * @param key
	 *            the key associated with the value
	 * @param value
	 *            the value to process
	 * @param level
	 *            the level of processing
	 * @param definition
	 *            the definition to use for converting
	 * @return the pair of converted value and key
	 */
	private Pair<String, Serializable> processInternalDMSProperty(String key, Object value, FieldProcessor level,
			PropertyDefinition definition) {
		Object valueLocal = value;
		LOGGER.trace("DMS to CMF value: {} definition: {}", shortenToNull(value, 512), definition);
		if (!(valueLocal instanceof Serializable)) {

			if (valueLocal instanceof JSONArray) {
				valueLocal = typeConverter.convert(List.class, valueLocal);
			} else {
				LOGGER.warn("Skipping not Serializable value: {} for key: {}", shortenToNull(valueLocal, 512), key);
				return null;
			}
		}
		if (definition != null) {
			return level.processToCMFPassed(definition, (Serializable) valueLocal, this);
		}
		LOGGER.trace("Skipping no definition fields: {}", key);
		return level.processToCmfNoDefinition(key, (Serializable) valueLocal, this);
	}

	/**
	 * Gets the property definition by name.
	 *
	 * @param field
	 *            the field
	 * @param defModel
	 *            the model
	 * @return the property definition by name
	 */
	private static PropertyDefinition getPropertyDefinitionByName(String field, DefinitionModel defModel) {
		if (defModel == null) {
			return null;
		}
		Node child = defModel.getChild(field);
		if (child instanceof PropertyDefinition) {
			return (PropertyDefinition) child;
		}
		// REVIEW:BB: the next code is probably obsolete but could help only if
		// there is a field
		// with name as for example the same name of a transition
		// REVIEW: probably good idea is to move the whole method to PathHelper
		return PathHelper.findProperty(defModel, (PathElement) defModel, field);
	}

	/**
	 * Update cached model.
	 *
	 * @param modelKey
	 *            the model key
	 * @param defModel
	 *            the model
	 * @param cache
	 *            the cache to update
	 * @return the mirror model data
	 */
	private static Map<String, PropertyDefinition> updateCachedModel(String modelKey, DefinitionModel defModel,
			EntityLookupCache<String, Map<String, PropertyDefinition>, Serializable> cache) {
		int size = defModel.getFields().size();
		if (defModel instanceof RegionDefinitionModel) {
			RegionDefinitionModel regionDefinitionModel = (RegionDefinitionModel) defModel;
			for (RegionDefinition definition : regionDefinitionModel.getRegions()) {
				size += definition.getFields().size();
			}
		}
		Map<String, PropertyDefinition> mirrorModelData = CollectionUtils.createHashMap(size);

		updateCacheModel(defModel, mirrorModelData);
		if (defModel instanceof RegionDefinitionModel) {
			RegionDefinitionModel regionDefinitionModel = (RegionDefinitionModel) defModel;
			for (RegionDefinition definition : regionDefinitionModel.getRegions()) {
				updateCacheModel(definition, mirrorModelData);
			}
		}

		cache.setValue(modelKey, mirrorModelData);
		return mirrorModelData;
	}

	/**
	 * Update cache model.
	 *
	 * @param defModel
	 *            the model
	 * @param mirrorModelData
	 *            the mirror model data
	 */
	private static void updateCacheModel(DefinitionModel defModel, Map<String, PropertyDefinition> mirrorModelData) {
		for (PropertyDefinition definition : defModel.getFields()) {
			String dmsType = definition.getDmsType();
			if (FieldProcessor.FORBIDDEN.equals(dmsType)) {
				continue;
			}
			if (dmsType.indexOf('-') == 0) {
				dmsType = dmsType.substring(1);
			}
			LOGGER.trace("Adding mirror {} already contained: {}", definition.getName(), mirrorModelData.get(dmsType));
			// this is fix for the problem when some time there is a problem
			// with the cast to
			// propertyDefinition
			// separate the instance from the DB proxy
			if (definition instanceof PropertyDefinitionProxy) {
				mirrorModelData.put(dmsType, ((PropertyDefinitionProxy) definition).createCopy());
			} else {
				LOGGER.warn("WARNING: The property definition is of not recognized class: {} {}", definition.getClass(),
						Arrays.asList(definition.getClass().getGenericInterfaces()));
				// if not for some reason - this should not happen
				mirrorModelData.put(dmsType, definition);
			}
		}
	}

	/**
	 * Gets the dMS property definition by name.
	 *
	 * @param field
	 *            the field
	 * @param defModel
	 *            the model
	 * @return the dMS property definition by name
	 */
	private PropertyDefinition getDMSPropertyDefinitionByName(String field, DefinitionModel defModel) {
		String modelKey;
		if (defModel.getHash() == null) {
			modelKey = hashCalculator.computeHash(defModel).toString();
		} else {
			modelKey = defModel.getHash().toString();
		}
		EntityLookupCache<String, Map<String, PropertyDefinition>, Serializable> cache = getMirrorCache();
		Map<String, PropertyDefinition> map = cache.getValue(modelKey);
		if (map == null) {
			map = updateCachedModel(modelKey, defModel, cache);
		}
		PropertyDefinition propertyDefinition = map.get(field);
		LOGGER.trace("DMSPropertyDefinitionByName ({}) property is {}", field, propertyDefinition);
		return propertyDefinition;
	}

	/**
	 * Convert cmf to dms property.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param processor
	 *            the processor
	 * @return the pair of converted key->value
	 */
	@Override
	public Pair<String, Serializable> convertCMFtoDMSProperty(String key, Serializable value,
			FieldProcessor processor) {
		return convertCMFtoDMSProperty(getBaseModel(), key, value, processor);
	}

	/**
	 * Convert cmf to dms property.
	 *
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param instance
	 *            the instance to use as model base
	 * @param processor
	 *            the processor to use
	 * @return the pair of converted key->value
	 */
	@Override
	public Pair<String, Serializable> convertCMFtoDMSProperty(String key, Serializable value, Instance instance,
			FieldProcessor processor) {
		DefinitionModel modelId = definitionService.getInstanceDefinition(instance);
		if (modelId == null) {
			modelId = getBaseModel();
		}
		return convertCMFtoDMSProperty(modelId, key, value, processor);
	}

	/**
	 * Convert cmf to dms property.
	 *
	 * @param defModel
	 *            the model
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param level
	 *            the level
	 * @return the pair
	 */
	private Pair<String, Serializable> convertCMFtoDMSProperty(DefinitionModel defModel, String key, Serializable value,
			FieldProcessor level) {
		if (key == null || value == null) {
			return null;
		}
		PropertyDefinition propertyDef = getPropertyDefinitionByName(key, defModel);
		if (propertyDef != null) {
			if (level.hasRequiredLevel(propertyDef)) {
				return level.processToDMSPassed(propertyDef, value, this);
			}
			return level.processToDMSSkipped(propertyDef, value, this);
		}
		return level.processToDmsNoDefinition(key, value, this);
	}

	/**
	 * Convert cmf to dms properties.
	 *
	 * @param properties
	 *            the sort args
	 * @param processor
	 *            the filter model
	 * @return the map
	 */
	@Override
	public Map<String, Serializable> convertCMFtoDMSProperties(Map<String, Serializable> properties,
			FieldProcessor processor) {
		return filterCMFProperties(getBaseModel(), properties, processor);
	}

	/**
	 * Convert cmf to dms properties.
	 *
	 * @param properties
	 *            the properties
	 * @param instance
	 *            the current instance element
	 * @param level
	 *            the level of processing
	 * @return the map of processed properties
	 */
	@Override
	public Map<String, Serializable> convertCMFtoDMSProperties(Map<String, Serializable> properties, Instance instance,
			FieldProcessor level) {
		return convertCMFtoDMSProperties(properties, instance, null, level);
	}

	/**
	 * Convert cmf to dms properties.
	 *
	 * @param properties
	 *            the properties
	 * @param instance
	 *            the current instance element
	 * @param definition
	 *            the definition
	 * @param level
	 *            the level of processing
	 * @return the map of processed properties
	 */
	@Override
	public Map<String, Serializable> convertCMFtoDMSProperties(Map<String, Serializable> properties, Instance instance,
			DefinitionModel definition, FieldProcessor level) {
		DefinitionModel modelId = definition;
		if (modelId == null) {
			modelId = definitionService.getInstanceDefinition(instance);
		}
		if (modelId == null) {
			modelId = getBaseModel();
		}
		return filterCMFProperties(modelId, properties, level);
	}

	/**
	 * Convert dms to cmf properties.
	 *
	 * @param jsonObject
	 *            the json object
	 * @param instance
	 *            the task path element
	 * @param level
	 *            the level
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	@Override
	public Map<String, Serializable> convertDMSToCMFProperties(JSONObject jsonObject, Instance instance,
			FieldProcessor level) throws JSONException {
		DefinitionModel modelId = definitionService.getInstanceDefinition(instance);
		if (modelId == null) {
			modelId = getBaseModel();
		}
		return filterDMSProperties(modelId, jsonObject, level);
	}

	/**
	 * Convert dms to cmf properties.
	 *
	 * @param props
	 *            the props
	 * @param instance
	 *            the task path element
	 * @param level
	 *            the level
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	@Override
	public Map<String, Serializable> convertDMSToCMFProperties(Map<String, Serializable> props, Instance instance,
			FieldProcessor level) throws JSONException {
		// take into count the revision
		DefinitionModel modelId = definitionService.getInstanceDefinition(instance);
		if (modelId == null) {
			modelId = getBaseModel();
		}
		return filterDMSProperties(modelId, props, level);

	}

	/**
	 * Convert dms to cmf properties.
	 *
	 * @param jsonObject
	 *            the json object
	 * @param processor
	 *            the processor
	 * @return the map
	 */
	@Override
	public Map<String, Serializable> convertDMSToCMFProperties(JSONObject jsonObject, FieldProcessor processor) {
		try {
			return filterDMSProperties(getBaseModel(), jsonObject, processor);
		} catch (Exception e) {
			throw new EmfRuntimeException(AlfrescoErrorReader.parse(e));
		}
	}

	/**
	 * Convert dms to cmf single property.
	 *
	 * @param dmsKey
	 *            is the dms key
	 * @param objectValue
	 *            is the value to set to the key
	 * @param level
	 *            is the required level processor. Most probably {@link #PROPERTIES_MAPPING}
	 * @return the pair of converted key, value or null if no mapping is available
	 */
	@Override
	public Pair<String, Serializable> convertDMSToCMFProperty(String dmsKey, Serializable objectValue,
			FieldProcessor level) {
		Pair<String, Serializable> value = processInternalDMSProperty(dmsKey, objectValue, level, null);
		if (value != null && !Pair.NULL_PAIR.equals(value)) {
			return value;
		}
		return null;
	}

	/**
	 * Convert cmf to dms properties by value.<br>
	 * REVIEW: the name of the method should be changed to reflect that we a using the base model for conversion
	 *
	 * @param properties
	 *            the properties
	 * @param processor
	 *            the processor
	 * @return the map
	 */
	@Override
	public Map<String, Serializable> convertCMFtoDMSPropertiesByValue(Map<String, Serializable> properties,
			FieldProcessor processor) {
		Map<String, Serializable> props = new LinkedHashMap<>(properties.size());

		if (getBaseModel() != null && getBaseModel().getFields() != null) {
			// TODO when model is cached do the opposite iteration
			Set<Entry<String, Serializable>> entrySet = properties.entrySet();
			for (Entry<String, Serializable> entry : entrySet) {
				PropertyDefinition definition = getPropertyDefinitionByName(entry.getKey(), getBaseModel());
				Pair<String, Serializable> value = processInternalProperty(entry.getKey(), entry.getValue(),
						processor, definition);
				if (value != null && value.getFirst() != null) {
					props.put(value.getFirst(), value.getSecond());
				}
			}

			return props;
		}
		return props;
	}

	/**
	 * Convert dms to cmf properties by value - iteration is done over the value not the model itself
	 *
	 * @param properties
	 *            the properties
	 * @param instance
	 *            the instance
	 * @param level
	 *            the level
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	@Override
	public Map<String, Serializable> convertDMStoCMFPropertiesByValue(JSONObject properties, Instance instance,
			FieldProcessor level) throws JSONException {
		DefinitionModel modelId = definitionService.getInstanceDefinition(instance);
		if (modelId == null) {
			modelId = getBaseModel();
		}
		LOGGER.trace("DMStoCMFPropertiesByValue for '{}' on {}", modelId, properties);
		if (modelId == null || modelId.getFields() == null) {
			return new LinkedHashMap<>(1);
		}
		// TODO when model is cached do the opposite iteration
		DefinitionModel localModel = modelId;
		return JsonUtil
				.getKeyStream(properties)
					.map(level::prepareDMSKey)
					.filter(modelKey -> properties.has(modelKey) && !properties.isNull(modelKey))
					.map(modelKey -> {
						PropertyDefinition definition = getDMSPropertyDefinitionByName(modelKey, localModel);
						if (!level.hasRequiredReadLevel(definition)) {
							return null;
						}
						Object valueOrNull = JsonUtil.getValueOrNull(properties, modelKey);
						return processInternalDMSProperty(definition.getName(), valueOrNull, level, definition);
					})
					.filter(pair -> pair != null && pair.getFirst() != null)
					.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
	}

	/**
	 * Gets the model prefix.
	 *
	 * @return the model prefix
	 */
	@Override
	public String getModelPrefix() {
		if (modelPrefix == null) {
			modelPrefix = convertorProperties.get().getProperty(model.getId() + ".modelPrefix");
		}
		return modelPrefix;
	}

	/**
	 * Gets the cmf to dms mapping properties
	 *
	 * @return the cmf to dms mapping properties loaded from the provided config
	 */
	protected Properties getCMFtoDMSMapping() {
		if (cmfTOdms == null) {
			loadPropertiesMappings();
		}
		return cmfTOdms;
	}

	/**
	 * Gets the cmf to dms reverse mapping
	 *
	 * @return the cmf to dms reverse mapping properties
	 */
	protected Map<String, Set<String>> getDMStoCMFMapping() {
		if (dmsTOcmf == null) {
			loadPropertiesMappings();
		}
		return dmsTOcmf;
	}

	private DefinitionModel getBaseModel() {
		return baseModelSupplier.get();
	}

	@Override
	public void reset() {
		modelPrefix = null;
		loadPropertiesMappings();
		getMirrorCache().clear();
	}
}
