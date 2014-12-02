package com.sirma.itt.cmf.services.impl.dao;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.domain.model.Entity;
import com.sirma.itt.emf.instance.dao.InstanceType;

/**
 * Service that handles the Folder instances
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesCmf.FOLDER)
public class FolderInstanceDao extends BaseSectionInstanceDao<FolderInstance, GenericDefinition> {

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public FolderInstance persistChanges(FolderInstance instance) {
		return super.persistChanges(instance);
	}

	@Override
	@SuppressWarnings("rawtypes")
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Entity saveEntity(Entity entity) {
		return super.saveEntity(entity);
	}

	@Override
	protected Class<FolderInstance> getInstanceClass() {
		return FolderInstance.class;
	}

	@Override
	protected Class<GenericDefinition> getDefinitionClass() {
		return GenericDefinition.class;
	}
}
