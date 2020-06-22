package com.sirma.itt.seip.instance.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.instance.properties.PropertiesStorageAccess;
import com.sirma.itt.seip.instance.properties.PropertyEntryKey;
import com.sirma.itt.seip.instance.properties.PropertyModelEntity;
import com.sirma.itt.seip.instance.properties.PropertyModelKey;
import com.sirma.itt.seip.instance.properties.PropertyModelValue;
import com.sirma.itt.seip.instance.properties.RelationalNonPersistentPropertiesExtension;
import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * Base helper class to facilitate common logic for properties access.
 *
 * @author BBonev
 */
public abstract class BasePropertyStorageAccessor implements PropertiesStorageAccess {

	private static final String BEAN_TYPE = "beanType";
	private static final String BEAN_ID = "beanId";

	@Inject
	protected DbDao dbDao;
	@Inject
	protected DefinitionService definitionService;

	/** The non persistent properties. */
	@Inject
	@ExtensionPoint(value = RelationalNonPersistentPropertiesExtension.TARGET_NAME)
	private Iterable<RelationalNonPersistentPropertiesExtension> nonPersistentProperties;
	private Set<String> forbiddenProperties = new LinkedHashSet<>(50);

	@PostConstruct
	protected void initialize() {
		for (RelationalNonPersistentPropertiesExtension extension : nonPersistentProperties) {
			forbiddenProperties.addAll(extension.getNonPersistentProperties());
		}
	}

	@Override
	public void insertProperties(PropertyModelKey entityId, Map<PropertyEntryKey, PropertyModelValue> newPropsRaw) {
		for (Entry<PropertyEntryKey, PropertyModelValue> entry : newPropsRaw.entrySet()) {
			PropertyModelEntity propertyEntity = createPropertiesEntity();
			propertyEntity.setKey(entry.getKey());
			propertyEntity.setValue(entry.getValue());
			propertyEntity.setEntityId(entityId);
			dbDao.saveOrUpdate(propertyEntity);
		}
	}

	@Override
	public void deleteProperties(PropertyModelKey entityId, Set<Long> propStringIdsToDelete) {
		List<Pair<String, Object>> args = new ArrayList<>(3);
		args.add(new Pair<>("id", propStringIdsToDelete));
		args.add(new Pair<>(BEAN_ID, entityId.getBeanId()));
		args.add(new Pair<>(BEAN_TYPE, entityId.getBeanType()));
		// explicitly delete values as the query depends on the properties, and then delete the properties
		int removedValues = dbDao.executeUpdate(getDeleteValuesQuery(), args);
		int removedProperties = dbDao.executeUpdate(getDeleteQuery(), args);
		getLogger().debug("Removed {}/{} properties/values for {}", removedProperties, removedValues, entityId);
	}

	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	protected abstract Logger getLogger();

	/**
	 * Gets the delete query.
	 *
	 * @return the delete query
	 */
	protected abstract String getDeleteQuery();
	/**
	 * Gets the delete query for property values.
	 *
	 * @return the delete query
	 */
	protected abstract String getDeleteValuesQuery();

	@Override
	public <P extends PropertyModelEntity> List<P> batchLoadProperties(Integer beanType, Collection<String> beanIds) {
		List<Pair<String, Object>> params = new ArrayList<>(2);
		params.add(new Pair<String, Object>(BEAN_TYPE, beanType));
		params.add(new Pair<String, Object>(BEAN_ID, beanIds));
		return dbDao.fetchWithNamed(getBatchLoadQuery(), params);
	}

	/**
	 * Gets the non persistent properties.
	 *
	 * @return the non persistent properties
	 */
	protected Set<String> getNonPersistentProperties() {
		return forbiddenProperties;
	}

	/**
	 * Gets the batch load query.
	 *
	 * @return the batch load query
	 */
	protected abstract String getBatchLoadQuery();

}