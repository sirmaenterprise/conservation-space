package com.sirma.itt.seip.resources;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.sirma.itt.seip.annotation.NoOperation;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.InstanceExistResult;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.dao.InstanceToServiceRegistryExtension;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.properties.PropertyModelCallback;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Service register for {@link Resource} sub classes.
 *
 * @author BBonev
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@InstanceType(type = ResourceInstanceTypes.RESOURCE)
@Extension(target = InstanceToServiceRegistryExtension.TARGET_NAME, order = 5)
public class ResourceInstanceToServiceRegistryExtension implements InstanceToServiceRegistryExtension {

	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<Class> SUPPORTED_OBJECTS;

	static {
		SUPPORTED_OBJECTS = new ArrayList<>();
		SUPPORTED_OBJECTS.add(Resource.class);
		SUPPORTED_OBJECTS.add(EmfGroup.class);
		SUPPORTED_OBJECTS.add(EmfUser.class);
		SUPPORTED_OBJECTS.add(EmfResource.class);
	}

	@Inject
	private ResourceService resourceService;

	@Inject
	@InstanceType(type = ResourceInstanceTypes.DEFAULT)
	private PropertyModelCallback<PropertyModel> propertyModelCallback;

	@Inject
	@InstanceType(type = ResourceInstanceTypes.RESOURCE)
	private InstanceEventProvider<Resource> instanceEventProvider;

	private InstanceService proxy;

	@PostConstruct
	void init() {
		proxy = new ResourceServiceInstanceServiceProxy(resourceService);
	}

	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	@Override
	public InstanceService getInstanceService() {
		return proxy;
	}

	@Override
	public InstanceDao getInstanceDao() {
		return null;
	}

	@Override
	public <P extends PropertyModel> PropertyModelCallback<P> getModelCallback() {
		return (PropertyModelCallback<P>) propertyModelCallback;
	}

	@Override
	public <I extends Instance> InstanceEventProvider<I> getEventProvider() {
		return (InstanceEventProvider<I>) instanceEventProvider;
	}

	/**
	 * {@link InstanceService} proxy for {@link ResourceService}. The not supported methods will throw
	 * {@link UnsupportedOperationException}
	 *
	 * @author BBonev
	 */
	@NoOperation
	private static class ResourceServiceInstanceServiceProxy implements InstanceService {

		private final ResourceService delegate;

		ResourceServiceInstanceServiceProxy(ResourceService resourceService) {
			delegate = resourceService;
		}

		@Override
		public Instance createInstance(DefinitionModel definition, Instance parent) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Instance createInstance(DefinitionModel definition, Instance parent, Operation operation) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Instance save(Instance instance, Operation operation) {
			if (instance instanceof Resource) {
				delegate.updateResource(instance, operation);
			}
			throw new UnsupportedOperationException();
		}

		@Override
		public Instance cancel(Instance instance) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void refresh(Instance instance) {
			// nothing to do
		}

		@Override
		public List<Instance> loadInstances(Instance owner) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Instance loadByDbId(Serializable id) {
			return delegate.loadByDbId(id);
		}

		@Override
		public Instance load(Serializable instanceId) {
			return delegate.load(instanceId);
		}

		@Override
		public <S extends Serializable> List<Instance> load(List<S> ids) {
			return delegate.load(ids);
		}

		@Override
		public <S extends Serializable> List<Instance> loadByDbId(List<S> ids) {
			return delegate.loadByDbId(ids);
		}

		@Override
		public <S extends Serializable> List<Instance> load(List<S> ids, boolean allProperties) {
			return delegate.load(ids, allProperties);
		}

		@Override
		public <S extends Serializable> List<Instance> loadByDbId(List<S> ids, boolean allProperties) {
			return delegate.loadByDbId(ids, allProperties);
		}

		@Override
		public boolean isChildAllowed(Instance owner, String type, String definitionId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Map<String, List<DefinitionModel>> getAllowedChildren(Instance owner) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<DefinitionModel> getAllowedChildren(Instance owner, String type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isChildAllowed(Instance owner, String type) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Instance clone(Instance instance, Operation operation) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Instance deepClone(Instance instanceToClone, Operation operation) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Collection<String> delete(Instance instance, Operation operation, boolean permanent) {
			delegate.delete(instance, operation, permanent);
			return Collections.singleton(instance.getId().toString());
		}

		@Override
		public void attach(Instance targetInstance, Operation operation, Instance... children) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void detach(Instance sourceInstance, Operation operation, Instance... instances) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Instance publish(Instance instance, Operation operation) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Optional<Instance> loadDeleted(Serializable id) {
			return Optional.ofNullable(loadByDbId(id));
		}

		@Override
		public <S extends Serializable> InstanceExistResult<S> exist(Collection<S> identifiers,
				boolean includeDeleted) {
			return new InstanceExistResult(
					identifiers.stream().collect(Collectors.toMap(Function.identity(), delegate::resourceExists)));
		}
	}
}
