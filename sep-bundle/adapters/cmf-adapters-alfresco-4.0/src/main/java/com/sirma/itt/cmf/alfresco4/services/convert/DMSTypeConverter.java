package com.sirma.itt.cmf.alfresco4.services.convert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.cmf.alfresco4.AlfrescoCommunicationConstants;
import com.sirma.itt.cmf.alfresco4.AlfrescoErrorReader;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.cache.lookup.NoEntityLookup;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.hash.HashCalculator;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.PathHelper;
import com.sirma.itt.emf.util.ResourceLoadUtil;

/**
 * The Class DMSTypeConverter is custom converter for specific models. <br>
 * REVIEW: why not separate the converter into 2 one for CMF->DMS and one DMS->CMF so we can have
 * more clear implementation with common interface/abstract class
 */
@SuppressWarnings("unchecked")
public class DMSTypeConverter implements AlfrescoCommunicationConstants {

	/**
	 * The class ConvertibleModels holds the possible models converters as dynamic reference to id
	 * of the model.
	 */
	static class ConvertibleModels {

		/** The id. */
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

		/**
		 * Gets the id.
		 *
		 * @return the id
		 */
		public String getId() {
			return id;
		}

		/**
		 * Sets the id.
		 *
		 * @param id
		 *            the id to set
		 */
		public void setId(String id) {
			this.id = id;
		}
	}

