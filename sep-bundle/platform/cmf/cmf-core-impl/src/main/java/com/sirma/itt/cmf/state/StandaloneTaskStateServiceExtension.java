package com.sirma.itt.cmf.state;


import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.state.StateServiceExtension;
import com.sirma.itt.emf.util.Documentation;

/**
 * Extension implementation for standalone task state management.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Documentation("Extension implementation for standalone task state management")
@InstanceType(type = ObjectTypesCmf.STANDALONE_TASK)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 32)
public class StandaloneTaskStateServiceExtension extends
		AbstractTaskStateServiceExtension<StandaloneTaskInstance> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<StandaloneTaskInstance> getInstanceClass() {
		return StandaloneTaskInstance.class;
	}
}
