package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.model.InstanceEntity;

/**
 * Common instance specific operations
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = "CommonInstance")
public class CommonInstanceDao extends BaseInstanceDaoImpl<InstanceEntity, String, String, DefinitionModel> {

	@Override
	protected void updateCreatorAndModifierInfo(Instance instance) {
		// no need to update anything
	}

	@Override
	public void synchRevisions(Instance instance, Long revision) {
		instance.setRevision(revision);
	}

	@Override
	protected void populateInstanceForModel(Instance instance, DefinitionModel model) {
		// nothing to do here
	}

	@Override
	protected Class<CommonInstance> getInstanceClass() {
		return CommonInstance.class;
	}

	@Override
	protected Class<InstanceEntity> getEntityClass() {
		return InstanceEntity.class;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <I extends Serializable> Class<I> getPrimaryIdType() {
		return (Class<I>) String.class;
	}
}
