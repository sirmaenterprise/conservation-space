package com.sirma.itt.objects.instance;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.dao.InstanceToServiceRegistryExtension;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.properties.PropertyModelCallback;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Extension implementation for {@link ObjectInstance}
 *
 * @author BBonev
 */
@ApplicationScoped
@SuppressWarnings("unchecked")
@InstanceType(type = ObjectTypes.OBJECT)
@Extension(target = InstanceToServiceRegistryExtension.TARGET_NAME, order = 300)
public class ObjectInstanceToServiceRegistryExtension implements InstanceToServiceRegistryExtension {

	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<Class> SUPPORTED_OBJECTS;

	static {
		SUPPORTED_OBJECTS = new ArrayList<>();
		SUPPORTED_OBJECTS.add(ObjectInstance.class);
		SUPPORTED_OBJECTS.add(EmfInstance.class);
		SUPPORTED_OBJECTS.add(GenericDefinition.class);
	}

	@Inject
	private InstanceService instanceService;
	@Inject
	@InstanceType(type = ObjectTypes.OBJECT)
	private InstanceDao instanceDao;

	/** The instance event provider. */
	@Inject
	@InstanceType(type = ObjectTypes.OBJECT)
	private InstanceEventProvider<ObjectInstance> instanceEventProvider;

	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	@Override
	public InstanceService getInstanceService() {
		return instanceService;
	}

	@Override
	public InstanceDao getInstanceDao() {
		return instanceDao;
	}

	@Override
	public <P extends PropertyModel> PropertyModelCallback<P> getModelCallback() {
		return null;
	}

	@Override
	public <I extends Instance> InstanceEventProvider<I> getEventProvider() {
		return (InstanceEventProvider<I>) instanceEventProvider;
	}

}
