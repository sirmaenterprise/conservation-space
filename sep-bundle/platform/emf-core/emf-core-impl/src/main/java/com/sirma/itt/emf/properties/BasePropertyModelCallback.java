package com.sirma.itt.emf.properties;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.instance.EntityType;
import com.sirma.itt.emf.instance.EntityTypeProvider;
import com.sirma.itt.emf.properties.dao.PropertyModelCallback;
import com.sirma.itt.emf.properties.entity.EntityId;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.properties.model.PropertyModelKey;

/**
 * Implements common functions for {@link PropertyModelCallback}.
 * 
 * @author BBonev
 * @param <E>
 *            the element type
 */
public abstract class BasePropertyModelCallback<E extends PropertyModel> implements
		PropertyModelCallback<E> {

	/** The logger. */
	@Inject
	private Logger logger;
	/** The type provider. */
	@Inject
	protected EntityTypeProvider typeProvider;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PropertyModelKey createModelKey(Entity<?> baseEntity, Long revision) {
		int entityTypeIdentifier = getEntityTypeIdentifier(baseEntity);
		if (entityTypeIdentifier == 0) {
			// the entity is not supported
			return null;
		}

		PathElement pathElement = null;
		if (baseEntity instanceof PathElement) {
			pathElement = (PathElement) baseEntity;
		}
		return new EntityId(baseEntity.getId().toString(), entityTypeIdentifier, revision,
				pathElement);
	}

	/**
	 * Gets the entity type identifier. The Id is used to distinguish between different entity types
	 * when saving and retrieving properties. The default implementation is located into the enum
	 * {@link EntityTypeProvider}. If the entity is not supported then the method will return 0. The
	 * method should be overridden if added new entity type that is not supported by CMF.
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
	 * Sets the properties to model. The model could be a {@link PropertyModel} or {@link List} of
	 * models.
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
			if (properties != null) {
				model.setProperties(properties);
			} else if (model.getProperties() == null) {
				model.setProperties(new LinkedHashMap<String, Serializable>());
			}
		}
		if (value instanceof List) {
			for (Object object : (List) value) {
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
	protected <M extends PropertyModel> void createModel(M model,
			Map<PropertyModelKey, Object> modelMapping) {
		if (model instanceof Entity) {
			PropertyModelKey key = createModelKey((Entity<?>) model, model.getRevision());
			if (key != null) {
				addToModel(key, model, modelMapping, false);
			} else {
				logger.warn("No properties support for " + model.getClass());
			}
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
	@SuppressWarnings("unchecked")
	private <M extends PropertyModel> void addToModel(PropertyModelKey key, M model,
			Map<PropertyModelKey, Object> modelMapping, boolean iterateSubModel) {
		Object value = modelMapping.get(key);
		if (iterateSubModel) {
			createSubModel(model, modelMapping);
		}
		if (value == null) {
			modelMapping.put(key, model);
		} if (value instanceof List) {
			((List) value).add(model);
		}else {
			List<PropertyModel> list = new LinkedList<PropertyModel>();
			list.add((PropertyModel) modelMapping.get(key));
			list.add(model);
			modelMapping.put(key, list);
			// logger.warn("\n>>>>>>>>>>>>>>\n>The given PropertyModel has a duplicate key: "
			// + key + "\n>>>>>>>>>>>>>>");
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
	protected <M extends PropertyModel> void createSubModel(M model,
			Map<PropertyModelKey, Object> modelMapping) {
		if ((model == null) || (model.getProperties() == null)) {
			return;
		}
		for (Serializable serializable : model.getProperties().values()) {
			// if we have complex model we can load all properties of the model also
			if ((serializable instanceof Entity) && (serializable instanceof PropertyModel)) {
				Entity<?> entity = (Entity<?>) serializable;
				if (entity.getId() != null) {
					PropertyModel propertyModel = (PropertyModel) serializable;
					PropertyModelKey key = createModelKey(entity, propertyModel.getRevision());
					if (key != null) {
						addToModel(key, propertyModel, modelMapping, true);
					} else {
						logger.warn("No properties support for " + entity.getClass());
					}
				}
			}
		}
	}

}
