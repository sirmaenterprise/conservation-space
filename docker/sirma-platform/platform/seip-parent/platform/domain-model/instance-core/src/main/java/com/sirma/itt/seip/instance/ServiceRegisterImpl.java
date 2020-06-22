package com.sirma.itt.seip.instance;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.definition.TypeMappingProvider;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.dao.InstanceToServiceRegistryExtension;
import com.sirma.itt.seip.instance.dao.ServiceRegistry;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.properties.PropertyModelCallback;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.seip.plugin.PluginUtil;

/**
 * Default implementation of the {@link ServiceRegistry}. If extension is not found then <code>null</code> is returned
 * by all methods.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ServiceRegisterImpl implements ServiceRegistry {

	/** The extensions. */
	@Inject
	@ExtensionPoint(InstanceToServiceRegistryExtension.TARGET_NAME)
	private Iterable<InstanceToServiceRegistryExtension> extensions;

	@Inject
	private TypeMappingProvider typeProvider;

	/** The extension mapping. */
	private Map<Class, InstanceToServiceRegistryExtension> extensionMapping;

	/**
	 * Initialize extension mapping.
	 */
	@PostConstruct
	public void initializeExtensionMapping() {
		extensionMapping = PluginUtil.parseSupportedObjects(extensions, true);
	}

	@Override
	public InstanceService getInstanceService(Object object) {
		InstanceToServiceRegistryExtension extension = getExtension(object);
		if (extension == null) {
			return null;
		}
		return extension.getInstanceService();
	}

	@Override
	public InstanceDao getInstanceDao(Object object) {
		InstanceToServiceRegistryExtension extension = getExtension(object);
		if (extension == null) {
			return null;
		}
		return extension.getInstanceDao();
	}

	@Override
	public <P extends PropertyModel> PropertyModelCallback<P> getModelCallback(Object object) {
		InstanceToServiceRegistryExtension extension = getExtension(object);
		if (extension == null) {
			return null;
		}
		return extension.getModelCallback();
	}

	@Override
	public <I extends Instance> InstanceEventProvider<I> getEventProvider(Object object) {
		InstanceToServiceRegistryExtension extension = getExtension(object);
		InstanceEventProvider<I> provider = null;
		if (extension != null) {
			provider = extension.getEventProvider();
		}
		if (provider == null) {
			provider = InstanceEventProvider.NoOpInstanceEventProvider.instance();
		}
		return provider;
	}

	/**
	 * Gets the extension.
	 *
	 * @param object
	 *            the object
	 * @return the extension
	 */
	private InstanceToServiceRegistryExtension getExtension(Object object) {
		if (object == null) {
			return null;
		}
		if (object instanceof Class) {
			return extensionMapping.get(object);
		}
		Class<?> lookFor = object.getClass();
		if (object instanceof GenericDefinition) {
			// for generic definitions we should fetch a class by type of the definition not the
			// class itself
			Class<? extends Instance> instanceClass = typeProvider
					.getInstanceClass(((GenericDefinition) object).getType());
			if (instanceClass != null) {
				lookFor = instanceClass;
			}
		}
		return extensionMapping.get(lookFor);
	}

	@Override
	public Set<Class> getSupportedObjects() {
		return Collections.unmodifiableSet(extensionMapping.keySet());
	}

	@Override
	public Set<InstanceToServiceRegistryExtension> getExtensions() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(extensionMapping.values()));
	}

}
