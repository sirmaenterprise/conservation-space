package com.sirma.itt.emf.instance;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.annotation.Proxy;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.definition.dao.AllowedChildrenTypeProvider;
import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.InstanceServiceProvider;
import com.sirma.itt.emf.instance.dao.InstanceToServiceRegisterExtension;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.OwnedModel;
import com.sirma.itt.emf.plugin.ExtensionPoint;
import com.sirma.itt.emf.plugin.PluginUtil;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Proxy implementation for instance service. It uses the extension
 * {@link InstanceToServiceRegisterExtension} to fetch a concrete implementation for the given
 * definition or instance.
 *
 * @author BBonev
 */
@Proxy
@ApplicationScoped
public class InstanceServiceProxy implements InstanceServiceProvider {

	/** The extensions. */
	@Inject
	@ExtensionPoint(InstanceToServiceRegisterExtension.TARGET_NAME)
	private Iterable<InstanceToServiceRegisterExtension> extensions;

	/** The extension mapping. */
	private Map<Class<?>, InstanceToServiceRegisterExtension> extensionMapping;

	@Inject
	private AllowedChildrenTypeProvider typeProvider;

	/**
	 * Initialize extension mapping.
	 */
	@PostConstruct
	public void initializeExtensionMapping() {
		extensionMapping = PluginUtil.parseSupportedObjects(extensions, false);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <I extends Instance, D extends DefinitionModel> InstanceService<I, D> getService(
			Class<I> instanceClass) {
		InstanceToServiceRegisterExtension extension = extensionMapping.get(instanceClass);
		if (extension == null) {
			return null;
		}
		return (InstanceService<I, D>) extension.getInstanceService();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<DefinitionModel> getInstanceDefinitionClass() {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instance createInstance(DefinitionModel definition, Instance parent) {
		return getService(definition).createInstance(definition, parent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instance createInstance(DefinitionModel definition, Instance parent, Operation operation) {
		return getService(definition).createInstance(definition, parent, operation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	public Instance save(Instance instance, Operation operation) {
		if (operation != null) {
			RuntimeConfiguration.setConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION,
					operation.getOperation());
		}
		try {
			return getService(instance).save(instance, operation);
		} finally {
			if (operation != null) {
				RuntimeConfiguration
						.clearConfiguration(RuntimeConfigurationProperties.CURRENT_OPERATION);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Instance> loadInstances(Instance owner) {
		return getService(owner).loadInstances(owner);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instance loadByDbId(Serializable id) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instance load(Serializable instanceId) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends Serializable> List<Instance> load(List<S> ids) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends Serializable> List<Instance> loadByDbId(List<S> ids) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends Serializable> List<Instance> load(List<S> ids, boolean allProperties) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends Serializable> List<Instance> loadByDbId(List<S> ids, boolean allProperties) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, List<DefinitionModel>> getAllowedChildren(Instance owner) {
		return getService(owner).getAllowedChildren(owner);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh(Instance instance) {
		Set<Instance> history = new LinkedHashSet<Instance>(10);
		refreshInternal(instance, history);
	}

	/**
	 * Refreshes the full structure back to the root.
	 *
	 * @param instance
	 *            the instance
	 * @param history
	 *            the history
	 */
	private void refreshInternal(Instance instance, Set<Instance> history) {
		if (history.contains(instance)) {
			return;
		}
		history.add(instance);
		getService(instance).refresh(instance);
		Instance local = instance;
		while ((local instanceof OwnedModel) && (((OwnedModel) local).getOwningInstance() != null)) {
			refreshInternal(((OwnedModel) local).getOwningInstance(), history);
			local = ((OwnedModel) local).getOwningInstance();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DefinitionModel> getAllowedChildren(Instance owner, String type) {
		return getService(owner).getAllowedChildren(owner, type);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isChildAllowed(Instance owner, String type) {
		return getService(owner).isChildAllowed(owner, type);
	}

	/**
	 * Gets the service by argument class
	 * 
	 * @param object
	 *            the definition
	 * @return the service
	 */
	private InstanceService<Instance, DefinitionModel> getService(Object object) {
		if (object == null) {
			throw new EmfRuntimeException("Cannot fetch service no null object");
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
		InstanceToServiceRegisterExtension extension = extensionMapping.get(lookFor);
		if (extension == null) {
			throw new EmfRuntimeException("The given object with class " + lookFor
					+ " is not defined for service support.");
		}
		return extension.getInstanceService();
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	public Instance cancel(Instance instance) {
		return getService(instance).cancel(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Instance clone(Instance instance, Operation operation) {
		return getService(instance).clone(instance, operation);
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	public void delete(Instance instance, Operation operation, boolean permanent) {
		getService(instance).delete(instance, operation, permanent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	public void attach(Instance targetInstance, Operation operation, Instance... children) {
		getService(targetInstance).attach(targetInstance, operation, children);
	}

	/**
	 * {@inheritDoc}
	 */
	@Secure
	@Override
	public void detach(Instance sourceInstance, Operation operation, Instance... instances) {
		getService(sourceInstance).detach(sourceInstance, operation, instances);
	}

}
