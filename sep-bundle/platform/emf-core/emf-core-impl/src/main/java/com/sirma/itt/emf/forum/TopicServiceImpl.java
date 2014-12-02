package com.sirma.itt.emf.forum;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.definition.model.GenericDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Acts as a proxy class for the {@link CommentService} where all the logic is implemented for the
 * topics.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class TopicServiceImpl implements InstanceService<TopicInstance, GenericDefinition> {

	/** The service. */
	@Inject
	private javax.enterprise.inject.Instance<CommentService> service;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<GenericDefinition> getInstanceDefinitionClass() {
		return GenericDefinition.class;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TopicInstance createInstance(GenericDefinition definition, Instance parent) {
		if (!service.isUnsatisfied()) {
			return (TopicInstance) service.get().createInstance(definition, parent);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TopicInstance createInstance(GenericDefinition definition, Instance parent,
			Operation operation) {
		if (!service.isUnsatisfied()) {
			return (TopicInstance) service.get().createInstance(definition, parent, operation);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TopicInstance save(TopicInstance instance, Operation operation) {
		if (!service.isUnsatisfied()) {
			return (TopicInstance) service.get().save(instance, operation);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TopicInstance cancel(TopicInstance instance) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void refresh(TopicInstance instance) {
		if (!service.isUnsatisfied()) {
			service.get().refresh(instance);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<TopicInstance> loadInstances(Instance owner) {
		if (!service.isUnsatisfied() && (owner != null)) {
			return service.get().getTopics(owner.toReference(), null, -1, true, null, null);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TopicInstance loadByDbId(Serializable id) {
		if (!service.isUnsatisfied() && (id != null)) {
			return (TopicInstance) service.get().loadByDbId(id);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TopicInstance load(Serializable instanceId) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends Serializable> List<TopicInstance> load(List<S> ids) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends Serializable> List<TopicInstance> loadByDbId(List<S> ids) {
		return loadByDbId(ids, true);
	}

	/**
	 * Cast to topic.
	 * 
	 * @param loadByDbId
	 *            the load by db id
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	private List<TopicInstance> castToTopic(List<?> loadByDbId) {
		return (List<TopicInstance>) loadByDbId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends Serializable> List<TopicInstance> load(List<S> ids, boolean allProperties) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <S extends Serializable> List<TopicInstance> loadByDbId(List<S> ids,
			boolean allProperties) {
		if (!service.isUnsatisfied() && (ids != null)) {
			return castToTopic(service.get().loadByDbId(ids, allProperties));
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, List<DefinitionModel>> getAllowedChildren(TopicInstance owner) {
		return Collections.emptyMap();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<DefinitionModel> getAllowedChildren(TopicInstance owner, String type) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isChildAllowed(TopicInstance owner, String type) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TopicInstance clone(TopicInstance instance, Operation operation) {
		if (!service.isUnsatisfied()) {
			return (TopicInstance) service.get().clone(instance, operation);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(TopicInstance instance, Operation operation, boolean permanent) {
		if (!service.isUnsatisfied()) {
			service.get().delete(instance, operation, permanent);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void attach(TopicInstance targetInstance, Operation operation, Instance... children) {
		if (!service.isUnsatisfied()) {
			service.get().attach(targetInstance, operation, children);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void detach(TopicInstance sourceInstance, Operation operation, Instance... instances) {
		if (!service.isUnsatisfied()) {
			service.get().detach(sourceInstance, operation, instances);
		}
	}

}
