package com.sirma.itt.pm.instance;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.DefinitionType;
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
import com.sirma.itt.pm.domain.ObjectTypesPm;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.definitions.impl.ProjectDefinitionImpl;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.services.ProjectService;
import com.sirma.itt.pm.services.impl.dao.ProjectDefinitionAccessor;

/**
 * Extension implementation for {@link ProjectInstance} and {@link ProjectDefinition}.
 * 
 * @author BBonev
 */
@ApplicationScoped
@SuppressWarnings("unchecked")
@InstanceType(type = ObjectTypesPm.PROJECT)
@Extension(target = InstanceToServiceRegisterExtension.TARGET_NAME, order = 200)
public class ProjectInstanceToServiceRegisterExtension implements InstanceToServiceRegisterExtension {

	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<Class<?>> SUPPORTED_OBJECTS;

	static {
		SUPPORTED_OBJECTS = new ArrayList<Class<?>>();
		SUPPORTED_OBJECTS.add(ProjectInstance.class);
		SUPPORTED_OBJECTS.add(ProjectDefinition.class);
		SUPPORTED_OBJECTS.add(ProjectDefinitionImpl.class);
	}

	/** The instance service. */
	@Inject
	private ProjectService instanceService;

	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesPm.PROJECT)
	private InstanceDao<ProjectInstance> instanceDao;

	/** The property model callback. */
	@Inject
	@InstanceType(type = ObjectTypesPm.PROJECT)
	private PropertyModelCallback<ProjectInstance> propertyModelCallback;

	/** The definition accessor. */
	@Inject
	private javax.enterprise.inject.Instance<ProjectDefinitionAccessor> definitionAccessor;

	/** The compiler callback. */
	@Inject
	@DefinitionType(ObjectTypesPm.PROJECT)
	private DefinitionCompilerCallback<ProjectDefinition> compilerCallback;

	/** The instance event provider. */
	@Inject
	@InstanceType(type = ObjectTypesPm.PROJECT)
	private InstanceEventProvider<ProjectInstance> instanceEventProvider;

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
		return definitionAccessor.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends TopLevelDefinition> DefinitionCompilerCallback<T> getCompilerCallback() {
		return (DefinitionCompilerCallback<T>) compilerCallback;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <I extends Instance> InstanceEventProvider<I> getEventProvider() {
		return (InstanceEventProvider<I>) instanceEventProvider;
	}

}
