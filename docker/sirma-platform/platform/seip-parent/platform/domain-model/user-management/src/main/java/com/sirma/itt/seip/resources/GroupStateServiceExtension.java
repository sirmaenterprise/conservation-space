package com.sirma.itt.seip.resources;

import java.util.Set;

import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.state.GenericInstanceStateServiceExtension;
import com.sirma.itt.seip.instance.state.PrimaryStates;
import com.sirma.itt.seip.instance.state.StateServiceExtension;
import com.sirma.itt.seip.plugin.Extension;

/**
 * State service extension to handle for {@link EmfGroup} instance. The extension uses the default generic state
 * management.
 *
 * @author BBonev
 */
@InstanceType(type = ResourceInstanceTypes.GROUP)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 3.1)
public class GroupStateServiceExtension extends GenericInstanceStateServiceExtension {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "group.state.active", type = Set.class, defaultValue = PrimaryStates.ACTIVE_STATES_CONFIG
			+ ",ACTIVE", label = "Set of active group states")
	private ConfigurationProperty<Set<String>> activeStates;

	@Override
	protected String getInstanceType() {
		return ResourceInstanceTypes.GROUP;
	}

	@Override
	protected Set<String> getActiveStates() {
		if (activeStates.isNotSet()) {
			return super.getActiveStates();
		}
		return activeStates.get();
	}
}
