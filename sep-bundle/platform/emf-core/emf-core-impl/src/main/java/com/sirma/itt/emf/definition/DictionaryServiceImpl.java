/**
 *
 */
package com.sirma.itt.emf.definition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.cache.CacheConfiguration;
import com.sirma.itt.emf.cache.CacheTransactionMode;
import com.sirma.itt.emf.cache.Eviction;
import com.sirma.itt.emf.cache.Expiration;
import com.sirma.itt.emf.cache.lookup.EntityLookupCache;
import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.definition.model.BaseDefinition;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.DefinitionEntry;
import com.sirma.itt.emf.definition.model.FieldDefinitionImpl;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.emf.definition.model.PrototypeDefinition;
import com.sirma.itt.emf.definition.model.PrototypeDefinitionImpl;
import com.sirma.itt.emf.definition.model.WritablePropertyDefinition;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.Quad;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.PathElementProxy;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.label.Displayable;
import com.sirma.itt.emf.label.LabelProvider;
import com.sirma.itt.emf.security.AuthenticationService;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.Documentation;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.PathHelper;

/**
 * Default service implementation for working with definitions.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DictionaryServiceImpl extends BaseDefinitionService implements DictionaryService {

	private static final long serialVersionUID = -6013425395204741541L;

	/** The Constant NO_DEFINITION_SELECTOR. */
	private static final String BASE_DEFINITION_SELECTOR = "$DEFAULT_DEFINITION$";

	/** The Constant DEFAULT_PATH_ELEMENT. */
	private static final PathElementProxy DEFAULT_PATH_ELEMENT = new PathElementProxy(
			BASE_DEFINITION_SELECTOR);

	/** The Constant TYPE_DEFINITION_CACHE. */
	@CacheConfiguration(container = "cmf", doc = @Documentation(""
			+ "Cache used to contain the type definitions in the system. For every type defined are used 2 cache entries."
			+ "<br>Minimal value expression: types * 2.1"))
	private static final String TYPE_DEFINITION_CACHE = "TYPE_DEFINITION_CACHE";
	@CacheConfiguration(container = "cmf", doc = @Documentation(""
			+ "Cache used to contain the type definitions in the system mapped by URI. For every type defined are used 2 cache entries."
			+ "<br>Minimal value expression: types * 1.2"))
	private static final String TYPE_DEFINITION_URI_CACHE = "TYPE_DEFINITION_URI_CACHE";
	/** The Constant MAX_REVISIONS_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 20), expiration = @Expiration(maxIdle = 900000, interval = 60000), doc = @Documentation(""
			+ "Cache for the list of all definitions at max revision per type"
			+ "<br>Minimal value expression: types * 1.2"))
	private static final String MAX_REVISIONS_CACHE = "MAX_REVISIONS_CACHE";
	/** The Constant PROTOTYPE_CACHE. */
	@CacheConfiguration(container = "cmf", eviction = @Eviction(maxEntries = 1000), expiration = @Expiration(maxIdle = 1800000, interval = 60000), transaction = CacheTransactionMode.FULL_XA, doc = @Documentation(""
			+ "Fully transactional cache used to store the unique prototy entries. For every unique property are used 2 cache entries."
			+ "<br>Minimal value expression: uniqueProperties * 2.2"))
	private static final String PROTOTYPE_CACHE = "PROTOTYPE_CACHE";

	/** The cache context. */
	@Inject
	private EntityLookupCacheContext cacheContext;

	/** The label provider. */
	@Inject
	private LabelProvider labelProvider;

	/** The logger. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryServiceImpl.class);

	/** The evaluator manager. */
	@Inject
	private ExpressionsManager evaluatorManager;

	/** The authentication service instance. */
	@Inject
	private javax.enterprise.inject.Instance<AuthenticationService> authenticationServiceInstance;

	/**
	 * The definition type to accessor mapping. A mapping of concrete definition id to specific
	 * accessor. Populated lazily.
	 */
	private final Map<String, DefinitionAccessor> definitionTypeToAccessorMapping = new HashMap<String, DefinitionAccessor>(
			64);

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private javax.enterprise.inject.Instance<MutableDictionaryService> mutableDictionaryService;

	/** The trace. */
	private boolean trace;

	/**
	 * Initialize the instance by collecting information about the accessors and cache instances
	 */
	@Override
	@PostConstruct
	public void init() {
		trace = LOGGER.isTraceEnabled();
		super.init();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <E extends DefinitionModel> List<E> getAllDefinitions(Class<E> ref) {
		String currentContainer = getCurrentContainer();
		if ((ref == null) || StringUtils.isNullOrEmpty(currentContainer)) {
			return CollectionUtils.emptyList();
		}
		Pair<Pair<Class, String>, List<DefinitionModel>> pair = getMaxRevisionsCache().getByKey(
				new Pair<Class, String>(ref, currentContainer));
		List<DefinitionModel> list = getCacheValue(pair);
		// used linked list: later in the application the returned list is
		// filtered - elements are removed from it so we use faster list for removal
		List<E> result = new LinkedList<>();
		if (list != null) {
			for (DefinitionModel topLevelDefinition : list) {
				result.add((E) topLevelDefinition);
			}
		}
		return result;
	}

	/**
	 * Gets the current container.
	 *
	 * @return the current container
	 */
	private String getCurrentContainer() {
		return SecurityContextManager.getCurrentContainer(authenticationServiceInstance);
	}

	@Override
	public <E extends DefinitionModel> E getDefinition(Class<E> ref, String defId) {
		return getDefinitionAccessor(ref, true).getDefinition(getCurrentContainer(), defId);
	}

	@Override
	public <E extends DefinitionModel> E getDefinition(Class<E> ref, String defId, Long version) {
		return getDefinitionAccessor(ref, true)
				.getDefinition(getCurrentContainer(), defId, version);
	}

	@Override
	public <E extends DefinitionModel> List<E> getDefinitionVersions(Class<E> ref, String defId) {
		return getDefinitionAccessor(ref, true).getDefinitionVersions(getCurrentContainer(), defId);
	}

	@Override
	public PropertyDefinition getProperty(String propertyName, Long revision,
			PathElement pathElement) {
		String container = null;
		if (!RuntimeConfiguration
				.isConfigurationSet(RuntimeConfigurationProperties.DO_NOT_USE_CONTAINER_FILTERING)) {
			container = getCurrentContainer();
		}
		return getPropertyInternal(propertyName, revision, pathElement, container);
	}

	@Override
	public PrototypeDefinition getPrototype(String propertyName, Long revision,
			PathElement pathElement) {
		return getPropertyInternal(propertyName, revision, pathElement, getCurrentContainer());
	}

	/**
	 * Gets the property internal.
	 *
	 * @param propertyName
	 *            the property name
	 * @param revision
	 *            the revision
	 * @param pathElement
	 *            the path element
	 * @param container
	 *            the container
	 * @return the property internal
	 */
	private PropertyDefinition getPropertyInternal(String propertyName, Long revision,
			PathElement pathElement, String container) {
		if (trace) {
			LOGGER.trace("Searching for property (" + propertyName + ", " + revision + ", "
					+ PathHelper.getPath(pathElement) + ")");
		}
		DefinitionModel model = getRootModel(pathElement, revision, container);
		if (model == null) {
			LOGGER.warn("Invalid path. No model found for " + PathHelper.getPath(pathElement));
			return null;
		}
		PropertyDefinition propertyDefinition = PathHelper.findProperty(model, pathElement,
				propertyName);
		if (propertyDefinition != null) {
			return injectLabelProvider(propertyDefinition);
		}
		if (trace) {
			LOGGER.trace("Property was NOT found!");
		}
		return null;
	}

	/**
	 * Gets the root model by the given path. The model is searched in workflow and case definitions
	 * caches.
	 *
	 * @param pathElement
	 *            the path element
	 * @param revision
	 *            the revision
	 * @param container
	 *            the container
	 * @return the root model if found or <code>null</code> if not.
	 */
	private DefinitionModel getRootModel(PathElement pathElement, Long revision, String container) {
		String rootPath = PathHelper.getRootPath(pathElement);
		if (StringUtils.isNullOrEmpty(rootPath)) {
			return null;
		}
		DefinitionModel model = null;
		if (isDefaultPath(pathElement)) {
			model = getDefaultModel();
			if (model != null) {
				return model;
			}
		}
		// first we try in the mapping for the supported class
		DefinitionAccessor definitionAccessor = getDefinitionAccessor(pathElement.getClass(), false);
		if (definitionAccessor != null) {
			model = definitionAccessor.getDefinition(container, rootPath, revision);
			if (model != null) {
				return model;
			}
		} else if (trace) {
			LOGGER.trace("No explicit accessor found for " + pathElement.getClass());
		}

		model = getModelByMappedDefinitionAccessor(rootPath, revision, container);

		// as last resort will try to fetch the model from the parent if any
		if (model == null) {
			model = getDefinitionByParent(pathElement, revision, container, rootPath);
		}
		return model;
	}

	/**
	 * Gets the default model.
	 * 
	 * @return the default model if found
	 */
	@SuppressWarnings("rawtypes")
	private DefinitionModel getDefaultModel() {
		DefinitionModel model = null;
		EntityLookupCache<Pair<Class, String>, List<DefinitionModel>, Serializable> maxRevisionsCache = getMaxRevisionsCache();
		Pair<Pair<Class, String>, List<DefinitionModel>> pair = maxRevisionsCache
				.getByKey(new Pair<Class, String>(BaseDefinition.class,
						SecurityContextManager.NO_CONTAINER));
		List<DefinitionModel> list = getCacheValue(pair);
		if ((list != null) && !list.isEmpty()) {
			DefinitionModel entity = list.get(0);
			if (entity instanceof DefinitionEntry) {
				model = ((DefinitionEntry) entity).getTarget();
			}
		}
		return model;
	}

	/**
	 * Checks if is default path.
	 * 
	 * @param pathElement
	 *            the path element
	 * @return true, if is default path
	 */
	private boolean isDefaultPath(PathElement pathElement) {
		return EqualsHelper.nullSafeEquals(pathElement, DEFAULT_PATH_ELEMENT);
	}

	/**
	 * Gets the model by mapped definition accessor.
	 * 
	 * @param rootPath
	 *            the root path
	 * @param revision
	 *            the revision
	 * @param container
	 *            the container
	 * @return the model by mapped definition accessor
	 */
	private DefinitionModel getModelByMappedDefinitionAccessor(String rootPath, Long revision,
			String container) {
		DefinitionAccessor definitionAccessor;
		DefinitionModel model = null;
		// first try to get it from the cached mapping
		definitionAccessor = definitionTypeToAccessorMapping.get(rootPath);
		if (definitionAccessor != null) {
			model = definitionAccessor.getDefinition(container, rootPath, revision);
		}
		if (model == null) {
			for (DefinitionAccessor accessor : getAccessors()) {
				model = accessor.getDefinition(container, rootPath, revision);
				if (model != null) {
					// cache for future use
					definitionTypeToAccessorMapping.put(rootPath, accessor);
					break;
				}
			}
		}
		return model;
	}

	/**
	 * Gets the definition by parent.
	 * 
	 * @param pathElement
	 *            the path element
	 * @param revision
	 *            the revision
	 * @param container
	 *            the container
	 * @param rootPath
	 *            the root path
	 * @return the definition by parent
	 */
	@SuppressWarnings("rawtypes")
	private DefinitionModel getDefinitionByParent(PathElement pathElement, Long revision,
			String container, String rootPath) {
		DefinitionModel model = null;
		DefinitionAccessor definitionAccessor;
		// if not found then we have some proxy that we have to search for
		// if element instance of owned model then we will try to return the parent instead of
		// the current node
		if (pathElement instanceof OwnedModel) {
			InstanceReference reference = ((OwnedModel) pathElement).getOwningReference();
			if ((reference != null) && (reference.getReferenceType() != null)) {
				Class parentModelClass = reference.getReferenceType().getJavaClass();
				definitionAccessor = getDefinitionAccessor(parentModelClass, false);
				if (definitionAccessor != null) {
					model = definitionAccessor.getDefinition(container, rootPath, revision);
				}
			}
		}
		return model;
	}

	/**
	 * Inject label provider.
	 *
	 * @param <E>
	 *            the element type
	 * @param displayable
	 *            the definition impl
	 * @return the property definition
	 */
	private <E extends Displayable> E injectLabelProvider(E displayable) {
		if ((displayable != null)
				&& ((displayable.getLabelId() != null) || (displayable.getTooltipId() != null))) {
			displayable.setLabelProvider(labelProvider);
		}
		return displayable;
	}

	@Override
	public Long getPropertyId(String propertyName, Long revision, PathElement pathElement,
			Serializable value) {
		String container = getCurrentContainer();
		PropertyDefinition propertyDefinition = getPropertyInternal(propertyName, revision,
				pathElement, container);
		if (propertyDefinition != null) {
			return propertyDefinition.getPrototypeId();
		}
		// if we does not have a value we cannot continue due to the fact we cannot determine the
		// type of the fields to look for
		if (value == null) {
			return null;
		}
		DataTypeDefinition detectType = detectType(value);
		if (detectType == null) {
			// we cannot continue if the type is unknown - most likely an empty list
			return null;
		}
		// as last resort we will try to determine the field prototype
		EntityLookupCache<Long, PrototypeDefinition, Quad<String, String, Boolean, Long>> prototypeCache = getPrototypeCache();
		PrototypeDefinition prototype = new PrototypeDefinitionImpl();
		prototype.setIdentifier(propertyName);
		prototype.setContainer(container);
		prototype.setDataType(detectType);
		prototype.setMultiValued(value instanceof Collection);
		Pair<Long, PrototypeDefinition> pair2 = prototypeCache.getByValue(prototype);
		if (pair2 != null) {
			return pair2.getFirst();
		}
		return null;
	}

	@Override
	public String getPropertyById(Long propertyId) {
		if (propertyId == null) {
			return null;
		}
		Pair<Long, PrototypeDefinition> pair = getPrototypeCache().getByKey(propertyId);
		if (pair == null) {
			return null;
		}
		return pair.getSecond().getIdentifier();
	}

	@Override
	public PrototypeDefinition getProperty(Long propertyId) {
		if (propertyId == null) {
			return null;
		}
		Pair<Long, PrototypeDefinition> pair = getPrototypeCache().getByKey(propertyId);
		return getCacheValue(pair);
	}

	@Override
	public DataTypeDefinition getDataTypeDefinition(String name) {
		if (StringUtils.isNullOrEmpty(name)) {
			return null;
		}
		String localName = name;
		EntityLookupCache<String, DataTypeDefinition, String> typeDefinitionCache = getTypeDefinitionCache();
		Pair<String, DataTypeDefinition> pair = null;
		// if the given argument is class name we could probably find it by it
		if (localName.indexOf(':', 1) > 0) {
			Pair<String, String> byKey = getTypeDefinitionUriCache().getByKey(localName);
			if (byKey != null) {
				localName = byKey.getSecond();
			}
		} else if (localName.indexOf('.', 1) > 0) {
			// probably we can cache the data type instance not to create it every time
			DataType dataType = new DataType();
			if (Date.class.toString().equals(localName)) {
				dataType.setName(DataTypeDefinition.DATETIME);
			}
			dataType.setJavaClassName(localName);
			pair = typeDefinitionCache.getByValue(dataType);
		}
		if (pair == null) {
			pair = typeDefinitionCache.getByKey(localName);
		}
		return getCacheValue(pair);
	}

	/**
	 * Gets the data type definition for the given class.
	 *
	 * @param clazz
	 *            the class to get the data type definition
	 * @return the data type definition
	 */
	protected DataTypeDefinition getDataTypeDefinition(Class<?> clazz) {
		if (clazz == null) {
			throw new EmfRuntimeException("Cannot fetch type for null class");
		}
		return getDataTypeDefinition(clazz.getName());
	}

	@Override
	public PrototypeDefinition getDefinitionByValue(String propertyName, Serializable serializable) {
		DataTypeDefinition detectType = detectType(serializable);
		if (detectType == null) {
			return null;
		}
		PrototypeDefinition prototypeDefinition = createDefaultPropertyDefinitionIfNotExist(
				propertyName, detectType.getName(), serializable instanceof Collection);
		if (!EqualsHelper.nullSafeEquals(prototypeDefinition.getDataType().getName(),
				detectType.getName(), false)) {
			try {
				// we will check if is convertible at all
				if (typeConverter.convert(prototypeDefinition.getDataType(), serializable) != null) {
					LOGGER.trace("Returned prototype with type {} for property {} and type {}",
							prototypeDefinition.getDataType().getName(), propertyName,
							detectType.getName());
					return prototypeDefinition;
				}
			} catch (RuntimeException e) {
				String message = "Failed to convert the {} of type {} to {} due to: {}";
				LOGGER.debug(message, serializable, detectType.getName(), prototypeDefinition
						.getDataType().getName(), e.getMessage());
				LOGGER.trace(message, serializable, detectType.getName(), prototypeDefinition
						.getDataType().getName(), e.getMessage(), e);
			}
			LOGGER.error("Trying to persist dynamic data with type " + detectType.getName()
					+ " but there is a property with the same name "
					+ prototypeDefinition.getIdentifier() + " but with type "
					+ prototypeDefinition.getDataType().getName() + ". Data will not be persisted!");
			return null;
		}
		return prototypeDefinition;
	}

	/**
	 * Creates the default property definition if not exist.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param multyValued
	 *            if the field is multi valued
	 * @return the property definition
	 */
	private PrototypeDefinition createDefaultPropertyDefinitionIfNotExist(String name, String type,
			boolean multyValued) {
		String container = getCurrentContainer();
		PropertyDefinition property = getPropertyInternal(name, 0L, DEFAULT_PATH_ELEMENT, container);
		boolean toSave = false;
		if (property == null) {
			property = new PropertyDefinitionProxy();
			WritablePropertyDefinition definition = new FieldDefinitionImpl();
			definition.setType(type);
			definition.setDataType(getDataTypeDefinition(type));
			definition.setDefaultProperties();
			PropertyDefinitionProxy proxy = (PropertyDefinitionProxy) property;
			proxy.setTarget(definition);
			proxy.setParentPath(DEFAULT_PATH_ELEMENT.getPath());
			proxy.setRevision(0L);
			proxy.setName(name);
			proxy.setContainer(container);
			definition.setMultiValued(multyValued);

			toSave = true;
		} else if (Boolean.FALSE.equals(property.isMultiValued()) && multyValued) {
			property.setMultiValued(true);
			toSave = true;
		}

		if (toSave) {
			saveNewProperty(property);
		}

		return property;
	}

	/**
	 * Save new property.
	 * 
	 * @param property
	 *            the property
	 */
	@SuppressWarnings("rawtypes")
	private void saveNewProperty(PropertyDefinition property) {
		PropertyDefinition propertyDefinition = mutableDictionaryService.get()
				.savePropertyIfChanged(property, null);
		if (propertyDefinition.getPrototypeId() != null) {
			EntityLookupCache<Pair<Class, String>, List<DefinitionModel>, Serializable> maxRevisionsCache = getMaxRevisionsCache();
			Pair<Class, String> key = new Pair<Class, String>(BaseDefinition.class,
					SecurityContextManager.NO_CONTAINER);
			Pair<Pair<Class, String>, List<DefinitionModel>> pair = maxRevisionsCache.getByKey(key);
			List<DefinitionModel> list = getCacheValue(pair);
			if ((list != null) && !list.isEmpty()) {
				DefinitionModel model = list.get(0);
				if (model instanceof DefinitionEntry) {
					DefinitionEntry entry = (DefinitionEntry) model;
					DefinitionModel target = entry.getTarget();
					target.getFields().add(propertyDefinition);
					// update the internal model and serialize back to array
					entry.setTarget(target);
					// save model to DB
					dbDao.saveOrUpdate(entry);
					List<DefinitionModel> arrayList = new ArrayList<DefinitionModel>(1);
					arrayList.add(entry);
					// and update the stale cache
					maxRevisionsCache.setValue(key, arrayList);
				}
			}
		}
	}

	/**
	 * Detects the argument type.
	 *
	 * @param serializable
	 *            the serializable
	 * @return the data type definition
	 */
	@SuppressWarnings("unchecked")
	protected DataTypeDefinition detectType(Serializable serializable) {
		if (serializable instanceof String) {
			return getDataTypeDefinition(DataTypeDefinition.TEXT);
		} else if (serializable instanceof Number) {
			if (serializable instanceof Long) {
				return getDataTypeDefinition(DataTypeDefinition.LONG);
			} else if (serializable instanceof Integer) {
				return getDataTypeDefinition(DataTypeDefinition.INT);
			} else if (serializable instanceof Double) {
				return getDataTypeDefinition(DataTypeDefinition.DOUBLE);
			} else if (serializable instanceof Float) {
				return getDataTypeDefinition(DataTypeDefinition.FLOAT);
			}
		} else if (serializable instanceof Boolean) {
			return getDataTypeDefinition(DataTypeDefinition.BOOLEAN);
		} else if (serializable instanceof CommonInstance) {
			return getDataTypeDefinition(DataTypeDefinition.INSTANCE);
		} else if (serializable instanceof Date) {
			return getDataTypeDefinition(DataTypeDefinition.DATETIME);
		} else if (serializable instanceof Collection) {
			Collection<Serializable> collection = (Collection<Serializable>) serializable;
			if (!collection.isEmpty()) {
				return detectType(collection.iterator().next());
			}
		} else if (serializable != null) {
			return getDataTypeDefinition(DataTypeDefinition.ANY);
		}
		LOGGER.warn("Not supported type for "
				+ (serializable != null ? serializable.getClass().getName() : "null")
				+ " class with value = " + serializable);
		return null;
	}


	@Override
	public DefinitionModel getInstanceDefinition(Instance instance) {
		if (instance == null) {
			throw new EmfRuntimeException("Instance is required argument!");
		}
		DefinitionAccessor accessor = getDefinitionAccessor(instance.getClass(), false);
		if (accessor != null) {
			return accessor.getDefinition(instance);
		}
		LOGGER.warn("Trying to load definition for instance " + instance.getClass()
				+ " but there is not defined definition accessor! Nothing will be returned.");
		return null;
	}

	@Override
	public Map<String, Serializable> filterProperties(DefinitionModel model,
			Map<String, Serializable> properties, DisplayType displayType) {
		Map<String, Serializable> props = new LinkedHashMap<>();
		if ((displayType == DisplayType.EDITABLE) && (model != null) && (model.getFields() != null)) {
			// removed all non editable properties
			for (PropertyDefinition definition : model.getFields()) {
				if ((definition.getDisplayType() != DisplayType.EDITABLE)
						&& !definition.isMandatory()) {
					LOGGER.trace("Ignoring property {} from the source map", definition.getName());
					// the previous implementation removed the property
				} else
				// if the we keep the property then we try to convert it
				if (properties.containsKey(definition.getName())) {
					props.put(definition.getName(),
							convertPropertyValue(definition, properties.get(definition.getName())));
				}
			}
			return props;
		}
		if (model != null) {
			// convert property values
			for (PropertyDefinition definition : model.getFields()) {
				if (properties.containsKey(definition.getName())) {
					props.put(definition.getName(),
							convertPropertyValue(definition, properties.get(definition.getName())));
				}
			}
		}

		return props;
	}

	/**
	 * Convert property value.
	 *
	 * @param definition
	 *            the definition
	 * @param serializable
	 *            the serializable
	 * @return the serializable
	 */
	private Serializable convertPropertyValue(PropertyDefinition definition,
			Serializable serializable) {
		if (serializable == null) {
			return null;
		}
		Serializable converted = evaluatorManager.ruleConvertTo(definition, serializable);
		if (trace) {
			LOGGER.trace("Converted property value " + definition.getName() + " from "
					+ serializable + " to " + converted);
		}
		return converted;
	}

	@Override
	public MutableDictionaryService getMutableInstance() {
		return mutableDictionaryService.get();
	}
}
