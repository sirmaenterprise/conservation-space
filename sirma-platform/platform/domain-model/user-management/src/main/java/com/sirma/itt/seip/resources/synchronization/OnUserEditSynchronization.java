package com.sirma.itt.seip.resources.synchronization;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.LANGUAGE;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.io.Serializable;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.event.InstancePersistedEvent;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;

/**
 * Observer that listens for user edits done by the user to synchronize language, email and other specific properties to
 * relational DB
 *
 * @author BBonev
 */
@Singleton
public class OnUserEditSynchronization {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "user.sync.properties", type = Set.class, defaultValue = LANGUAGE, sensitive = true, label = "Set of properties that should be synchronized on edit and available in the user info. Different properties should be separated by comma (,)")
	private ConfigurationProperty<Set<String>> propertiesToSync;

	@Inject
	private ResourceService resourceService;

	/**
	 * On user changed via rest call should trigger update of the configured properties from the modified instance to
	 * the resource instance
	 *
	 * @param event
	 *            that carry the modified instance
	 */
	public void onUserChange(@Observes InstancePersistedEvent<? extends Instance> event) {
		Instance instance = event.getInstance();
		// the save bellow triggers another update of the same user and triggers a recursion
		// the second check is to break this recursion
		if (instance.type() != null && instance.type().is("user") && !(instance instanceof Resource)) {
			Resource resource = resourceService.findResource(instance.getId());
			if (resource != null) {
				boolean changed = copyProperties(instance, resource);
				if (changed) {
					resourceService.save(resource, event.getOperation());
				}
			}
		}
	}

	private boolean copyProperties(Instance instance, Resource resource) {
		return propertiesToSync.get().stream().filter(propertyName -> {
			Serializable newValue = instance.get(propertyName);
			Serializable currentValue = resource.get(propertyName);
			resource.add(propertyName, newValue);
			return !nullSafeEquals(newValue, currentValue);
		}).count() > 0;
	}

}
