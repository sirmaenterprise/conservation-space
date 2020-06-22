package com.sirma.itt.seip.instance.dao;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Trackable;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.properties.SemanticNonPersistentPropertiesExtension;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.serialization.SerializationHelper;

/**
 * Copy converter. Clones the instances and cleans the non semantic properties.
 *
 * @author BBonev
 */
@Singleton
public class CopyInstanceConverter implements InstanceConverter, EntityConverter {

	@Inject
	@ExtensionPoint(SemanticNonPersistentPropertiesExtension.TARGET_NAME)
	private Iterable<SemanticNonPersistentPropertiesExtension> nonPersistentPropertiesExtension;

	private Set<String> nonPersistentProperties;
	@Inject
	private SerializationHelper serializationHelper;

	@SuppressWarnings({ "findbugs:IS2_INCONSISTENT_SYNC", "findbugs:DC_DOUBLECHECK" })
	private Set<String> getNonPersistentProperties() {
		if (nonPersistentProperties == null) {
			synchronized (this) {
				// synchronize only the initialization
				if (nonPersistentProperties != null) {
					return nonPersistentProperties;
				}
				Set<String> properties = new HashSet<>();
				for (SemanticNonPersistentPropertiesExtension extension : nonPersistentPropertiesExtension) {
					properties.addAll(extension.getNonPersistentProperties());
				}
				nonPersistentProperties = properties;
			}
		}
		return nonPersistentProperties;
	}

	private Instance clean(Instance instance) {
		Trackable.disableTracking(instance);
		if (instance.getProperties() != null) {
			instance.getProperties().keySet().removeAll(getNonPersistentProperties());
		}
		return instance;
	}

	@Override
	public Entity<Serializable> convertToEntity(Instance instance) {
		return serializationHelper.copy(clean(instance));
	}

	@Override
	public Instance convertToInstance(Entity<? extends Serializable> entity) {
		Trackable.disableTracking(entity);
		return serializationHelper.copy((Instance) entity);
	}
}
