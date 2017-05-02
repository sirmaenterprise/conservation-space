package com.sirma.itt.seip.instance.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.instance.properties.PropertyEntryKey;
import com.sirma.itt.seip.instance.properties.PropertyModelEntity;
import com.sirma.itt.seip.instance.properties.PropertyModelKey;
import com.sirma.itt.seip.instance.properties.PropertyModelValue;
import com.sirma.itt.seip.instance.properties.entity.NodePropertyHelper;

/**
 * Cache callback used to fetch properties by {@link PropertyModelKey}. The cache stores the converted to internal model
 * properties. The callback supports property retrieval and deletion but no creation due to the fact that the stored
 * cache value does not provide means to persist values.
 *
 * @author BBonev
 */
public class PropertiesLookupCallback
		extends EntityLookupCallbackDAOAdaptor<PropertyModelKey, Map<String, Serializable>, Serializable> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BasePropertyStorageAccessor.class);
	private static final boolean TRACE = LOGGER.isTraceEnabled();
	private static final String BEAN_TYPE = "beanType";
	private static final String BEAN_ID = "beanId";

	private String lookupQuery;
	private DbDao dbDao;
	private String deleteQuery;
	private NodePropertyHelper nodePropertyHelper;

	/**
	 * Instantiates a new properties lookup callback.
	 *
	 * @param lookupQuery
	 *            the lookup query used to find the properties by {@link PropertyModelKey}.
	 * @param deleteQuery
	 *            the delete query to remove all properties lookup by the first query
	 * @param dbDao
	 *            the db dao used to access the database
	 * @param nodePropertyHelper
	 *            used for property conversion
	 */
	public PropertiesLookupCallback(String lookupQuery, String deleteQuery, DbDao dbDao,
			NodePropertyHelper nodePropertyHelper) {
		this.lookupQuery = lookupQuery;
		this.deleteQuery = deleteQuery;
		this.dbDao = dbDao;
		this.nodePropertyHelper = nodePropertyHelper;
	}

	@Override
	public Pair<PropertyModelKey, Map<String, Serializable>> findByKey(PropertyModelKey key) {
		if (lookupQuery == null) {
			return null;
		}
		List<Pair<String, Object>> args = new ArrayList<>(2);
		args.add(new Pair<String, Object>(BEAN_ID, key.getBeanId()));
		args.add(new Pair<String, Object>(BEAN_TYPE, key.getBeanType()));
		List<PropertyModelEntity> resultList = dbDao.fetchWithNamed(lookupQuery, args);
		if (resultList.isEmpty()) {
			return null;
		}
		if (TRACE) {
			LOGGER.trace("Fetched for key {} {} results", key, resultList.size());
		}
		Map<PropertyEntryKey, PropertyModelValue> propertyValues = new LinkedHashMap<>((int) (resultList.size() * 1.2),
				1f);
		for (PropertyModelEntity propertyEntity : resultList) {
			propertyValues.put(propertyEntity.getKey(), propertyEntity.getValue());
		}
		if (TRACE) {
			LOGGER.trace("Returning unique results " + propertyValues.size());
		}
		Map<String, Serializable> publicProperties = nodePropertyHelper.convertToPublicProperties(propertyValues);
		return new Pair<>(key, Collections.unmodifiableMap(publicProperties));
	}

	@Override
	public Pair<PropertyModelKey, Map<String, Serializable>> createValue(Map<String, Serializable> value) {
		throw new UnsupportedOperationException("A node always has a 'map' of properties.");
	}

	@Override
	public int deleteByKey(PropertyModelKey key) {
		if (deleteQuery == null) {
			return 0;
		}
		List<Pair<String, Object>> args = new ArrayList<>(2);
		args.add(new Pair<String, Object>(BEAN_ID, key.getBeanId()));
		args.add(new Pair<String, Object>(BEAN_TYPE, key.getBeanType()));
		int update = dbDao.executeUpdate(deleteQuery, args);
		LOGGER.debug("Removed {} properties for {}", update, key);
		return update;
	}

}