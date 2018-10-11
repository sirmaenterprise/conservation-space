package com.sirma.itt.seip.definition;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.Quad;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.collections.ContextualMap;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.definition.model.BaseDefinition;
import com.sirma.itt.seip.definition.model.FieldDefinitionImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.PrototypeDefinitionImpl;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.PathElementProxy;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.PrototypeDefinition;
import com.sirma.itt.seip.domain.definition.label.Displayable;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.model.DataType;
import com.sirma.itt.seip.runtime.boot.Startup;
import com.sirma.itt.seip.security.annotation.RunAsAllTenantAdmins;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.sep.definition.db.DefinitionEntry;

/**
 * Default service implementation for working with definitions.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DefinitionServiceImpl extends BaseDefinitionService implements DefinitionService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final long serialVersionUID = -6013425395204741541L;

	@SuppressWarnings("rawtypes")
	private static final Class BASE_DEFINITION_CACHE_KEY = BaseDefinition.class;
	private static final String BASE_DEFINITION_SELECTOR = "$DEFAULT_DEFINITION$";
	private static final PathElementProxy DEFAULT_PATH_ELEMENT = new PathElementProxy(BASE_DEFINITION_SELECTOR);

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private ExpressionsManager evaluatorManager;

	/**
	 * The definition type to accessor mapping. A mapping of concrete definition id to specific accessor. Populated
	 * lazily.
	 */
	@Inject
	private ContextualMap<String, DefinitionAccessor> definitionTypeToAccessorMapping;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private javax.enterprise.inject.Instance<MutableDefinitionService> mutableDefinitionService;

	@Inject
	private DatabaseIdManager databaseIdManager;

	private Set<Class<? extends DefinitionModel>> definitionTypeKeys = new LinkedHashSet<>();

	/**
	 * Initialize the instance by collecting information about the accessors and cache instances
	 */
	@Override
	@PostConstruct
	@SuppressWarnings("unchecked")
	public void init() {
		super.init();

		// collect a definition key from each accessor to allow accessor iteration via the cache
		for (DefinitionAccessor accessor : accessors) {
			for (Class<?> key : accessor.getSupportedObjects()) {
				if (DefinitionModel.class.isAssignableFrom(key)) {
					definitionTypeKeys.add((Class<? extends DefinitionModel>) key);
					break;
				}
			}
		}
	}

	/**
	 * Warm up the definition cache
	 */
	@Startup(async = true)
	@RunAsAllTenantAdmins
	void preloadDefinitions() {
		long count = getAllDefinitions().count();
		LOGGER.info("Loaded {} definitions", count);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends DefinitionModel> Stream<E> getAllDefinitions() {
		return (Stream<E>) definitionTypeKeys.stream().flatMap(type -> getAllDefinitionsInternal(type).stream()).filter(
				removeAbstract());
	}

	private static Predicate<? super DefinitionModel> removeAbstract() {
		return model -> !(model instanceof TopLevelDefinition) || !((TopLevelDefinition) model).isAbstract();
	}

	@Override
	public Stream<DefinitionModel> getAllDefinitions(InstanceType instanceType) {
		// this check will work only if the definition value for semantic class is in full uri format
		return getAllDefinitions().filter(byInstanceType(instanceType));
	}

	private static Predicate<DefinitionModel> byInstanceType(InstanceType instanceType) {
		// does check for the sub and super types
		return model -> {
			String typeId = model.getField(SEMANTIC_TYPE).map(PropertyDefinition::getDefaultValue).orElse(null);
			return instanceType.instanceOf(typeId) || instanceType.hasSubType(typeId);
		};
	}

	@Override
	public Stream<DefinitionModel> getAllDefinitions(String category) {
		return getAllDefinitions().filter(model -> category != null && model.getType() != null
				&& (category.contains(model.getType()) || model.getType().contains(category)));
	}

	@Override
	public <E extends DefinitionModel> List<E> getAllDefinitions(Class<?> ref) {
		if (ref == null) {
			return CollectionUtils.emptyList();
		}
		List<E> list = getAllDefinitionsInternal(ref);
		// used linked list: later in the application the returned list is
		// filtered - elements are removed from it so we use faster list for removal
		if (list != null) {
			return new LinkedList<>(list);
		}
		return CollectionUtils.emptyList();
	}

	@SuppressWarnings("unchecked")
	private <E extends DefinitionModel> List<E> getAllDefinitionsInternal(Class<?> ref) {
		return (List<E>) getCacheValue(getMaxRevisionsCache().getByKey(ref));
	}

	@Override
	public <E extends DefinitionModel> E getDefinition(Class<E> ref, String defId) {
		return getDefinitionAccessor(ref, true).getDefinition(defId);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends DefinitionModel> E getDefinition(Class<E> ref, String defId, Long version) {
		return (E) getDefinitionUsingAccessor(getDefinitionAccessor(ref, true), defId, version);
	}

	@Override
	public PropertyDefinition getProperty(String propertyName, Long revision, PathElement pathElement) {
		return getPropertyInternal(propertyName, revision, pathElement);
	}

	@Override
	public PrototypeDefinition getPrototype(String propertyName, Long revision, PathElement pathElement) {
		return getPropertyInternal(propertyName, revision, pathElement);
	}

	private PropertyDefinition getPropertyInternal(String propertyName, Long revision, PathElement pathElement) {
		boolean trace = LOGGER.isTraceEnabled();
		if (trace) {
			LOGGER.trace("Searching for property ({}, {}, {})", propertyName, revision,
					PathHelper.getPath(pathElement));
		}
		DefinitionModel model = getRootModel(pathElement, revision);
		if (model == null) {
			return null;
		}
		PropertyDefinition propertyDefinition = PathHelper.findProperty(model, pathElement, propertyName);
		if (propertyDefinition != null) {
			return injectLabelProvider(propertyDefinition);
		}
		if (trace) {
			LOGGER.trace("Property [{}] [{}] [{}] was NOT found!", propertyName, revision,
					PathHelper.getPath(pathElement));
		}
		return null;
	}

	/**
	 * Gets the root model by the given path. The model is searched in workflow and case definitions caches.
	 *
	 * @return the root model if found or <code>null</code> if not.
	 */
	private DefinitionModel getRootModel(PathElement pathElement, Long revision) {
		String rootPath = PathHelper.getRootPath(pathElement);
		if (StringUtils.isBlank(rootPath)) {
			return null;
		}
		DefinitionModel model;
		if (isDefaultPath(pathElement)) {
			model = getDefaultModel();
			if (model != null) {
				return model;
			}
		}
		return getModelUsingDetectedAccessor(pathElement, revision, rootPath);
	}

	private DefinitionModel getModelUsingDetectedAccessor(PathElement pathElement, Long revision, String rootPath) {
		DefinitionModel model;
		// first we try in the mapping for the supported class
		DefinitionAccessor definitionAccessor = getDefinitionAccessor(pathElement.getClass(), false);
		if (definitionAccessor == null) {
			LOGGER.trace("No explicit accessor found for {}", pathElement.getClass());
		}
		model = getDefinitionUsingAccessor(definitionAccessor, rootPath, revision);
		if (model != null) {
			return model;
		}

		// try to loaded as instance - currently most accurate method of definition loading
		if (pathElement instanceof Instance) {
			model = getInstanceDefinition((Instance) pathElement);
			if (model != null) {
				return model;
			}
		}

		return getModelByMappedDefinitionAccessor(rootPath, revision);
	}

	private static DefinitionModel getDefinitionUsingAccessor(DefinitionAccessor definitionAccessor, String rootPath,
			Long revision) {
		if (definitionAccessor == null) {
			return null;
		}
		DefinitionModel model;
		// if there revision is not known or not supported then we can call the latest revision
		if (revision == null || revision <= 0L) {
			model = definitionAccessor.getDefinition(rootPath);
		} else {
			model = definitionAccessor.getDefinition(rootPath, revision);
		}
		return model;
	}

	/**
	 * Gets the default model.
	 *
	 * @return the default model if found
	 */
	private DefinitionModel getDefaultModel() {
		List<DefinitionModel> list = getAllDefinitionsInternal(BASE_DEFINITION_CACHE_KEY);
		if (list != null && list.size() > 1) {
			return list.get(1);
		}
		return null;
	}

	private DefinitionEntry getDefaultModelEntry() {
		List<DefinitionModel> list = getAllDefinitionsInternal(BASE_DEFINITION_CACHE_KEY);
		if (CollectionUtils.isNotEmpty(list)) {
			DefinitionModel entity = list.get(0);
			if (entity instanceof DefinitionEntry) {
				return (DefinitionEntry) entity;
			}
		}
		return null;
	}

	/**
	 * Checks if is default path.
	 *
	 * @param pathElement
	 *            the path element
	 * @return true, if is default path
	 */
	private static boolean isDefaultPath(PathElement pathElement) {
		return EqualsHelper.nullSafeEquals(pathElement, DEFAULT_PATH_ELEMENT);
	}

	private DefinitionModel getModelByMappedDefinitionAccessor(String rootPath, Long revision) {
		DefinitionAccessor definitionAccessor;
		// first try to get it from the cached mapping
		definitionAccessor = definitionTypeToAccessorMapping.computeIfAbsent(rootPath,
				path -> getAccessors()
						.stream()
							.filter(accessor -> getDefinitionUsingAccessor(accessor, path, revision) != null)
							.findFirst()
							.orElse(null));

		return getDefinitionUsingAccessor(definitionAccessor, rootPath, revision);
	}

	private <E extends Displayable> E injectLabelProvider(E displayable) {
		if (displayable != null && (displayable.getLabelId() != null || displayable.getTooltipId() != null)) {
			displayable.setLabelProvider(labelProvider);
		}
		return displayable;
	}

	@Override
	@SuppressWarnings("boxing")
	public Long getPropertyId(String propertyName, Long revision, PathElement pathElement, Serializable value) {
		PropertyDefinition propertyDefinition = getPropertyInternal(propertyName, revision, pathElement);
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
	@SuppressWarnings("boxing")
	public DataTypeDefinition getDataTypeDefinition(Object key) {
		if (key == null) {
			return null;
		}

		if (key instanceof Long) {
			return getCacheValue(getTypeDefinitionIdCache().getByKey((Long) key));
		} else if (key instanceof Number) {
			return getCacheValue(getTypeDefinitionIdCache().getByKey(((Number) key).longValue()));
		}

		String name = getTypeNameFromKey(key);
		if (name == null || name.isEmpty()) {
			return null;
		}

		String localName = name;
		EntityLookupCache<String, DataTypeDefinition, String> typeDefinitionCache = getTypeDefinitionCache();
		Pair<String, DataTypeDefinition> pair = null;
		// if the given argument is class name we could probably find it by it
		if (localName.indexOf(':', 1) > 0) {
			localName = getCacheValue(getTypeDefinitionUriCache().getByKey(localName));
		} else if (localName.indexOf('.', 1) > 0) {
			pair = getAsDate(localName, typeDefinitionCache);
		}

		// localName == null when the parameter is not registered semantic class
		if (pair == null && localName!= null) {
			pair = typeDefinitionCache.getByKey(localName);
		}
		return getCacheValue(pair);
	}

	private static Pair<String, DataTypeDefinition> getAsDate(String localName,
			EntityLookupCache<String, DataTypeDefinition, String> typeDefinitionCache) {
		// probably we can cache the data type instance not to create it every time
		DataType dataType = new DataType();
		setNameIfDate(localName, dataType);
		dataType.setJavaClassName(localName);
		return typeDefinitionCache.getByValue(dataType);
	}

	private static void setNameIfDate(String localName, DataType dataType) {
		if (Date.class.toString().equals(localName)) {
			dataType.setName(DataTypeDefinition.DATETIME);
		}
	}

	private static String getTypeNameFromKey(Object key) {
		String name = null;
		if (key instanceof String) {
			name = (String) key;
		} else if (key instanceof Class) {
			name = ((Class<?>) key).getName();
		} else if (key instanceof Instance) {
			name = key.getClass().getName();
		} else if (key instanceof InstanceReference && ((InstanceReference) key).getReferenceType() != null) {
			name = ((InstanceReference) key).getReferenceType().getName();
		}
		return name;
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
		PrototypeDefinition prototypeDefinition = createDefaultPropertyDefinitionIfNotExist(propertyName,
				detectType.getName(), serializable instanceof Collection);
		if (!EqualsHelper.nullSafeEquals(prototypeDefinition.getDataType().getName(), detectType.getName(), false)) {
			try {
				// we will check if is convertible at all
				if (typeConverter.convert(prototypeDefinition.getDataType().getJavaClass(), serializable) != null) {
					LOGGER.trace("Returned prototype with type {} for property {} and type {}",
							prototypeDefinition.getDataType().getName(), propertyName, detectType.getName());
					return prototypeDefinition;
				}
			} catch (RuntimeException e) {
				String message = "Failed to convert the {} of type {} to {} due to: {}";
				LOGGER.debug(message, serializable, detectType.getName(), prototypeDefinition.getDataType().getName(),
						e.getMessage());
				LOGGER.trace(message, serializable, detectType.getName(), prototypeDefinition.getDataType().getName(),
						e.getMessage(), e);
			}
			LOGGER.error(
					"Trying to persist dynamic data with type {} but there is a property with the same name {} but with type {}. Data will not be persisted!",
					detectType.getName(), prototypeDefinition.getIdentifier(),
					prototypeDefinition.getDataType().getName());
			return null;
		}
		return prototypeDefinition;
	}

	/**
	 * Creates the default property definition if not exist.
	 */
	@SuppressWarnings("boxing")
	private PrototypeDefinition createDefaultPropertyDefinitionIfNotExist(String name, String type,
			boolean multyValued) {
		PropertyDefinition property = getPropertyInternal(name, 0L, DEFAULT_PATH_ELEMENT);
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
			proxy.setContainer(BASE_DEFINITION_SELECTOR);
			definition.setMultiValued(multyValued);

			toSave = true;
		} else if (Boolean.FALSE.equals(property.isMultiValued()) && multyValued) {
			property.setMultiValued(Boolean.TRUE);
			toSave = true;
		}

		if (toSave) {
			saveNewProperty(property);
		}

		return property;
	}

	private void saveNewProperty(PropertyDefinition property) {
		PropertyDefinition propertyDefinition = mutableDefinitionService.get().savePropertyIfChanged(property, null);
		if (propertyDefinition.getPrototypeId() == null) {
			return;
		}

		DefinitionEntry entry = getDefaultModelEntry();
		if (entry == null) {
			return;
		}

		DefinitionModel target = entry.getTarget();
		target.getFields().add(propertyDefinition);
		// update the internal model and serialize back to array
		entry.setTarget(target);
		// save model to DB
		dbDao.saveOrUpdate(entry);
		List<DefinitionModel> arrayList = new ArrayList<>(2);
		arrayList.add(entry);
		arrayList.add(target);
		// and update the stale cache
		getMaxRevisionsCache().setValue(BASE_DEFINITION_CACHE_KEY, arrayList);
	}

	/**
	 * Detects the argument type.
	 *
	 * @param serializable
	 *            the serializable
	 * @return the data type definition
	 */
	protected DataTypeDefinition detectType(Serializable serializable) {
		DataTypeDefinition detectedType = null;
		if (serializable instanceof String) {
			detectedType = getDataTypeDefinition(DataTypeDefinition.TEXT);
		} else if (serializable instanceof Number) {
			detectedType = detectNumberType(serializable);
		} else if (serializable instanceof Boolean) {
			detectedType = getDataTypeDefinition(DataTypeDefinition.BOOLEAN);
		} else if (serializable instanceof CommonInstance) {
			detectedType = getDataTypeDefinition(DataTypeDefinition.INSTANCE);
		} else if (serializable instanceof Date) {
			detectedType = getDataTypeDefinition(DataTypeDefinition.DATETIME);
		} else if (serializable instanceof Collection) {
			detectedType = detectCollectionType(serializable);
		} else if (serializable != null) {
			detectedType = getDataTypeDefinition(DataTypeDefinition.ANY);
		}
		if (detectedType == null) {
			String type = serializable != null ? serializable.getClass().getName() : "null";
			LOGGER.warn("Not supported type for {} class with value = {}", type, serializable);
		}
		return detectedType;
	}

	private DataTypeDefinition detectNumberType(Serializable serializable) {
		DataTypeDefinition detectedType = null;
		if (serializable instanceof Long) {
			detectedType = getDataTypeDefinition(DataTypeDefinition.LONG);
		} else if (serializable instanceof Integer) {
			detectedType = getDataTypeDefinition(DataTypeDefinition.INT);
		} else if (serializable instanceof Double) {
			detectedType = getDataTypeDefinition(DataTypeDefinition.DOUBLE);
		} else if (serializable instanceof Float) {
			detectedType = getDataTypeDefinition(DataTypeDefinition.FLOAT);
		}
		return detectedType;
	}

	@SuppressWarnings("unchecked")
	private DataTypeDefinition detectCollectionType(Serializable serializable) {
		DataTypeDefinition detectedType = null;
		Collection<Serializable> collection = (Collection<Serializable>) serializable;
		if (!collection.isEmpty()) {
			Iterator<Serializable> iterator = collection.iterator();
			detectedType = detectType(iterator.next());
		}
		return detectedType;
	}

	@Override
	public <D extends DefinitionModel> D getInstanceDefinition(Instance instance) {
		if (instance == null) {
			throw new EmfRuntimeException("Instance is required argument!");
		}
		DefinitionAccessor accessor = getDefinitionAccessor(instance.getClass(), false);
		if (accessor != null) {
			return accessor.getDefinition(instance);
		}
		LOGGER.warn(
				"Trying to load definition for instance id={}, definition={} class={} but there is not defined definition accessor! Nothing will be returned.",
				instance.getId(), instance.getIdentifier(), instance.getClass().getSimpleName());
		return null;
	}

	@Override
	public Stream<PropertyDefinition> getInstanceObjectProperties(Instance instance) {
		DefinitionModel definition = getInstanceDefinition(instance);
		return definition == null ? Stream.empty()
				: definition.fieldsStream().filter(PropertyDefinition.isObjectProperty());
	}

	@Override
	public Map<String, Serializable> filterProperties(DefinitionModel model, Map<String, Serializable> properties,
			DisplayType displayType) {
		Map<String, Serializable> props = new LinkedHashMap<>();
		if (displayType == DisplayType.EDITABLE && model != null && model.getFields() != null) {
			// removed all non editable properties
			for (PropertyDefinition definition : model.getFields()) {
				if (definition.getDisplayType() != DisplayType.EDITABLE && !definition.isMandatory()) {
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
	private Serializable convertPropertyValue(PropertyDefinition definition, Serializable serializable) {
		if (serializable == null) {
			return null;
		}
		Serializable converted = evaluatorManager.ruleConvertTo(definition, serializable);
		LOGGER.trace("Converted property value {} from {} to {}", definition.getName(), serializable, converted);
		return converted;
	}

	@Override
	public MutableDefinitionService getMutableInstance() {
		return mutableDefinitionService.get();
	}

	@Override
	public DefinitionModel find(String id) {
		return getModelByMappedDefinitionAccessor(id, null);
	}

	@Override
	public String getDefinitionIdentifier(DefinitionModel model) {
		if (model == null) {
			return null;
		}

		String id = model.getIdentifier();
		String type = model.getType();
		if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(type)) {
			return String.valueOf(databaseIdManager.getValidId(type + "-" + id));
		}
		return null;
	}

	@Override
	public String getDefaultDefinitionId(Instance instance) {
		if (instance == null) {
			throw new EmfRuntimeException("Instance is required argument!");
		}
		DefinitionAccessor accessor = getDefinitionAccessor(instance.getClass(), false);
		if (accessor != null) {
			return accessor.getDefaultDefinitionId(instance);
		}
		LOGGER.warn("Trying to load default definition for instance " + instance.getClass()
				+ " but there is not defined definition accessor! Nothing will be returned.");
		return null;
	}
}
