package com.sirma.itt.seip.instance.properties;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.domain.instance.PropertyModel;

/**
 * Defines methods for working with various implementations of {@link PropertyModel}.
 *
 * @param <E>
 *            the model type
 * @author BBonev
 */
public interface PropertyModelCallback<E extends PropertyModel> {

	/**
	 * Gets the supported objects by this callback. The method should return the main model and all instance classes
	 * that are supported.
	 *
	 * @return the supported definition
	 */
	Set<Class<?>> getSupportedObjects();

	/**
	 * Checks if the given property model callback can handle the given model
	 *
	 * @param model
	 *            the model to check
	 * @return true, if the callback implementation can handle the given model
	 */
	boolean canHandle(Object model);

	/**
	 * Creates the model key for the given entity and revision.
	 *
	 * @param baseEntity
	 *            the base entity
	 * @return the property model key
	 */
	PropertyModelKey createModelKey(Entity<?> baseEntity);

	/**
	 * Gets the model map.
	 *
	 * @param model
	 *            the model
	 * @return the model
	 */
	Map<PropertyModelKey, Object> getModel(E model);

	/**
	 * Gets the model for persist of the properties.
	 *
	 * @param model
	 *            the model
	 * @return the model for persist
	 */
	Map<PropertyModelKey, Object> getModelForLoading(E model);

	/**
	 * Updates the model properties.
	 *
	 * @param target
	 *            the target
	 * @param properties
	 *            the properties
	 */
	void updateModel(Map<PropertyModelKey, Object> target, Map<PropertyModelKey, Map<String, Serializable>> properties);
}
