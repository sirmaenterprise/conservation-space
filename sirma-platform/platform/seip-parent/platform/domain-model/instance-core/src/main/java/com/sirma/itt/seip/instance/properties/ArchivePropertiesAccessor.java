package com.sirma.itt.seip.instance.properties;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PrototypeDefinition;
import com.sirma.itt.seip.domain.instance.ArchivedDataAccess;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.instance.properties.entity.ArchivedPropertyEntity;
import com.sirma.itt.seip.instance.properties.entity.ArchivedPropertyValue;
import com.sirma.itt.seip.instance.properties.entity.NodePropertyHelper;

/**
 * Storage accessor for default runtime properties. The accessor defines a cache that is used to store the properties of
 * the loaded instances.
 *
 * @author BBonev
 */
@ArchivedDataAccess
public class ArchivePropertiesAccessor extends BasePropertyStorageAccessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(ArchivePropertiesAccessor.class);

	@Inject
	private NodePropertyHelper nodePropertyHelper;
	private EntityLookupCache<PropertyModelKey, Map<String, Serializable>, Serializable> emptyCache;

	/**
	 * Initialize the cache instance
	 */
	@Override
	@PostConstruct
	protected void initialize() {
		super.initialize();
		// for now we does not have a cache
		emptyCache = new EntityLookupCache<>(
				new PropertiesLookupCallback(ArchivedPropertyEntity.QUERY_ARCHIVED_PROPERTIES_BY_ENTITY_ID_KEY,
						ArchivedPropertyEntity.DELETE_ALL_PROPERTIES_FOR_ARCHIVED_BEAN_KEY, dbDao, nodePropertyHelper));
	}

	@Override
	public PropertyModelEntity createPropertiesEntity() {
		return new ArchivedPropertyEntity();
	}

	@Override
	public PropertyModelValue createPropertiesValue(String typeName, Serializable value) {
		return new ArchivedPropertyValue(typeName, value);
	}

	@Override
	public EntityLookupCache<PropertyModelKey, Map<String, Serializable>, Serializable> getCache() {
		return emptyCache;
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
		// nothing to do here no notification for archived properties - this should not trigger
		// audit events
	}

	@Override
	protected String getDeleteQuery() {
		return ArchivedPropertyEntity.DELETE_ARCHIVED_PROPERTIES_KEY;
	}

	@Override
	protected String getDeleteValuesQuery() {
		return ArchivedPropertyEntity.DELETE_ARCHIVED_PROPERTY_VALUES_KEY;
	}

	@Override
	protected String getBatchLoadQuery() {
		return ArchivedPropertyEntity.QUERY_ARCHIVED_PROPERTIES_KEY;
	}

	@Override
	protected Logger getLogger() {
		return LOGGER;
	}

	@Override
	public PrototypeDefinition getPropertyPrototype(String propertyName, Serializable value, PathElement pathElement,
			DefinitionModel definitionModel) {
		return definitionService.getDefinitionByValue(propertyName, value);
	}

}
