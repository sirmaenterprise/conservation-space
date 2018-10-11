package com.sirma.itt.emf.link.state;

import java.util.Collections;
import java.util.Set;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.relation.LinkInstance;
import com.sirma.itt.seip.instance.state.GenericInstanceStateServiceExtension;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.instance.state.StateServiceExtension;
import com.sirma.itt.seip.plugin.Extension;

/**
 * State service extension to handle for {@link LinkInstance} instance. The extension uses the default generic state
 * management.
 *
 * @author BBonev
 */
@InstanceType(type = ObjectTypes.LINK)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 4)
public class LinkInstanceStateServiceExtension extends GenericInstanceStateServiceExtension {

	@Override
	public String getPrimaryState(Instance instance) {
		String state = super.getPrimaryState(instance);
		if (state == null) {
			// the links are active if not specified otherwise
			boolean isActive = instance.getBoolean("emf:isActive", true);
			if (isActive) {
				ConfigurationProperty<String> property = getStateTypeMapping().get(PrimaryStates.IN_PROGRESS_KEY);
				if (property != null) {
					return property.get();
				}
				return null;
			}
		}
		return state;
	}

	@Override
	protected String getInstanceType() {
		return ObjectTypes.LINK_REFERENCE;
	}

	@Override
	protected Set<String> getActiveStates() {
		return Collections.emptySet();
	}

}
