package com.sirma.itt.objects.services.impl;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.state.StateServiceExtension;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Extension point for state service to support object states management
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypes.OBJECT)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 150)
public class ObjectStateServiceExtension extends AbstractObjectStateServiceExtension<ObjectInstance> {

	@Override
	protected Class<ObjectInstance> getInstanceClass() {
		return ObjectInstance.class;
	}

}
