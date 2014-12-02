package com.sirma.itt.pm.schedule.converter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.exceptions.EmfConfigurationException;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.pm.schedule.model.ScheduleAssignment;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.StartInstance;
import com.sirma.itt.pm.schedule.service.ScheduleResourceService;

/**
 * Converter provider that produces an concrete instance objects from a {@link StartInstance}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class StartInstanceToInstanceConverterProvider implements TypeConverterProvider {

	/** The resource service. */
	@Inject
	protected ScheduleResourceService resourceService;

	/** The project resource service. */
	@Inject
	protected ResourceService projectResourceService;

	/** The converter. */
	protected TypeConverter converter;

	/**
	 * Common converter that handles the {@link StartInstance} to {@link Instance} conversion.
	 *
	 * @param <T>
	 *            the specific instance type
	 * @author BBonev
	 */
	public abstract class StartInstanceToInstanceConverter<T extends Instance> implements
			Converter<StartInstance, T> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		@SuppressWarnings("unchecked")
		public T convert(StartInstance source) {
			ScheduleEntry entry = source.getScheduleEntry();
			if (entry.getActualInstanceClass() == null) {
				throw new EmfRuntimeException("No actual instance class!");
			}

			List<ScheduleAssignment> assignments = resourceService.getAssignments(entry);
			if (assignments.isEmpty()) {
				// no assignments so we cannot start an instance here
				throw new EmfConfigurationException("No assignments for schedule entry" + entry);
			}
			if (assignments.size() > 1) {
				// more then one assignment so..
			}
			List<Serializable> ids = new ArrayList<Serializable>(assignments.size());
			for (ScheduleAssignment scheduleAssignment : assignments) {
				ids.add(scheduleAssignment.getResourceId());
			}
			List<Resource> resources = projectResourceService.getResources(ids);

			Instance instance = converter.convert(entry.getActualInstanceClass(), entry);
			updateInstanceAssignments(entry, (T) instance, resources);

			return (T) instance;
		}

		/**
		 * Update instance assignments.
		 *
		 * @param entry
		 *            the entry
		 * @param instance
		 *            the instance
		 * @param resources
		 *            the resources
		 */
		protected abstract void updateInstanceAssignments(ScheduleEntry entry, T instance,
				List<Resource> resources);
	}

	/**
	 * Concrete class that handles a {@link CaseInstance} population
	 *
	 * @author BBonev
	 */
	public class StartInstanceToCaseConverter extends
			StartInstanceToInstanceConverter<CaseInstance> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void updateInstanceAssignments(ScheduleEntry entry, CaseInstance instance,
				List<Resource> resources) {
			// add resources to case priority users
		}
	}

	/**
	 * Concrete converter to perform start instance for {@link StandaloneTaskInstance}.
	 *
	 * @author BBonev
	 */
	public class StartInstanceToTaskConverter extends
			StartInstanceToInstanceConverter<StandaloneTaskInstance> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void updateInstanceAssignments(ScheduleEntry entry,
				StandaloneTaskInstance instance, List<Resource> resources) {
			ArrayList<String> users = new ArrayList<String>(resources.size());
			for (Resource resource : resources) {
				String userId = resource.getIdentifier();
				users.add(userId);
			}
			// if we have more then one assignee we will start a pool task
			if (users.size() == 1) {
				instance.getProperties().put(TaskProperties.TASK_ASSIGNEE, users.get(0));
			} else {
				instance.getProperties().put(TaskProperties.TASK_ASSIGNEES, users);
			}

			Instance actualInstance = entry.getActualInstance();
			if (actualInstance != null) {
				InstanceReference reference = converter.convert(InstanceReference.class,
						actualInstance);
				instance.setOwningReference(reference);
			}
		}
	}

	/**
	 * Concrete converter to perform start instance for {@link WorkflowInstanceContext}.
	 *
	 * @author BBonev
	 */
	public class StartInstanceToWorkflowConverter extends
			StartInstanceToInstanceConverter<WorkflowInstanceContext> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void updateInstanceAssignments(ScheduleEntry entry,
				WorkflowInstanceContext instance, List<Resource> resources) {
			if (resources.size() > 1) {
				// log more then one user
			}
			Resource resource = resources.get(0);

			String userId = resource.getIdentifier();
			instance.getProperties().put(TaskProperties.TASK_ASSIGNEE, userId);
		}
	}

	/**
	 * Concrete converter to perform start instance for {@link TaskInstance}.
	 *
	 * @author BBonev
	 */
	public class StartInstanceToWorkflowTaskConverter extends
			StartInstanceToInstanceConverter<TaskInstance> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void updateInstanceAssignments(ScheduleEntry entry, TaskInstance instance,
				List<Resource> resources) {
			if (resources.size() > 1) {
				// log more then one user
			}
			Resource resource = resources.get(0);

			String userId = resource.getIdentifier();
			instance.getProperties().put(TaskProperties.TASK_ASSIGNEE, userId);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		this.converter = converter;
		converter.addConverter(StartInstance.class, CaseInstance.class,
				new StartInstanceToCaseConverter());
		converter.addConverter(StartInstance.class, StandaloneTaskInstance.class, new StartInstanceToTaskConverter());
		converter.addConverter(StartInstance.class, WorkflowInstanceContext.class, new StartInstanceToWorkflowConverter());
		converter.addConverter(StartInstance.class, TaskInstance.class, new StartInstanceToWorkflowTaskConverter());

	}

}
