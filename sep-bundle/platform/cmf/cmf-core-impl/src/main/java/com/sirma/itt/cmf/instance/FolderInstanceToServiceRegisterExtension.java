package com.sirma.itt.cmf.instance;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.definitions.impl.SectionDefinitionProxy;
import com.sirma.itt.cmf.beans.model.FolderInstance;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.services.FolderService;
import com.sirma.itt.cmf.services.impl.dao.GenericDefinitionAccessor;
import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.DefinitionType;
import com.sirma.itt.emf.definition.model.GenericDefinition;
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
 * Extension implementation for {@link FolderInstance}
 * 
 * @author BBonev
 */
@ApplicationScoped
@SuppressWarnings("unchecked")
@InstanceType(type = ObjectTypesCmf.FOLDER)
@Extension(target = InstanceToServiceRegisterExtension.TARGET_NAME, order = 65)
public class FolderInstanceToServiceRegisterExtension implements InstanceToServiceRegisterExtension {

	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<Class<?>> SUPPORTED_OBJECTS;

	static {
		SUPPORTED_OBJECTS = new ArrayList<Class<?>>();
		SUPPORTED_OBJECTS.add(FolderInstance.class);
		SUPPORTED_OBJECTS.add(SectionDefinitionProxy.class);
	}

	/** The section service. */
	@Inject
	private FolderService folderService;

	/** The instance dao. */
	@Inject
	@InstanceType(type = ObjectTypesCmf.FOLDER)
	private InstanceDao<FolderInstance> instanceDao;

	/** The property model callback. */
	@Inject
	@InstanceType(type = ObjectTypes.DEFAULT)
	private PropertyModelCallback<Instance> propertyModelCallback;

	/** The definition accessor. */
	@Inject
	private javax.enterprise.inject.Instance<GenericDefinitionAccessor> definitionAccessor;

	/** The compiler callback. */
	@Inject
	@DefinitionType(ObjectTypesCmf.GENERIC)
	private DefinitionCompilerCallback<GenericDefinition> compilerCallback;

	/** The instance event provider. */
	@Inject
	@InstanceType(type = ObjectTypesCmf.FOLDER)
	private InstanceEventProvider<FolderInstance> instanceEventProvider;

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
		return (InstanceService<I, D>) folderService;
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
