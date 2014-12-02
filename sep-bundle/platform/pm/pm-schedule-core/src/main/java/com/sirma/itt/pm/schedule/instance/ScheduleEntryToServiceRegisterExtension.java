package com.sirma.itt.pm.schedule.instance;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.instance.dao.DefaultEventsInstanceEventProvider;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceEventProvider;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.InstanceToServiceRegisterExtension;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.properties.dao.PropertyModelCallback;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.pm.schedule.domain.ObjectTypesPms;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;

/**
 * Service registration for {@link ScheduleEntry}
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = InstanceToServiceRegisterExtension.TARGET_NAME, order = 251)
public class ScheduleEntryToServiceRegisterExtension implements
		InstanceToServiceRegisterExtension {

	/** The set of supported objects that are returned by the method {@link #getSupportedObjects()}. */
	private static final List<Class<?>> SUPPORTED_OBJECTS;

	static {
		SUPPORTED_OBJECTS = new ArrayList<Class<?>>();
		SUPPORTED_OBJECTS.add(ScheduleEntry.class);
	}

	@Inject
	@InstanceType(type = ObjectTypesPms.SCHEDULE_ENTRY)
	private InstanceDao<ScheduleEntry> instanceDao;

	@Inject
	@InstanceType(type = ObjectTypesPms.SCHEDULE_ENTRY)
	private PropertyModelCallback<ScheduleEntry> propertyModelCallback;

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
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <I extends Instance> InstanceDao<I> getInstanceDao() {
		return (InstanceDao<I>) instanceDao;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
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

	@Override
	public <I extends Instance> InstanceEventProvider<I> getEventProvider() {
		return new DefaultEventsInstanceEventProvider<I>();
	}
}
