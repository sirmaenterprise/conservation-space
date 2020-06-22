package com.sirma.itt.seip.instance.properties;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The Interface RichtextPropertiesDao. Provides functionality to retrieve, persist and manage richtext fields.
 *
 * @author S.Djulgerova
 */
public interface RichtextPropertiesDao {

	/**
	 * Saves property html value, or updates it if it already exists.
	 * 
	 * @param instanceId
	 *            instance id
	 * @param propertyId
	 *            property id
	 * @param value
	 *            property value
	 */
	void saveOrUpdate(String instanceId, Long propertyId, String value);

	/**
	 * Fetch properties value by given instance id
	 * 
	 * @param instanceId
	 *            instance id
	 * @return properties map for given instance
	 */
	Map<String, Serializable> fetchByInstanceId(String instanceId);

	/**
	 * Fetch properties value by list of instance ids
	 * 
	 * @param ids
	 *            instance ids
	 * @return properties map for each instance
	 */
	<S extends Serializable> Map<String, Map<String, Serializable>> fetchByInstanceIds(List<S> ids);

	/**
	 * Deletes property value by given id.
	 *
	 * @param instanceId
	 *            instance id
	 */
	void delete(String instanceId);

}
