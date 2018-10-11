package com.sirma.itt.objects.services.impl;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.state.StateServiceExtension;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Extension point for state service to support object states management
 *
 * @author BBonev
 */
@InstanceType(type = ObjectTypes.OBJECT)
@Extension(target = StateServiceExtension.TARGET_NAME, order = Double.MAX_VALUE)
public class ObjectStateServiceExtension extends AbstractObjectStateServiceExtension {

	@Override
	public boolean canHandle(String targetType) {
		// default provider
		return true;
	}

	@Override
	protected String getInstanceType() {
		return "any";
	}

}
