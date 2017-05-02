package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Resettable;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.OwnedModel;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.model.InstanceEntity;
import com.sirma.itt.seip.serialization.SerializationHelper;

/**
 * Instance converter that uses {@link ObjectMapper} to convert the {@link InstanceEntity} to instance. If as source is
 * passed an instance object the converter will just clone the instance.
 *
 * @author BBonev
 */
@Singleton
@InstanceType(type = ObjectTypes.DEFAULT)
public class InstanceEntityToInstanceConverter implements InstanceConverter {

	private final ObjectMapper dozerMapper;
	private final DictionaryService dictionaryService;
	@Inject
	private SerializationHelper serializationHelper;

	/**
	 * Instantiates a new dozer instance converter.
	 *
	 * @param mapper
	 *            the mapper
	 * @param dictionaryService
	 *            the dictionary service
	 */
	@Inject
	public InstanceEntityToInstanceConverter(ObjectMapper mapper, DictionaryService dictionaryService) {
		dozerMapper = mapper;
		this.dictionaryService = dictionaryService;
	}

	@Override
	public Instance convertToInstance(Entity<? extends Serializable> entity) {
		if (entity == null) {
			return null;
		}
		if (entity instanceof Instance) {
			// the instance is cloned because the convert method is called to fetch instance from
			// the cache to the user but we cannot return the same instance that is in the cache to
			// the user. If used local cache the user may update the instance in the cache.
			return cleanup(cloneInstance((Instance) entity));
		}
		if (entity instanceof InstanceEntity) {
			return convertEntity((InstanceEntity) entity);
		}
		throw new EmfRuntimeException("Not supported entity: " + entity);
	}

	private <T> T cleanup(T cloneInstance) {
		// reset any parent reference if any
		if (cloneInstance instanceof OwnedModel) {
			cleanup(((OwnedModel) cloneInstance).getOwningReference());
		}
		if (cloneInstance instanceof Resettable) {
			((Resettable) cloneInstance).reset();
			return cloneInstance;
		}
		return cloneInstance;
	}

	/**
	 * Clone instance.
	 *
	 * @param <I>
	 *            the generic type
	 * @param entity
	 *            the entity
	 * @return the instance
	 */
	protected <I> I cloneInstance(I entity) {
		return serializationHelper.copy(entity);
	}

	/**
	 * Convert entity.
	 *
	 * @param entity
	 *            the entity
	 * @return the instance
	 */
	private Instance convertEntity(InstanceEntity entity) {
		Class<?> typeDefinition = getTargetClass(entity);
		return (Instance) dozerMapper.map(entity, typeDefinition);
	}

	/**
	 * Gets the target class for the given instance entity
	 *
	 * @param entity
	 *            the entity
	 * @return the target class
	 */
	private Class<?> getTargetClass(InstanceEntity entity) {
		Long instanceType = entity.getInstanceType();
		if (instanceType == null) {
			throw new EmfRuntimeException("Instance type is required field!");
		}
		DataTypeDefinition typeDefinition = dictionaryService.getDataTypeDefinition(instanceType);
		if (typeDefinition == null) {
			throw new EmfRuntimeException("Undefined instance type: " + instanceType);
		}
		if (!Instance.class.isAssignableFrom(typeDefinition.getJavaClass())) {
			throw new EmfRuntimeException("The type " + instanceType + " does not point to instance object but to: "
					+ typeDefinition.getJavaClassName());
		}
		return typeDefinition.getJavaClass();
	}

}
