package com.sirma.itt.objects.services.impl;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.state.StateServiceExtension;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Extension point for state service to support class states management
 *
 * @author bbanchev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypes.CLASS)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 151)
public class ClassStateServiceExtension extends AbstractObjectStateServiceExtension<ClassInstance> {

	@Override
	protected Class<ClassInstance> getInstanceClass() {
		return ClassInstance.class;
	}

}
