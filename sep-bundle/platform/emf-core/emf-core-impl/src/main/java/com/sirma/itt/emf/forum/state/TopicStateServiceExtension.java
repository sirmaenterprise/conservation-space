package com.sirma.itt.emf.forum.state;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.state.GenericInstanceStateServiceExtension;
import com.sirma.itt.emf.state.StateServiceExtension;

/**
 * Topic instance state service extension.
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypes.TOPIC)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 5)
public class TopicStateServiceExtension extends GenericInstanceStateServiceExtension<TopicInstance> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<TopicInstance> getInstanceClass() {
		return TopicInstance.class;
	}

}
