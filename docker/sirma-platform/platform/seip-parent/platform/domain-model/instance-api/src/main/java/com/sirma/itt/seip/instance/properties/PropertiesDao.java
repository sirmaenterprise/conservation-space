package com.sirma.itt.seip.instance.properties;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.instance.PropertyModel;

/**
 * The Interface PropertiesDao. Access to low level mechanism for persisting and retrieving properties properties. To
 * define non persistent properties an extension is provided: {@link RelationalNonPersistentPropertiesExtension}.
 *
 * @author BBonev
 */
public interface PropertiesDao {

	/**
	 * Save properties.
	 *
	 * @param <E>
	 *            the element type
	 * @param model
	 *            the model
	 * @param callback
	 *            the callback
	 * @param access
	 *            the access instance to use
	 */
	<E extends PropertyModel> void saveProperties(E model, PropertyModelCallback<E> callback,
			PropertiesStorageAccess access);

	/**
	 * Save properties for the given model using the given callback to parse the model. If addOnly property is
	 * <code>true</code> then only modified properties will be saved and deletion will not be performed.
	 *
	 * @param <E>
	 *            the element type
	 * @param model
	 *            the model
	 * @param addOnly
	 *            the add only
	 * @param callback
	 *            the callback
	 * @param access
	 *            accessor used to access the properties store
	 */
	<E extends PropertyModel> void saveProperties(E model, boolean addOnly, PropertyModelCallback<E> callback,
			PropertiesStorageAccess access);

	/**
	 * Load properties.
	 *
	 * @param <E>
	 *            the element type
	 * @param model
	 *            the model
	 * @param callback
	 *            the callback
	 * @param access
	 *            accessor used to access the properties store
	 */
	<E extends PropertyModel> void loadProperties(E model, PropertyModelCallback<E> callback,
			PropertiesStorageAccess access);

	/**
	 * Load properties.
	 *
	 * @param <E>
	 *            the element type
	 * @param cases
	 *            the cases
	 * @param callback
	 *            the callback
	 * @param access
	 *            accessor used to access the properties store
	 */
	<E extends PropertyModel> void loadProperties(List<E> cases, PropertyModelCallback<E> callback,
			PropertiesStorageAccess access);

	/**
	 * Gets the entity properties.
	 *
	 * @param entity
	 *            the entity
	 * @param path
	 *            the path
	 * @param callback
	 *            the callback
	 * @param access
	 *            accessor used to access the properties store
	 * @return the entity properties
	 */
	@SuppressWarnings("rawtypes")
	Map<String, Serializable> getEntityProperties(Entity entity, PathElement path,
			PropertyModelCallback<PropertyModel> callback, PropertiesStorageAccess access);

	/**
	 * Removes the properties for the given entity.
	 *
	 * @param entity
	 *            the entity
	 * @param path
	 *            the path
	 * @param callback
	 *            the callback
	 * @param access
	 *            accessor used to access the properties store
	 */
	@SuppressWarnings("rawtypes")
	void removeProperties(Entity entity, PathElement path, PropertyModelCallback<PropertyModel> callback,
			PropertiesStorageAccess access);

	/**
	 * Saves the properties for the given entity properties.
	 *
	 * @param entity
	 *            the target entity
	 * @param path
	 *            the path
	 * @param properties
	 *            the properties to save
	 * @param callback
	 *            the callback
	 * @param access
	 *            accessor used to access the properties store
	 */
	@SuppressWarnings("rawtypes")
	void saveProperties(Entity entity, PathElement path, Map<String, Serializable> properties,
			PropertyModelCallback<PropertyModel> callback, PropertiesStorageAccess access);

	/**
	 * Saves the properties for the given entity properties. If addOnly property is <code>true</code> then the method
	 * call will merge only modified properties and will not delete any.
	 *
	 * @param entity
	 *            the entity
	 * @param path
	 *            the path
	 * @param properties
	 *            the properties
	 * @param addOnly
	 *            the add only operation
	 * @param callback
	 *            the callback
	 * @param access
	 *            accessor used to access the properties store
	 */
	@SuppressWarnings("rawtypes")
	void saveProperties(Entity entity, PathElement path, Map<String, ? extends Serializable> properties, boolean addOnly,
			PropertyModelCallback<PropertyModel> callback, PropertiesStorageAccess access);
}
