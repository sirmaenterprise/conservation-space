package com.sirma.itt.cmf.services.impl.dao;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.entity.WorkflowInstanceContextEntity;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.properties.BasePropertyModelCallback;
import com.sirma.itt.emf.properties.dao.PropertyModelCallback;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Workflow instance property model callback.
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypesCmf.WORKFLOW)
public class WorkflowInstancePropertyModelCallback extends
		BasePropertyModelCallback<WorkflowInstanceContext> implements
		PropertyModelCallback<WorkflowInstanceContext> {

	private static final Set<Class<?>> SUPPORTED_OBJECTS;
	static {
		SUPPORTED_OBJECTS = CollectionUtils.createHashSet(5);
		SUPPORTED_OBJECTS.add(WorkflowInstanceContext.class);
		SUPPORTED_OBJECTS.add(WorkflowInstanceContextEntity.class);
	}

	@Override
	public Set<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	@Override
	public boolean canHandle(Object model) {
		return (model instanceof WorkflowInstanceContext)
				|| (model instanceof WorkflowInstanceContextEntity);
	}

}
