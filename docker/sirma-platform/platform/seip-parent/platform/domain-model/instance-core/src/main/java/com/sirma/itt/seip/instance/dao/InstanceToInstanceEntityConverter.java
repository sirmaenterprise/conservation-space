package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.model.InstanceEntity;
import com.sirma.itt.seip.model.LinkSourceId;

/**
 * Instance converter that uses dozer {@link ObjectMapper} to convert the {@link InstanceEntity} to instance. If as
 * source is passed an instance object the converter will just clone the instance.
 *
 * @author BBonev
 */
@Singleton
@InstanceType(type = ObjectTypes.DEFAULT)
public class InstanceToInstanceEntityConverter implements EntityConverter {

	private final ObjectMapper dozerMapper;
	private final DefinitionService definitionService;

	/**
	 * Instantiates a new dozer instance converter.
	 *
	 * @param mapper
	 *            the mapper
	 * @param definitionService
	 *            the definition service
	 */
	@Inject
	public InstanceToInstanceEntityConverter(ObjectMapper mapper, DefinitionService definitionService) {
		dozerMapper = mapper;
		this.definitionService = definitionService;
	}

	@Override
	public Entity<? extends Serializable> convertToEntity(Instance instance) {
		if (instance == null) {
			return null;
		}

		InstanceEntity entity = dozerMapper.map(instance, InstanceEntity.class);
		LinkSourceId owningInstance = entity.getOwning();
		if (owningInstance != null) {
			owningInstance.reset();
		}
		Long typeDefinition = getTargetClass(instance);
		entity.setInstanceType(typeDefinition);
		return entity;
	}

	/**
	 * Gets the target class for the given instance entity
	 *
	 * @param instance
	 *            the entity
	 * @return the target class
	 */
	private Long getTargetClass(Instance instance) {
		Class<? extends Instance> instanceClass = instance.getClass();
		DataTypeDefinition typeDefinition = definitionService.getDataTypeDefinition(instanceClass.getName());
		if (typeDefinition == null) {
			throw new EmfRuntimeException("Undefined instance type: " + instanceClass.getName());
		}
		if (!Instance.class.isAssignableFrom(typeDefinition.getJavaClass())) {
			throw new EmfRuntimeException("The type " + instanceClass.getName()
					+ " does not point to instance object but to: " + typeDefinition.getJavaClassName());
		}
		return typeDefinition.getId();
	}

}
