package com.sirma.itt.seip.instance.properties;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.instance.properties.EntityType;
import com.sirma.itt.seip.instance.properties.EntityTypeProvider;
import com.sirma.itt.seip.instance.properties.PropertyModelCallback;
import com.sirma.itt.seip.instance.properties.PropertyModelKey;
import com.sirma.itt.seip.instance.properties.entity.EntityId;

/**
 * Implements common functions for {@link PropertyModelCallback}.
 *
 * @author BBonev
 * @param <E>
 *            the element type
 */
public abstract class BasePropertyModelCallback<E extends PropertyModel> implements PropertyModelCallback<E> {

	private static final Logger LOGGER = LoggerFactory.getLogger(BasePropertyModelCallback.class);
	/** The type provider. */
	@Inject
	protected EntityTypeProvider typeProvider;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyModelKey createModelKey(Entity<?> baseEntity) {
		int entityTypeIdentifier = getEntityTypeIdentifier(baseEntity);
		if (entityTypeIdentifier == 0) {
			// the entity is not supported
			return null;
		}

		PathElement pathElement = null;
		if (baseEntity instanceof PathElement) {
			pathElement = (PathElement) baseEntity;
		}
		return new EntityId(baseEntity.getId().toString(), entityTypeIdentifier, pathElement);
	}

	/**
	 * Gets the entity type identifier. The Id is used to distinguish between different entity types when saving and
	 * retrieving properties. The default implementation is located into the enum {@link EntityTypeProvider}. If the
	 * entity is not supported then the method will return 0. The method should be overridden if added new entity type
	 * that is not supported by CMF.
	 *
	 * @param entity
	 *            the entity
	 * @return the entity type identifier
	 */
	protected int getEntityTypeIdentifier(Entity<?> entity) {
		EntityType type = typeProvider.getEntityType(entity);
		if (type == null) {
			return 0;
		}
		return type.getTypeId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<PropertyModelKey, Object> getModel(E model) {
		Map<PropertyModelKey, Object> map = new LinkedHashMap<>();
		createSubModel(model, map);
		createModel(model, map);
		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<PropertyModelKey, Object> getModelForLoading(E model) {
		Map<PropertyModelKey, Object> map = new LinkedHashMap<>();
		// currently this will work only of we need to refresh the properties of the model
		// otherwise the model's properties are empty
		createSubModel(model, map);
		createModel(model, map);
		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateModel(Map<PropertyModelKey, Object> target,
			Map<PropertyModelKey, Map<String, Serializable>> properties) {

		for (Entry<PropertyModelKey, Map<String, Serializable>> entry : properties.entrySet()) {
			Object value = target.remove(entry.getKey());
			setPropertiesToModel(value, entry.getValue());
		}
		// if for some instances the model was not fetched then we populate empty maps
		for (Entry<PropertyModelKey, Object> entry : target.entrySet()) {
			setPropertiesToModel(entry.getValue(), null);
		}
	}

	/**
	 * Sets the properties to model. The model could be a {@link PropertyModel} or {@link List} of models.
	 *
	 * @param value
	 *            the value
	 * @param properties
	 *            the properties
	 */
	@SuppressWarnings("rawtypes")
	private void setPropertiesToModel(Object value, Map<String, Serializable> properties) {
		if (value instanceof PropertyModel) {
			PropertyModel model = (PropertyModel) value;
			// this will force properties initialization
			Map<String, Serializable> toAdd = properties == null ? Collections.emptyMap() : properties;
			// the previous logic was overriding the map in place
			model.addAllProperties(toAdd);
		}
		if (value instanceof Collection) {
			for (Object object : (Collection) value) {
				setPropertiesToModel(object, properties);
			}
		}
	}

	/**
	 * Creates the model and populates the model mapping.
	 *
	 * @param <M>
	 *            the generic type
	 * @param model
	 *            the model to iterate
	 * @param modelMapping
	 *            the target model mapping
	 */
	protected <M extends PropertyModel> void createModel(M model, Map<PropertyModelKey, Object> modelMapping) {
		if (model instanceof Entity) {
			createModel((Entity<?>) model, model, modelMapping, false);
		}
	}

	/**
	 * Creates the model.
	 *
	 * @param <M>
	 *            the generic type
	 * @param entity
	 *            the entity
	 * @param model
	 *            the model
	 * @param modelMapping
	 *            the model mapping
	 * @param iterateSubModel
	 *            the iterate sub model
	 */
	private <M extends PropertyModel> void createModel(Entity<?> entity, M model,
			Map<PropertyModelKey, Object> modelMapping, boolean iterateSubModel) {
		PropertyModelKey key = createModelKey(entity);
		if (key != null) {
			addToModel(key, model, modelMapping, iterateSubModel);
		} else {
			LOGGER.warn("No properties support for " + entity.getClass());
		}
	}

	/**
	 * Adds the to model.
	 *
	 * @param <M>
	 *            the generic type
	 * @param key
	 *            the key
	 * @param model
	 *            the model
	 * @param modelMapping
	 *            the model mapping
	 * @param iterateSubModel
	 *            the iterate sub model
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <M extends PropertyModel> void addToModel(PropertyModelKey key, M model,
			Map<PropertyModelKey, Object> modelMapping, boolean iterateSubModel) {
		Object value = modelMapping.get(key);
		if (iterateSubModel) {
			createSubModel(model, modelMapping);
		}
		if (value == null) {
			modelMapping.put(key, model);
		} else if (value instanceof List) {
			((List) value).add(model);
		} else {
			List<PropertyModel> list = new LinkedList<>();
			list.add((PropertyModel) modelMapping.get(key));
			list.add(model);
			modelMapping.put(key, list);
		}
	}

	/**
	 * Creates the sub model of the given model and adds it to the given model mapping.
	 *
	 * @param <M>
	 *            the generic type
	 * @param model
	 *            the model to iterate and check
	 * @param modelMapping
	 *            the target model mapping
	 */
	protected <M extends PropertyModel> void createSubModel(M model, Map<PropertyModelKey, Object> modelMapping) {
		if (model == null || model.getProperties() == null) {
			return;
		}
		for (Serializable serializable : model.getProperties().values()) {
			// if we have complex model we can load all properties of the model also
			if (serializable instanceof Entity && serializable instanceof PropertyModel) {
				Entity<?> entity = (Entity<?>) serializable;
				if (entity.getId() != null) {
					PropertyModel propertyModel = (PropertyModel) serializable;
					createModel(entity, propertyModel, modelMapping, true);
				}
			}
		}
	}

}
