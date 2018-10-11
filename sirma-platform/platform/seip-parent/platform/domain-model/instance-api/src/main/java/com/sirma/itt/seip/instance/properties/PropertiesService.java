package com.sirma.itt.seip.instance.properties;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;

/**
 * Defines a public service for working with properties.
 *
 * @author BBonev
 */
public interface PropertiesService {

	/**
	 * Checks if is model supported for properties persist
	 *
	 * @param <E>
	 *            the element type
	 * @param instance
	 *            the instance
	 * @return true, if is model supported
	 */
	<E extends PropertyModel> boolean isModelSupported(E instance);

	/**
	 * Save properties of the given instance and all of his sub model. The call replaces all properties from the given
	 * tree that are currently in the cache.
	 *
	 * @param <E>
	 *            the element type
	 * @param instance
	 *            the instance
	 */
	<E extends PropertyModel> void saveProperties(E instance);

	/**
	 * Save properties of the given instance and all of his sub model. If the addOnly parameter is <code>true</code>
	 * then the method will merge the properties of the model tree with this in the cache otherwise will replace all.
	 *
	 * @param <E>
	 *            the element type
	 * @param instance
	 *            the instance
	 * @param addOnly
	 *            the add only
	 */
	<E extends PropertyModel> void saveProperties(E instance, boolean addOnly);

	/**
	 * Save properties of the given instance. If the second argument is <code>true</code> then the properties will be
	 * added only and no properties will be removed from the total set except for the one that differ from the base set.
	 * If the third argument is <code>true</code> the service will try to persist all properties for the given instance
	 * graph using the custom property model converter, otherwise the default base level converter will be used.<br>
	 * <b>NOTE: </b> If for any instance there is custom logic that need to be executed in order to be able to persist
	 * the instance properties then it saveFullGraph should be set to <code>true</code>.
	 *
	 * @param <E>
	 *            the element type
	 * @param instance
	 *            the instance
	 * @param addOnly
	 *            the add only
	 * @param saveFullGraph
	 *            the save full graph
	 */
	<E extends PropertyModel> void saveProperties(E instance, boolean addOnly, boolean saveFullGraph);

	/**
	 * Loads all properties for the given instance.
	 *
	 * @param <E>
	 *            the element type
	 * @param instance
	 *            the instance
	 * @see #loadProperties(PropertyModel, boolean)
	 */
	<E extends PropertyModel> void loadProperties(E instance);

	/**
	 * Load properties for the given instance. The second argument defines if all properties are needed or not.
	 *
	 * @param <E>
	 *            the element type
	 * @param instance
	 *            the instance
	 * @param loadAll
	 *            the load all properties or only the base level
	 */
	<E extends PropertyModel> void loadProperties(E instance, boolean loadAll);

	/**
	 * Load all properties for the instances in the given list.<br>
	 * Equivalent to calling the method <code>propertiesService.loadProperties(instances, true)</code>
	 *
	 * @param <E>
	 *            the element type
	 * @param instances
	 *            the instances
	 * @see #loadProperties(List, boolean)
	 */
	<E extends PropertyModel> void loadProperties(List<E> instances);

	/**
	 * Load properties for the given list of instances. The second argument defines if all properties are needed or not.
	 * <p>
	 * <b>IMPORTANT:</b> The method is not capable to load full properties if the instances in the list are of different
	 * types and will result in throwing a {@link ClassCastException}. <br>
	 * If this is the case the second argument should be <code>false</code>. This will force the method to use only the
	 * default properties model converter that can handle most of the instance types. This also means that the
	 * properties in the complex objects will not be loaded of the fill object graph but only the first level (and not
	 * his direct child objects if any).
	 *
	 * @param <E>
	 *            the element type
	 * @param instances
	 *            the list of instances to load the properties
	 * @param loadAll
	 *            the load all properties or only the base level
	 */
	<E extends PropertyModel> void loadProperties(List<E> instances, boolean loadAll);

	/**
	 * Load properties for given instance collection. The operation is executed on batches.
	 *
	 * @param <I>
	 *            instance type
	 * @param instances
	 *            the instances which properties should be loaded
	 */
	<I extends Instance> void loadPropertiesBatch(Collection<I> instances);

	/**
	 * Gets the entity properties.
	 *
	 * @param entity
	 *            the entity
	 * @param path
	 *            the path
	 * @return the entity properties
	 */
	Map<String, Serializable> getEntityProperties(Entity entity, PathElement path);

	/**
	 * Removes the properties for the given entity.
	 *
	 * @param entity
	 *            the entity
	 * @param path
	 *            the path
	 */
	void removeProperties(Entity entity, PathElement path);

	/**
	 * Saves the properties for the given entity properties.
	 *
	 * @param entity
	 *            the target entity
	 * @param path
	 *            the path
	 * @param properties
	 *            the properties to save
	 */
	void saveProperties(Entity entity, PathElement path, Map<String, Serializable> properties);

	/**
	 * Saves the properties for the given entity properties. If the addOnly parameter is <code>true</code> then the
	 * method will merge the properties of the model tree with this in the cache otherwise will replace all.
	 *
	 * @param entity
	 *            the entity
	 * @param path
	 *            the path
	 * @param properties
	 *            the properties
	 * @param addOnly
	 *            the add only
	 */
	void saveProperties(Entity entity, PathElement path, Map<String, ? extends Serializable> properties, boolean addOnly);

}
