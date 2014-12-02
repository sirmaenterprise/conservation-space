package com.sirma.itt.emf.link.state;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.link.LinkInstance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.state.GenericInstanceStateServiceExtension;
import com.sirma.itt.emf.state.PrimaryStateType;
import com.sirma.itt.emf.state.StateServiceExtension;

/**
 * State service extension to handle for {@link LinkInstance} instance. The extension uses the
 * default generic state management.
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypes.LINK)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 4)
public class LinkInstanceStateServiceExtension extends
		GenericInstanceStateServiceExtension<LinkInstance> {

	@Override
	public String getPrimaryState(LinkInstance instance) {
		String state = super.getPrimaryState(instance);
		if (state == null) {
			Serializable serializable = instance.getProperties().get("emf:isActive");
			if ((serializable == null) || Boolean.TRUE.equals(serializable)) {
				String string = getStateTypeMapping().get(PrimaryStateType.IN_PROGRESS);
				return string;
			}
		}
		return state;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<LinkInstance> getInstanceClass() {
		return LinkInstance.class;
	}

}
