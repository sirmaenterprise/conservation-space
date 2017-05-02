package com.sirma.itt.seip.resources;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
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
 * State service extension to handle for {@link EmfUser} instance. The extension uses the default generic state
 * management.
 *
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ResourceInstanceTypes.USER)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 3)
public class UserStateServiceExtension extends GenericInstanceStateServiceExtension<EmfUser> {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "user.state.active", type = Set.class, defaultValue = PrimaryStates.ACTIVE_STATES_CONFIG
			+ ",ACTIVE", label = "Set of active user states")
	private ConfigurationProperty<Set<String>> activeStates;

	@Override
	protected Class<EmfUser> getInstanceClass() {
		return EmfUser.class;
	}

	@Override
	protected Set<String> getActiveStates() {
		if (activeStates.isNotSet()) {
			return super.getActiveStates();
		}
		return activeStates.get();
	}
}
