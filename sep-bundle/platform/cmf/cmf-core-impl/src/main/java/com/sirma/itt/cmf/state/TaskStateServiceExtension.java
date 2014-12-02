package com.sirma.itt.cmf.state;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.state.StateServiceExtension;
import com.sirma.itt.emf.util.Documentation;

/**
 * Extension implementation for task state management
 * 
 * @author BBonev
 */
@ApplicationScoped
@Documentation("Extension implementation for task state management")
@InstanceType(type = ObjectTypesCmf.WORKFLOW_TASK)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 30)
public class TaskStateServiceExtension extends AbstractTaskStateServiceExtension<TaskInstance> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<TaskInstance> getInstanceClass() {
		return TaskInstance.class;
	}

}
