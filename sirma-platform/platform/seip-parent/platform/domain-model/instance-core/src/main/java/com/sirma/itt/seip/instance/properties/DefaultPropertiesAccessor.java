package com.sirma.itt.seip.instance.properties;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.cache.CacheConfiguration;
import com.sirma.itt.seip.cache.Eviction;
import com.sirma.itt.seip.cache.Expiration;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PrototypeDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.instance.properties.entity.NodePropertyHelper;
import com.sirma.itt.seip.instance.properties.entity.PropertyEntity;
import com.sirma.itt.seip.instance.properties.entity.PropertyValue;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Storage accessor for default runtime properties. The accessor defines a cache that is used to store the properties of
 * the loaded instances.
 *
 * @author BBonev
 */
public class DefaultPropertiesAccessor extends BasePropertyStorageAccessor {
	/** The Constant PROPERTY_ENTITY_CACHE. */
	@CacheConfiguration(eviction = @Eviction(maxEntries = 50000) , expiration = @Expiration(maxIdle = 1800000, interval = 60000) , doc = @Documentation(""
			+ "Cache used to properties for the loaded active instances. The cache does NOT handle instance that are stored only in a semantic database. "
			+ "The cache SHOULD not be transactional due to invalid state when cascading properties save/load."
			+ "<br>Minimal value expression: (caseCache + documentCache + sectionCache + projectCache + averageNonStartedScheduleEntries + workflowTaskCache + standaloneTaskCache + workflowCache) * 1.2") )
	private static final String PROPERTY_ENTITY_CACHE = "PROPERTY_ENTITY_CACHE";
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPropertiesAccessor.class);

	/** The cache context. */
	@Inject
	private EntityLookupCacheContext cacheContext;
	/** The node property helper. */
	@Inject
	private NodePropertyHelper nodePropertyHelper;
	/** The persistent properties. */
	@Inject
	@ExtensionPoint(value = PersistentPropertiesExtension.TARGET_NAME)
	private Iterable<PersistentPropertiesExtension> persistentProperties;

	@Inject
	private EventService eventService;

	/** Set of properties that are allowed to be saved without definition. */
	private Set<String> alwaysPersistProperties;

	/**
	 * Initialize.
	 */
	@Override
	@PostConstruct
	protected void initialize() {
		super.initialize();
		if (!cacheContext.containsCache(PROPERTY_ENTITY_CACHE)) {
			cacheContext.createCache(PROPERTY_ENTITY_CACHE,
					new PropertiesLookupCallback(PropertyEntity.QUERY_PROPERTIES_BY_ENTITY_ID_KEY,
							PropertyEntity.DELETE_ALL_PROPERTIES_FOR_BEAN_KEY, dbDao, nodePropertyHelper));
		}

		alwaysPersistProperties = new HashSet<>(50);
		for (PersistentPropertiesExtension extension : persistentProperties) {
			alwaysPersistProperties.addAll(extension.getPersistentProperties());
		}
	}

	@Override
	public PropertyModelEntity createPropertiesEntity() {
		return new PropertyEntity();
	}

	@Override
	public PropertyModelValue createPropertiesValue(String typeName, Serializable value) {
		return new PropertyValue(typeName, value);
	}

	@Override
	public EntityLookupCache<PropertyModelKey, Map<String, Serializable>, Serializable> getCache() {
		return cacheContext.getCache(PROPERTY_ENTITY_CACHE);
	}

	@Override
	protected String getDeleteQuery() {
		return PropertyEntity.DELETE_PROPERTIES_KEY;
	}

	@Override
	protected String getDeleteValuesQuery() {
		return PropertyEntity.DELETE_PROPERTIES_VALUES_KEY;
	}

	@Override
	protected String getBatchLoadQuery() {
		return PropertyEntity.QUERY_PROPERTIES_KEY;
	}

	@Override
	public void filterOutForbiddenProperties(Set<String> properties) {
		if (properties != null) {
			properties.removeAll(getNonPersistentProperties());
		}
	}

	@Override
	public void notifyForChanges(PropertyModel model, Map<String, Serializable> propsToDelete,
			Map<String, Serializable> propsToAdd, Map<String, Serializable> actualProperties) {
		Map<String, Serializable> currentProperties = model.getProperties();
		model.setProperties(actualProperties);

		Instance instance = null;
		if (model instanceof Instance) {
			instance = (Instance) model;
		}
		// change event here
		eventService.fire(new PropertiesChangeEvent(instance, propsToAdd, propsToDelete,
				(Operation) Options.CURRENT_OPERATION.get()));
		model.setProperties(currentProperties);
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public PrototypeDefinition getPropertyPrototype(String propertyName, Serializable value, PathElement pathElement,
			DefinitionModel definitionModel) {
		PrototypeDefinition propertyDef = null;
		// added optimization not to fetch multiple types the definition
		// try to fetch the property from the preloaded definition
		if (definitionModel != null) {
			propertyDef = PathHelper.findProperty(definitionModel, pathElement, propertyName);
		}
		// if not found we execute the old algorithm for retrieving property definition
		if (propertyDef == null) {
			// if we have enabled custom saving then we can try to found an proper field to
			// save it into
			propertyDef = definitionService.getDefinitionByValue(propertyName, value);
			if (propertyDef == null) {
				LOGGER.debug("Enabled property for saving fields with no definitions "
						+ "but the property value is not supported: [{}={}]", propertyName, value);
			}
		}
		return propertyDef;
	}
}
