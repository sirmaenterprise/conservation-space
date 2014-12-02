package com.sirma.itt.objects.instance;

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
import com.sirma.itt.objects.domain.ObjectTypesObject;
import com.sirma.itt.objects.domain.definitions.ObjectDefinition;
import com.sirma.itt.objects.domain.definitions.impl.ObjectDefinitionImpl;
import com.sirma.itt.objects.domain.model.ObjectInstance;
import com.sirma.itt.objects.services.ObjectService;
import com.sirma.itt.objects.services.impl.dao.ObjectDefinitionAccessor;

/**
 * Extension implementation for {@link ObjectInstance} and {@link ObjectDefinition}
 * 
 * @author BBonev
 */
@ApplicationScoped
@SuppressWarnings("unchecked")
@InstanceType(type = ObjectTypesObject.OBJECT)
@Extension(target = InstanceToServiceRegisterExtension.TARGET_NAME, order = 300)
public class ObjectInstanceToServiceRegisterExtension implements InstanceToServiceRegisterExtension {

	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<Class<?>> SUPPORTED_OBJECTS;

	static {
		SUPPORTED_OBJECTS = new ArrayList<Class<?>>();
		SUPPORTED_OBJECTS.add(ObjectInstance.class);
		SUPPORTED_OBJECTS.add(ObjectDefinition.class);
		SUPPORTED_OBJECTS.add(ObjectDefinitionImpl.class);
	}

	@Inject
	private ObjectService instanceService;
	@Inject
	@InstanceType(type = ObjectTypesObject.OBJECT)
	private InstanceDao<ObjectInstance> instanceDao;

	@Inject
	private javax.enterprise.inject.Instance<ObjectDefinitionAccessor> definitionAccessor;

	@Inject
	@DefinitionType(ObjectTypesObject.OBJECT)
	private DefinitionCompilerCallback<ObjectDefinition> compilerCallback;

	/** The instance event provider. */
	@Inject
	@InstanceType(type = ObjectTypesObject.OBJECT)
	private InstanceEventProvider<ObjectInstance> instanceEventProvider;

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
		return null;
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

	@Override
	public <I extends Instance> InstanceEventProvider<I> getEventProvider() {
		return (InstanceEventProvider<I>) instanceEventProvider;
	}

}
