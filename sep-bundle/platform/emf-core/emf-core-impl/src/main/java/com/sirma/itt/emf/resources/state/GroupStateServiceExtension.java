package com.sirma.itt.emf.resources.state;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.security.model.EmfGroup;
import com.sirma.itt.emf.state.GenericInstanceStateServiceExtension;
import com.sirma.itt.emf.state.StateServiceExtension;

/**
 * State service extension to handle for {@link EmfGroup} instance. The extension uses the default
 * generic state management.
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypes.GROUP)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 3.1)
public class GroupStateServiceExtension extends GenericInstanceStateServiceExtension<EmfGroup> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<EmfGroup> getInstanceClass() {
		return EmfGroup.class;
	}

}