	//
	/** the ISO date format pattern. */
	private static final Pattern ISODATE_REGEX = Pattern
			.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}");
	/** the logger. */
	private static final Logger LOGGER = Logger.getLogger(DMSTypeConverter.class);
	/** The Constant debug. */
	private static final boolean debug = LOGGER.isDebugEnabled();

	/** The Constant trace. */
	private static final boolean trace = LOGGER.isTraceEnabled();

	/** The type converter. */
	private TypeConverter typeConverter;
	/** The dictionary service. */
	private DictionaryService dictionaryService;

	/** The model. */
	private ConvertibleModels model;
	/** elevates converting. */
	private ExpressionsManager evaluatorManager;

	/** The convertor properties. */
	private Properties convertorProperties;

	/** mapping to dms properties. */
	private Properties cmfTOdms;
	/** The mirror mapping. */
	private HashMap<String, Set<String>> dmsTOcmf;

	/** The model prefix. */
	private String modelPrefix;

	/** The base model. */
	private DefinitionModel baseModel;

	/** The cache name. */
	private String cacheName;

	/** The Constant ALLOW_ALL. */
	public static final FieldProcessor ALLOW_ALL = new FieldProcessorAllowedAll();
	/** The Constant ALLOW_ALL. */
	public static final FieldProcessor PROPERTIES_MAPPING = new FieldProcessorFromMapping();
	/** The Constant EDITABLE_OR_MANDATORY_LEVEL. */
	public static final FieldProcessor EDITABLE_HIDDEN_MANDATORY_LEVEL = new FieldProcessorWritable();
	/** The Constant EDITABLE_OR_MANDATORY_LEVEL. */
	public static final FieldProcessor WORKFLOW_TASK_LEVEL = new FieldProcessorBPMWritable();
	/** The Constant ALLOW_WITH_PREFIX. */
	public static final FieldProcessor ALLOW_WITH_PREFIX = new FieldProcessorAllowedAllWithPrefix();
	/** document version level. */
	public static final FieldProcessor DOCUMENT_LEVEL = new FieldProcessorFromMapping() {
		@Override
		public String prepareDMSKey(String key) {
			// return the original key
			if (key.indexOf('-') == 0) {
				return key.substring(1);
			}
			return key;
		}
	};

	/** The Constant CACHE_PREFIX. */
	private static final String CACHE_PREFIX = "DMS_TYPE_CACHE_FOR_";

	/** The cache context. */
	private EntityLookupCacheContext cacheContext;

	private HashCalculator hashCalculator;

	/**
	 * Instantiates a new dMS type converter.
	 *
	 * @param typeConverter
	 *            the type converter
	 * @param dictionaryService
	 *            the dictionary service
	 * @param evaluatorManager
	 *            the evaluator manager
	 * @param model
	 *            the model
	 * @param cacheContext
	 *            the cache provider
	 * @param convertorProperties
	 *            is the localtion for convertor config
	 */
	public DMSTypeConverter(TypeConverter typeConverter, DictionaryService dictionaryService,
			ExpressionsManager evaluatorManager, ConvertibleModels model,
			EntityLookupCacheContext cacheContext, Properties convertorProperties) {
		this.typeConverter = typeConverter;
		this.dictionaryService = dictionaryService;
		this.evaluatorManager = evaluatorManager;
		this.model = model;
		this.cacheContext = cacheContext;
		this.convertorProperties = convertorProperties;
		loadConvertor();
		loadPropertiesMappings();
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
		return new Pair<String, Serializable>(modelPrefix + name, value);
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
	protected Pair<String, Serializable> convertDMSToCMFPropertyInernal(
			PropertyDefinition definition, Serializable value) {

		Serializable valueReturned = null;
		if (definition.isMultiValued() && (value instanceof Collection)) {
			ArrayList<Object> valueReturnedList = new ArrayList<>(
					((Collection<Object>) value).size());
			for (Object next : (Collection<Object>) value) {
				valueReturnedList.add(typeConverter.convert(definition.getDataType(), next));
			}
			valueReturned = valueReturnedList;
		} else {
			// convert to proper value type
			valueReturned = (Serializable) typeConverter.convert(definition.getDataType(), value);
		}
		valueReturned = evaluatorManager.ruleConvertFrom(definition, valueReturned);
		return new Pair<String, Serializable>(definition.getName(), valueReturned);
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
	protected Pair<String, Serializable> doConvertInternalToDMS(PropertyDefinition definition,
			Serializable value) {
		int indexOfSkippable = definition.getDmsType().indexOf("-");
		if (indexOfSkippable == 0) {
			indexOfSkippable = 1;
		} else {
			indexOfSkippable = 0;
		}
		Pair<String, Serializable> pair = new Pair<String, Serializable>(definition.getDmsType()
				.substring(indexOfSkippable), null);
		if (definition.isMultiValued()) {
			Serializable serializable = evaluatorManager.ruleConvertTo(definition, value);
			if (serializable instanceof Collection) {
				pair.setSecond(typeConverter.convert(String.class, serializable));
			} else {
				pair.setSecond(serializable);
			}
		} else {
			pair.setSecond(typeConverter.convert(String.class,
					evaluatorManager.ruleConvertTo(definition, value)));
		}
		return pair;
	}

	/**
	 * Convert property valuet to cmf specific.
	 *
	 * @param definition
	 *            the definition
	 * @param serializable
	 *            the serializable
	 * @return the serializable
	 */
	protected Pair<String, Serializable> doConvertInternalToCMF(PropertyDefinition definition,
			Serializable serializable) {
		if (serializable == null) {
			return null;
		}
		Pair<String, Serializable> pair = new Pair<String, Serializable>(definition.getName(), null);
		if ("dateTime".equals(definition.getType())
				&& ISODATE_REGEX.matcher(serializable.toString()).find()) {
			pair.setSecond(typeConverter.convert(java.util.Date.class, serializable.toString()));
		} else {
			pair.setSecond(evaluatorManager.ruleConvertTo(definition, serializable));
		}
		if (trace) {
			LOGGER.trace("Converted property value " + definition.getName() + " from "
					+ serializable + " to " + pair.getSecond());
		}
		pair.setSecond((Serializable) typeConverter.convert(definition.getDataType(),
				pair.getSecond()));
		return pair;
	}

	/**
	 * Load converter properties and initialize converter params.
	 */
	private void loadConvertor() {

		// load converter data
		String baseId = convertorProperties.getProperty(model.getId() + ".baseDefinitionId");
		modelPrefix = convertorProperties.getProperty(model.getId() + ".modelPrefix");
		String definitionClassName = null;
		try {
			// load dynamically the definition class
			definitionClassName = convertorProperties.getProperty(model.getId()
					+ ".definitionClass");

			Class<? extends DefinitionModel> definitionClass = (Class<? extends DefinitionModel>) Class
					.forName(definitionClassName);
			baseModel = dictionaryService.getDefinition(definitionClass, baseId);
			// load the cache on valid model
			cacheName = CACHE_PREFIX + model.getId().toUpperCase();
			if (!cacheContext.containsCache(cacheName)) {
				cacheContext.createCache(cacheName, new NoEntityLookup());
			}
		} catch (Exception e) {
			if (!ConverterConstants.GENERAL.equalsIgnoreCase(model.getId())) {
				LOGGER.warn("Failed to find definition class for model: " + model.getId()
						+ " bind to class " + definitionClassName, e);
				throw new EmfRuntimeException(e);
			}
		}

	}

	/**
	 * Loads the default mapping for cmf properties to dms properties. Throws
	 * {@link EmfConfigurationException} on any error or unexpected values
	 */
	private void loadPropertiesMappings() {

		try {
			// do a loading mapping by mapping
			String[] propertiesFiles = convertorProperties.getProperty(
					model.getId() + ".propertiesFile").split("\\|");
			String[] propertiesFileLoaderClasses = convertorProperties.getProperty(
					model.getId() + ".propertiesFileLoaderClass").split("\\|");
			if (propertiesFiles.length != propertiesFileLoaderClasses.length) {
				throw new EmfConfigurationException(Arrays.toString(propertiesFiles)
						+ " size does not match the loaders size: "
						+ Arrays.toString(propertiesFileLoaderClasses));
			}
			Class<?> loaderClass = null;
			Properties collectiveProperties = new Properties();
			for (int i = 0; i < propertiesFiles.length; i++) {
				String nextProperties = propertiesFiles[i];
				// load next mapping - may override some values
				loaderClass = Class.forName(propertiesFileLoaderClasses[i]);
				collectiveProperties.putAll(ResourceLoadUtil.loadProperties(nextProperties,
						loaderClass));
			}

			cmfTOdms = new Properties();
			cmfTOdms.putAll(collectiveProperties);
			if (debug) {
				LOGGER.debug("For model " + model.getId() + " loaded mapping: " + cmfTOdms);
			}
			dmsTOcmf = new HashMap<String, Set<String>>(cmfTOdms.size());
			Set<Object> keySet = cmfTOdms.keySet();
			for (Object object : keySet) {
				String value = cmfTOdms.get(object).toString();
				if (!dmsTOcmf.containsKey(value)) {
					dmsTOcmf.put(value, new TreeSet<String>());
				} else {
					LOGGER.warn("Adding duplicate DMS->CMF value for: " + value + " as: " + object);
				}
				dmsTOcmf.get(value).add(object.toString());
			}
			if (debug) {
				LOGGER.debug("For model " + model.getId() + " loaded reverse mapping: " + dmsTOcmf);
			}
		} catch (EmfConfigurationException e) {
			throw e;
		} catch (Exception e) {
			throw new EmfConfigurationException(e);
		}

	}

	/**
	 * Filter properties by using json and provided model. Only model elements with requiered read
	 * level passes. Default Model is used to filter
	 *
	 * @param properties
	 *            the properties
	 * @param level
	 *            the level
	 * @return the map
	 */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, Serializable> filterCMFProperties(Map<String, Serializable> properties,
			FieldProcessor level) {
		return filterCMFProperties(baseModel, properties, level);
	}

	/**
	 * Filter properties by using json and provided model. Only model elements with requiered read
	 * level passes.
	 *
	 * @param model
	 *            the model
	 * @param properties
	 *            the properties
	 * @param level
	 *            the level
	 * @return the map
	 */

	public Map<String, Serializable> filterCMFProperties(DefinitionModel model,
			Map<String, Serializable> properties, FieldProcessor level) {
		Map<String, Serializable> props = new LinkedHashMap<String, Serializable>(properties.size());
		if ((model != null) && (model.getFields() != null)) {
			// removed all non editable properties
			// TODO when model is cached do the opposite iteration
			for (PropertyDefinition definition : model.getFields()) {
				Serializable objectValue = properties.get(definition.getName());
				Pair<String, Serializable> value = processInternalCMFPropery(definition.getName(),
						objectValue, level, definition);
				if ((value != null) && (value.getFirst() != null)) {
					props.put(value.getFirst(), value.getSecond());
				}
			}
			if (model instanceof RegionDefinitionModel) {
				for (RegionDefinition regionDefinition : ((RegionDefinitionModel) model)
						.getRegions()) {
					for (PropertyDefinition definition : regionDefinition.getFields()) {
						Serializable objectValue = properties.get(definition.getName());
						Pair<String, Serializable> value = processInternalCMFPropery(
								definition.getName(), objectValue, level, definition);
						if ((value != null) && (value.getFirst() != null)) {
							props.put(value.getFirst(), value.getSecond());
						}
					}
				}
			}
			return props;
		}
		return props;
	}

	/**
	 * Filter properties by using map and provided model. Only model elements with requiered read
	 * level passes.
	 *
	 * @param model
	 *            the model
	 * @param properties
	 *            the properties
	 * @param level
	 *            the level
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, Serializable> filterDMSProperties(DefinitionModel model,
			Map<String, Serializable> properties, FieldProcessor level) throws JSONException {
		Map<String, Serializable> props = new LinkedHashMap<String, Serializable>();
		if ((model != null) && (model.getFields() != null)) {
			// removed all non editable properties
			filterDMSPropertiesInternal(model, properties, level, props);
			if (model instanceof RegionDefinitionModel) {
				for (RegionDefinition regionDefinition : ((RegionDefinitionModel) model)
						.getRegions()) {
					filterDMSPropertiesInternal(regionDefinition, properties, level, props);
				}
			}
			return props;
		}
		return props;
	}

	/**
	 * Internal method. Filter properties by using map and provided model. Only model elements with
	 * required read level passes.
	 *
	 * @param model
	 *            the model
	 * @param properties
	 *            the properties
	 * @param level
	 *            the level
	 * @param props
	 *            the props
	 */
	private void filterDMSPropertiesInternal(DefinitionModel model,
			Map<String, Serializable> properties, FieldProcessor level,
			Map<String, Serializable> props) {
		for (PropertyDefinition definition : model.getFields()) {
			if (!level.hasRequiredReadLevel(definition)) {
				continue;
			}
			String preparedDMSKey = level.prepareDMSKey(definition.getDmsType());
			if (!properties.containsKey(preparedDMSKey)) {
				continue;
			}
			Serializable objectValue = properties.get(preparedDMSKey);
			Pair<String, Serializable> value = processInternalDMSProperty(definition.getName(),
					objectValue, level, definition);
			if ((value != null) && (value.getFirst() != null)) {
				props.put(value.getFirst(), value.getSecond());
			}
		}
	}

	/**
	 * Filter properties.
	 *
	 * @param model
	 *            the model
	 * @param properties
	 *            the properties
	 * @param level
	 *            the level
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Map<String, Serializable> filterDMSProperties(DefinitionModel model,
			JSONObject properties, FieldProcessor level) throws JSONException {
		Map<String, Serializable> props = new LinkedHashMap<String, Serializable>();
		if (model != null) {
			filterDefinitionPropertiesInternal(model, properties, level, props);
			// if the model is of type region model then we should search for
			// the properties into the regions
			if (model instanceof RegionDefinitionModel) {
				RegionDefinitionModel regionModel = (RegionDefinitionModel) model;
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
	 * @param model
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
	private void filterDefinitionPropertiesInternal(DefinitionModel model, JSONObject properties,
			FieldProcessor level, Map<String, Serializable> props) throws JSONException {
		if ((model == null) || (model.getFields() == null)) {
			return;
		}
		if (trace) {
			LOGGER.trace("Filter DMStoCMFPropertiesByValue for '" + model + "' on " + properties);
		}
		// removed all non editable properties
		for (PropertyDefinition definition : model.getFields()) {
			if (!level.hasRequiredReadLevel(definition)) {
				continue;
			}
			String preparedDMSKey = level.prepareDMSKey(definition.getDmsType());
			if (!properties.has(preparedDMSKey) || properties.isNull(preparedDMSKey)) {
				LOGGER.warn("Not found value for: " + preparedDMSKey);
				continue;
			}
			Object valueTemp = properties.get(preparedDMSKey);
			Pair<String, Serializable> value = processInternalDMSProperty(definition.getName(),
					valueTemp, level, definition);
			if ((value != null) && (value.getFirst() != null)) {
				props.put(value.getFirst(), value.getSecond());
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
	private Pair<String, Serializable> processInternalCMFPropery(String key, Object value,
			FieldProcessor level, PropertyDefinition definition) {
		if (trace) {
			LOGGER.trace("CMF to DMS value: " + value + " definition: " + definition);
		}
		if (!(value instanceof Serializable)) {
			return null;
		}
		if (definition != null) {
			if (level.hasRequiredLevel(definition)) {
				return level.processToDMSPassed(definition, (Serializable) value, this);
			} else {
				return level.processToDMSSkipped(definition, (Serializable) value, this);
			}
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
	private Pair<String, Serializable> processInternalDMSProperty(String key, Object value,
			FieldProcessor level, PropertyDefinition definition) {
		Object valueLocal = value;
		if (trace) {
			LOGGER.trace("DMS to CMF value: " + valueLocal + " definition: " + definition);
		}
		if (!(valueLocal instanceof Serializable)) {

			if (valueLocal instanceof JSONArray) {
				valueLocal = typeConverter.convert(List.class, valueLocal);
			} else {
				LOGGER.warn("Skipping not Serializable value: " + valueLocal + " for key: " + key);
				return null;
			}
		}
		if (definition != null) {
			return level.processToCMFPassed(definition, (Serializable) valueLocal, this);
		}
		if (trace) {
			LOGGER.trace("Skipping no definition fields: " + key);
		}
		return level.processToCmfNoDefinition(key, (Serializable) valueLocal, this);
	}

	/**
	 * Gets the property definition by name.
	 *
	 * @param field
	 *            the field
	 * @param model
	 *            the model
	 * @return the property definition by name
	 */
	private PropertyDefinition getPropertyDefinitionByName(String field, DefinitionModel model) {
		if (model == null) {
			return null;
		}
		Node child = model.getChild(field);
		if (child instanceof PropertyDefinition) {
			return (PropertyDefinition) child;
		}
		// REVIEW:BB: the next code is probably obsolete but could help only if
		// there is a field
		// with name as for example the same name of a transition
		// REVIEW: probably good idea is to move the whole method to PathHelper
		PropertyDefinition definition = PathHelper.find(model.getFields(), field);
		if (definition != null) {
			return definition;
		}
		if (model instanceof RegionDefinitionModel) {
			for (RegionDefinition regionDefinition : ((RegionDefinitionModel) model).getRegions()) {
				definition = PathHelper.find(regionDefinition.getFields(), field);
				if (definition != null) {
					return definition;
				}
			}
		}
		return null;
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
	private Map<String, PropertyDefinition> updateCachedModel(String modelKey,
			DefinitionModel defModel,
			EntityLookupCache<String, Map<String, PropertyDefinition>, Serializable> cache) {
		int size = defModel.getFields().size();
		if (defModel instanceof RegionDefinitionModel) {
			RegionDefinitionModel regionDefinitionModel = (RegionDefinitionModel) defModel;
			for (RegionDefinition definition : regionDefinitionModel.getRegions()) {
				size += definition.getFields().size();
			}
		}
		Map<String, PropertyDefinition> mirrorModelData = new HashMap<String, PropertyDefinition>(
				(int) (size * 1.1), 0.95f);

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
	 * @param model
	 *            the model
	 * @param mirrorModelData
	 *            the mirror model data
	 */
	private void updateCacheModel(DefinitionModel model,
			Map<String, PropertyDefinition> mirrorModelData) {
		for (PropertyDefinition definition : model.getFields()) {
			String dmsType = definition.getDmsType();
			if (FieldProcessor.FORBIDDEN.equals(dmsType)) {
				continue;
			}
			if (dmsType.indexOf("-") == 0) {
				dmsType = dmsType.substring(1);
			}
			// REVIEW: maybe we can move this to trace now
			if (debug) {
				LOGGER.debug("Adding mirror " + definition.getName() + " already contained: "
						+ mirrorModelData.get(dmsType));
			}
			// this is fix for the problem when some time there is a problem
			// with the cast to
			// propertyDefinition
			// separate the instance from the DB proxy
			if (definition instanceof PropertyDefinitionProxy) {
				mirrorModelData.put(dmsType, ((PropertyDefinitionProxy) definition).clone());
			} else {
				LOGGER.warn("WARNING: The property definition is of not recognized class: "
						+ definition.getClass() + " "
						+ Arrays.asList(definition.getClass().getGenericInterfaces()));
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
		String modelKey = null;
		if (defModel.getHash() == null) {
			modelKey = Integer.valueOf(hashCalculator.computeHash(defModel)).toString();
		} else {
			modelKey = Integer.valueOf(defModel.getHash()).toString();
		}
		EntityLookupCache<String, Map<String, PropertyDefinition>, Serializable> cache = getMirrorCache();
		Map<String, PropertyDefinition> map = cache.getValue(modelKey);
		if (map == null) {
			map = updateCachedModel(modelKey, defModel, cache);
		}
		PropertyDefinition propertyDefinition = map.get(field);
		if (trace) {
			LOGGER.trace("DMSPropertyDefinitionByName (" + field + ") property is "
					+ propertyDefinition);
		}
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
	public Pair<String, Serializable> convertCMFtoDMSProperty(String key, Serializable value,
			FieldProcessor processor) {
		return convertCMFtoDMSProperty(baseModel, key, value, processor);
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
	public Pair<String, Serializable> convertCMFtoDMSProperty(String key, Serializable value,
			Instance instance, FieldProcessor processor) {
		DefinitionModel modelId = dictionaryService.getInstanceDefinition(instance);
		if (modelId == null) {
			modelId = baseModel;
		}
		return convertCMFtoDMSProperty(modelId, key, value, processor);
	}

	/**
	 * Convert cmf to dms property.
	 *
	 * @param model
	 *            the model
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param level
	 *            the level
	 * @return the pair
	 */
	private Pair<String, Serializable> convertCMFtoDMSProperty(DefinitionModel model, String key,
			Serializable value, FieldProcessor level) {
		if ((key == null) || (value == null)) {
			return null;
		}
		PropertyDefinition propertyDef = getPropertyDefinitionByName(key, model);
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
	public Map<String, Serializable> convertCMFtoDMSProperties(
			Map<String, Serializable> properties, FieldProcessor processor) {
		return filterCMFProperties(baseModel, properties, processor);
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
	public Map<String, Serializable> convertCMFtoDMSProperties(
			Map<String, Serializable> properties, Instance instance, FieldProcessor level) {
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
	public Map<String, Serializable> convertCMFtoDMSProperties(
			Map<String, Serializable> properties, Instance instance, DefinitionModel definition,
			FieldProcessor level) {
		DefinitionModel modelId = definition;
		if (modelId == null) {
			modelId = dictionaryService.getInstanceDefinition(instance);
		}
		if (modelId == null) {
			modelId = baseModel;
		}
		return filterCMFProperties(modelId, properties, level);
	}

	/**
	 * Convert dms to cmf properties.
	 *
	 * @param jsonObject
	 *            the json object
	 * @param revision
	 *            the revision
	 * @param taskPathElement
	 *            the task path element
	 * @param level
	 *            the level
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	public Map<String, Serializable> convertDMSToCMFProperties(JSONObject jsonObject,
			Long revision, Instance taskPathElement, FieldProcessor level) throws JSONException {
		DefinitionModel modelId = dictionaryService.getInstanceDefinition(taskPathElement);
		if (modelId == null) {
			modelId = baseModel;
		}
		return filterDMSProperties(modelId, jsonObject, level);
	}

	/**
	 * Convert dms to cmf properties.
	 *
	 * @param props
	 *            the props
	 * @param revision
	 *            the revision
	 * @param taskPathElement
	 *            the task path element
	 * @param level
	 *            the level
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	public Map<String, Serializable> convertDMSToCMFProperties(Map<String, Serializable> props,
			Long revision, Instance taskPathElement, FieldProcessor level) throws JSONException {
		// take into count the revision
		DefinitionModel modelId = dictionaryService.getInstanceDefinition(taskPathElement);
		if (modelId == null) {
			modelId = baseModel;
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
	public Map<String, Serializable> convertDMSToCMFProperties(JSONObject jsonObject,
			FieldProcessor processor) {
		try {
			return filterDMSProperties(baseModel, jsonObject, processor);
		} catch (Exception e) {
			throw new RuntimeException(AlfrescoErrorReader.parse(e));
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
	public Pair<String, Serializable> convertDMSToCMFProperty(String dmsKey,
			Serializable objectValue, FieldProcessor level) {
		Pair<String, Serializable> value = processInternalDMSProperty(dmsKey, objectValue, level,
				null);
		if ((value != null) && !Pair.NULL_PAIR.equals(value)) {
			return value;
		}
		return null;
	}

	/**
	 * Convert cmf to dms properties by value.<br>
	 * REVIEW: the name of the method should be changed to reflect that we a using the base model
	 * for conversion
	 *
	 * @param properties
	 *            the properties
	 * @param processor
	 *            the processor
	 * @return the map
	 */
	public Map<String, Serializable> convertCMFtoDMSPropertiesByValue(
			Map<String, Serializable> properties, FieldProcessor processor) {
		Map<String, Serializable> props = new LinkedHashMap<String, Serializable>(properties.size());

		if ((baseModel != null) && (baseModel.getFields() != null)) {
			// TODO when model is cached do the opposite iteration
			Set<Entry<String, Serializable>> entrySet = properties.entrySet();
			for (Entry<String, Serializable> entry : entrySet) {
				PropertyDefinition definition = getPropertyDefinitionByName(entry.getKey(),
						baseModel);
				Pair<String, Serializable> value = processInternalCMFPropery(entry.getKey(),
						entry.getValue(), processor, definition);
				if ((value != null) && (value.getFirst() != null)) {
					props.put(value.getFirst(), value.getSecond());
				}
			}

			return props;
		}
		return props;
	}

	/**
	 * Convert dms to cmf properties by value - iteration is done over the value not the model
	 * itself
	 *
	 * @param properties
	 *            the properties
	 * @param revision
	 *            the revision
	 * @param instance
	 *            the instance
	 * @param level
	 *            the level
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	public Map<String, Serializable> convertDMStoCMFPropertiesByValue(JSONObject properties,
			Long revision, Instance instance, FieldProcessor level) throws JSONException {
		Map<String, Serializable> props = new LinkedHashMap<String, Serializable>(
				properties.length());
		DefinitionModel modelId = dictionaryService.getInstanceDefinition(instance);
		if (modelId == null) {
			modelId = baseModel;
		}
		if (trace) {
			LOGGER.trace("DMStoCMFPropertiesByValue for '" + modelId + "' on " + properties);
		}
		if ((modelId != null) && (modelId.getFields() != null)) {
			// TODO when model is cached do the opposite iteration
			Iterator<?> entrySet = properties.keys();
			while (entrySet.hasNext()) {
				String key = entrySet.next().toString();
				String modelKey = level.prepareDMSKey(key);
				PropertyDefinition definition = getDMSPropertyDefinitionByName(modelKey, modelId);
				if (!level.hasRequiredReadLevel(definition)) {
					continue;
				}
				if (!properties.has(modelKey) || properties.isNull(modelKey)) {
					LOGGER.warn("Not found value for: " + modelKey);
					continue;
				}
				Serializable objectValue = (Serializable) properties.get(modelKey);
				Pair<String, Serializable> value = processInternalDMSProperty(definition.getName(),
						objectValue, level, definition);
				if ((value != null) && (value.getFirst() != null)) {
					props.put(value.getFirst(), value.getSecond());
				}
			}

			return props;
		}

		return props;
	}

	/**
	 * Gets the model prefix.
	 *
	 * @return the model prefix
	 */
	public String getModelPrefix() {
		return modelPrefix;
	}

	/**
	 * Gets the cmf to dms mapping properties
	 *
	 * @return the cmf to dms mapping properties loaded from the provided config
	 */
	protected Properties getCMFtoDMSMapping() {
		return cmfTOdms;
	}

	/**
	 * Gets the cmf to dms reverse mapping
	 *
	 * @return the cmf to dms reverse mapping properties
	 */
	protected HashMap<String, Set<String>> getDMStoCMFMapping() {
		return dmsTOcmf;
	}

	/**
	 * Getter method for hashCalculator.
	 *
	 * @return the hashCalculator
	 */
	public HashCalculator getHashCalculator() {
		return hashCalculator;
	}

	/**
	 * Setter method for hashCalculator.
	 *
	 * @param hashCalculator
	 *            the hashCalculator to set
	 */
	public void setHashCalculator(HashCalculator hashCalculator) {
		this.hashCalculator = hashCalculator;
	}

}
