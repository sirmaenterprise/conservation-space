package com.sirma.itt.cmf.instance;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.impl.TaskDefinitionRefImpl;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.services.WorkflowTaskService;
import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceEventProvider;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.InstanceToServiceRegisterExtension;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.dao.PropertyModelCallback;
import com.sirma.itt.emf.properties.model.PropertyModel;

/**
 * Extension implementation for {@link TaskInstance} and {@link TaskDefinitionRef}.
 *
 * @author BBonev
 */
@ApplicationScoped
@SuppressWarnings("unchecked")
@InstanceType(type = ObjectTypesCmf.WORKFLOW_TASK)
@Extension(target = InstanceToServiceRegisterExtension.TARGET_NAME, order = 50)
public class TaskInstanceToServiceRegisterExtension implements InstanceToServiceRegisterExtension {

	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<Class<?>> SUPPORTED_OBJECTS;

	static {
		SUPPORTED_OBJECTS = new ArrayList<Class<?>>();
		SUPPORTED_OBJECTS.add(TaskInstance.class);
		SUPPORTED_OBJECTS.add(TaskDefinitionRef.class);
		SUPPORTED_OBJECTS.add(TaskDefinitionRefImpl.class);
	}

	/** The instance service. */
	@Inject
	private WorkflowTaskService instanceService;

	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesCmf.WORKFLOW_TASK)
	private InstanceDao<TaskInstance> instanceDao;

	/** The property model callback. */
	@Inject
	@InstanceType(type = ObjectTypes.DEFAULT)
	private PropertyModelCallback<Instance> propertyModelCallback;

	/** The instance event provider. */
	@Inject
	@InstanceType(type = ObjectTypesCmf.WORKFLOW_TASK)
	private InstanceEventProvider<TaskInstance> instanceEventProvider;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <I extends Instance, D extends DefinitionModel> InstanceService<I, D> getInstanceService() {
		return (InstanceService<I, D>) instanceService;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <I extends Instance> InstanceDao<I> getInstanceDao() {
		return (InstanceDao<I>) instanceDao;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <P extends PropertyModel> PropertyModelCallback<P> getModelCallback() {
		return (PropertyModelCallback<P>) propertyModelCallback;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DefinitionAccessor getDefinitionAccessor() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends TopLevelDefinition> DefinitionCompilerCallback<T> getCompilerCallback() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <I extends Instance> InstanceEventProvider<I> getEventProvider() {
		return (InstanceEventProvider<I>) instanceEventProvider;
	}
}
