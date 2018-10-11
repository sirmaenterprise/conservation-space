package com.sirma.itt.emf.common;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.instance.dao.InstanceDao;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.dao.InstanceToServiceRegistryExtension;
import com.sirma.itt.seip.instance.dao.InstanceType;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.properties.PropertyModelCallback;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Service extension for common instance.
 *
 * @author BBonev
 */
@ApplicationScoped
@SuppressWarnings("unchecked")
@InstanceType(type = ObjectTypes.INSTANCE)
@Extension(target = InstanceToServiceRegistryExtension.TARGET_NAME, order = 8)
public class CommonInstanceToServiceRegistryExtension implements InstanceToServiceRegistryExtension {

	private static final List<Class> SUPPORTED_OBJECTS;

	static {
		SUPPORTED_OBJECTS = new ArrayList<>();
		SUPPORTED_OBJECTS.add(CommonInstance.class);
	}

	@Inject
	@InstanceType(type = ObjectTypes.INSTANCE)
	private InstanceDao instanceDao;

	/** The instance event provider. */
	@Inject
	@InstanceType(type = ObjectTypes.INSTANCE)
	private InstanceEventProvider<? extends CommonInstance> instanceEventProvider;

	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	@Override
	public InstanceService getInstanceService() {
		return null;
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
