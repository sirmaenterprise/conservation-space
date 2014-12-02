package com.sirma.itt.pm.schedule.converter;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.beans.definitions.TaskDefinition;
import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.services.StandaloneTaskService;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.cmf.services.WorkflowTaskService;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.instance.PropertiesUtil;
import com.sirma.itt.emf.instance.dao.InstanceDao;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.properties.PropertiesService;
import com.sirma.itt.emf.properties.model.PropertyModel;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.pm.constants.ProjectProperties;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.schedule.domain.ObjectTypesPms;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleEntryProperties;
import com.sirma.itt.pm.schedule.util.DateUtil;
import com.sirma.itt.pm.schedule.util.ScheduleEntryUtil;
import com.sirma.itt.pm.services.ProjectService;

/**
 * Specific converter that transforms the {@link ScheduleEntry} instance to actual instance.
 *
 * @author BBonev
 */
@ApplicationScoped
public class ScheduleEntryToInstanceConverter implements TypeConverterProvider {

	/** The dictionary service. */
	@Inject
	protected DictionaryService dictionaryService;
	/** The case instance service. */
	@Inject
	protected CaseService caseInstanceService;
	/** The workflow service. */
	@Inject
	protected WorkflowService workflowService;
	/** The workflow task service. */
	@Inject
	protected WorkflowTaskService workflowTaskService;
	/** The project service. */
	@Inject
	protected ProjectService projectService;
	/** The task service. */
	@Inject
	protected StandaloneTaskService standaloneTaskService;

	@Inject
	@InstanceType(type = ObjectTypesPms.SCHEDULE_ENTRY)
	protected InstanceDao<ScheduleEntry> scheduleInstanceDao;

	@Inject
	protected CodelistService codelistService;

	@Inject
	private PropertiesService propertiesService;

	private TypeConverter typeConverter;

	/**
	 * Converter that uses the given instance to create/load an actual instance from the given.
	 *
	 * @param <I>
	 *            the generic type {@link ScheduleEntry}
	 * @param <D>
	 *            the generic type
	 * @author BBonev
	 */
	public abstract class InstanceLoadConverter<I extends Instance, D extends DefinitionModel>
			implements Converter<ScheduleEntry, I> {

		/** The instance dao. */
		private InstanceService<I, D> instanceService;

		/**
		 * Instantiates a new instance load converter.
		 *
		 * @param instanceDao
		 *            the instance dao
		 */
		public InstanceLoadConverter(InstanceService<I, D> instanceDao) {
			this.instanceService = instanceDao;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public I convert(ScheduleEntry source) {
			InstanceReference reference = source.getInstanceReference();
			if (reference == null) {
				throw new EmfRuntimeException(
						"Cannot convert the schedule entry to instance: no instance reference found");
			}
			I result = null;
			if (StringUtils.isNullOrEmpty(reference.getIdentifier())) {
				D definition = dictionaryService.getDefinition(
						instanceService.getInstanceDefinitionClass(), source.getIdentifier());
				if (definition == null) {
					// if not definition found we cannot continue
					throw new EmfRuntimeException("No definition found for "
							+ instanceService.getInstanceDefinitionClass() + " with ID="
							+ source.getIdentifier());
				}
				Instance parent = null;
				// get the parent if any and if we have enough data
				if (source.getParentId() != null) {
					ScheduleEntry parentEntry = scheduleInstanceDao.loadInstance(
							source.getParentId(), null, false);

					if ((parentEntry.getInstanceReference() != null)
							&& StringUtils.isNotNullOrEmpty(parentEntry.getInstanceReference()
									.getIdentifier())) {
						// fetch the started parent
						parent = typeConverter.convert(Instance.class,
								parentEntry.getInstanceReference());
						// load properties if missing
						if ((parent != null)
								&& ((parent.getProperties() == null) || parent.getProperties()
										.isEmpty())) {
							propertiesService.loadProperties(parent);
						}
					} else if (StringUtils.isNotNullOrEmpty(parentEntry.getIdentifier())
							&& (parentEntry.getInstanceReference() != null)
							&& (parentEntry.getActualInstanceClass() != null)
							&& StringUtils.isNullOrEmpty(parentEntry.getInstanceReference()
									.getIdentifier())) {
						// we could create the parent if needed
						parent = typeConverter.convert(parentEntry.getActualInstanceClass(),
								parentEntry);
						if (parent.getId() == null) {
							// FIXME
							parent = null;
							throw new EmfRuntimeException("Parent instance not created: "
									+ parentEntry.getInstanceReference());
						}
					}
				}

				// create new instance
				// creates a default instance based on the given definition, copies all properties
				// the entry to the instance and the instance is persisted
				result = instanceService.createInstance(definition, parent);
				result.getProperties().putAll(source.getProperties());
			} else {
				// load already created instance
				result = instanceService.loadByDbId(reference.getIdentifier());
			}

			copySpecificProperties(source, result);
			return result;
		}

		/**
		 * Copy specific properties.
		 *
		 * @param source the source
		 * @param result the result
		 */
		protected abstract void copySpecificProperties(ScheduleEntry source, I result);
	}

	/**
	 * Specific converter for {@link ScheduleEntry} to {@link CaseInstance}.
	 *
	 * @author BBonev
	 */
	public class CaseInstanceLoadConverter extends
			InstanceLoadConverter<CaseInstance, CaseDefinition> {

		/**
		 * Instantiates a new case instance load converter.
		 *
		 * @param instanceDao
		 *            the instance dao
		 */
		public CaseInstanceLoadConverter(InstanceService<CaseInstance, CaseDefinition> instanceDao) {
			super(instanceDao);
		}

		/**
		* {@inheritDoc}
		*/
		@Override
		protected void copySpecificProperties(ScheduleEntry source, CaseInstance result) {
			// not used
		}
	}

	/**
	 * Specific converter for {@link ScheduleEntry} to {@link ProjectInstance}.
	 *
	 * @author BBonev
	 */
	public class ProjectInstanceLoadConverter extends
			InstanceLoadConverter<ProjectInstance, ProjectDefinition> {

		/**
		 * Instantiates a new project instance load converter.
		 *
		 * @param instanceDao
		 *            the instance dao
		 */
		public ProjectInstanceLoadConverter(
				InstanceService<ProjectInstance, ProjectDefinition> instanceDao) {
			super(instanceDao);
		}

		/**
		* {@inheritDoc}
		*/
		@Override
		protected void copySpecificProperties(ScheduleEntry source, ProjectInstance result) {
			// sync project properties
			// CollectionUtils.copyValue(source, ScheduleEntryProperties.DURATION, result,
			// ProjectProperties.ESTIMATED_EFFORT_HOURS);
		}
	}

	/**
	 * Specific converter for {@link ScheduleEntry} to {@link StandaloneTaskInstance}.
	 *
	 * @author BBonev
	 */
	public class StandaloneTaskInstanceLoadConverter extends
			InstanceLoadConverter<StandaloneTaskInstance, TaskDefinition> {

		/**
		 * Instantiates a new standalone task instance load converter.
		 *
		 * @param instanceDao
		 *            the instance dao
		 */
		public StandaloneTaskInstanceLoadConverter(
				InstanceService<StandaloneTaskInstance, TaskDefinition> instanceDao) {
			super(instanceDao);
		}

		/**
		* {@inheritDoc}
		*/
		@Override
		protected void copySpecificProperties(ScheduleEntry source, StandaloneTaskInstance result) {
			if (result == null) {
				return;
			}
			if (result.getProperties() == null) {
				result.setProperties(new LinkedHashMap<String, Serializable>());
			}
			CollectionUtils.copyValue(source, ScheduleEntryProperties.PLANNED_END_DATE, result,
					TaskProperties.PLANNED_END_DATE);
		}
	}

	/**
	 * Specific converter for {@link ScheduleEntry} to {@link WorkflowInstanceContext}.
	 *
	 * @author BBonev
	 */
	public class WorkflowInstanceLoadConverter extends
			InstanceLoadConverter<WorkflowInstanceContext, WorkflowDefinition> {

		/**
		 * Instantiates a new workflow instance load converter.
		 *
		 * @param instanceDao
		 *            the instance dao
		 */
		public WorkflowInstanceLoadConverter(
				InstanceService<WorkflowInstanceContext, WorkflowDefinition> instanceDao) {
			super(instanceDao);
		}

		/**
		* {@inheritDoc}
		*/
		@Override
		protected void copySpecificProperties(ScheduleEntry source, WorkflowInstanceContext result) {
			// not used

		}
	}

	/**
	 * Specific converter for {@link ScheduleEntry} to {@link TaskInstance}.
	 *
	 * @author BBonev
	 */
	public class TaskInstanceLoadConverter extends
			InstanceLoadConverter<TaskInstance, TaskDefinitionRef> {

		/**
		 * Instantiates a new task instance load converter.
		 *
		 * @param instanceDao the instance dao
		 */
		public TaskInstanceLoadConverter(
				InstanceService<TaskInstance, TaskDefinitionRef> instanceDao) {
			super(instanceDao);
		}

		/**
		* {@inheritDoc}
		*/
		@Override
		protected void copySpecificProperties(ScheduleEntry source, TaskInstance result) {
			// not used

		}
	}


	/**
	 * Common {@link Instance} to {@link ScheduleEntry} converter.
	 *
	 * @param <I>
	 *            the concrete type type
	 * @author BBonev
	 */
	public abstract class InstanceToScheduleEntryConverter<I extends Instance> implements
			Converter<I, ScheduleEntry> {

		/** The converter. */
		private TypeConverter converter;

		/**
		 * Instantiates a new instance load converter.
		 *
		 * @param converter
		 *            the converter
		 */
		public InstanceToScheduleEntryConverter(TypeConverter converter) {
			this.converter = converter;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ScheduleEntry convert(I source) {
			ScheduleEntry entry = new ScheduleEntry();
			entry.setActualInstance(source);
			entry.setActualInstanceClass(source.getClass());
			entry.setInstanceReference(converter.convert(InstanceReference.class, source));
			if (source.getProperties() != null) {
				// remove the external properties if any
				filterOutExternalProperties(source);
				entry.setProperties(PropertiesUtil.cloneProperties(source.getProperties()));
			}
			entry.setIdentifier(source.getIdentifier());
			entry.setRevision(source.getRevision());
			// we should copy the id if we are creating an entry from actual instance
			entry.setActualInstanceId(source.getId());

			copySpecificProperties(source, entry);

			return entry;
		}

		/**
		 * Copy specific properties.
		 *
		 * @param source the source
		 * @param entry the entry
		 */
		protected abstract void copySpecificProperties(I source, ScheduleEntry entry);
	}

	/**
	 * Specific converter for {@link CaseInstance} to {@link ScheduleEntry}.
	 *
	 * @author BBonev
	 */
	public class CaseInstanceToScheduleEntryConverter extends
			InstanceToScheduleEntryConverter<CaseInstance> {

		/**
		 * Instantiates a new case instance to schedule entry converter.
		 *
		 * @param converter
		 *            the converter
		 */
		public CaseInstanceToScheduleEntryConverter(TypeConverter converter) {
			super(converter);
		}

		/**
		* {@inheritDoc}
		*/
		@Override
		protected void copySpecificProperties(CaseInstance source, ScheduleEntry entry) {
			entry.setLeaf(false);
			entry.setCssClass("case");
			entry.setContentManagementId(source.getContentManagementId());

			if (source.getProperties() == null) {
				return;
			}
			CollectionUtils.copyValue(source, DefaultProperties.CREATED_ON, entry, ScheduleEntryProperties.PLANNED_START_DATE);
			CollectionUtils.copyValue(source, DefaultProperties.CREATED_ON, entry, ScheduleEntryProperties.BASELINE_START_DATE);

			fillTitle(source, entry);
		}
	}

	/**
	 * Specific converter for {@link ProjectInstance} to {@link ScheduleEntry}.
	 *
	 * @author BBonev
	 */
	public class ProjectInstanceToScheduleEntryConverter extends
			InstanceToScheduleEntryConverter<ProjectInstance> {

		/**
		 * Instantiates a new project instance to schedule entry converter.
		 *
		 * @param converter
		 *            the converter
		 */
		public ProjectInstanceToScheduleEntryConverter(TypeConverter converter) {
			super(converter);
		}

		/**
		* {@inheritDoc}
		*/
		@Override
		protected void copySpecificProperties(ProjectInstance source, ScheduleEntry entry) {
			entry.setLeaf(false);
			entry.setCssClass("project");
			entry.setContentManagementId(source.getContentManagementId());
			CollectionUtils.copyValue(source, ProjectProperties.PLANNED_START_DATE, entry, ScheduleEntryProperties.BASELINE_START_DATE);
			CollectionUtils.copyValue(source, ProjectProperties.PLANNED_END_DATE, entry, ScheduleEntryProperties.BASELINE_END_DATE);
			// calculate duration
			Date startDate = (Date) source.getProperties().get(ProjectProperties.PLANNED_START_DATE);
			Date endDate = (Date) source.getProperties().get(ProjectProperties.PLANNED_END_DATE);
			Double duration = 0.0;
			if ((startDate != null) && (endDate != null)) {
				duration = DateUtil.daysBetween(startDate, endDate, true);
			}
			entry.getProperties().put(ScheduleEntryProperties.DURATION, duration);
			entry.getProperties().put(ScheduleEntryProperties.DURATION_UNIT, "d");

//			CollectionUtils.copyValue(source, ProjectProperties.CREATED_ON, entry, ScheduleEntryProperties.PLANNED_START_DATE);
//			CollectionUtils.copyValue(source, ProjectProperties.CREATED_ON, entry, ScheduleEntryProperties.BASELINE_START_DATE);
		}
	}

	/**
	 * Specific converter for {@link WorkflowInstanceContext} to {@link ScheduleEntry}.
	 *
	 * @author BBonev
	 */
	public class WorkflowInstanceToScheduleEntryConverter extends
			InstanceToScheduleEntryConverter<WorkflowInstanceContext> {

		/**
		 * Instantiates a new workflow instance to schedule entry converter.
		 *
		 * @param converter
		 *            the converter
		 */
		public WorkflowInstanceToScheduleEntryConverter(TypeConverter converter) {
			super(converter);
		}

		/**
		* {@inheritDoc}
		*/
		@Override
		protected void copySpecificProperties(WorkflowInstanceContext source, ScheduleEntry entry) {
			entry.setLeaf(false);
			entry.setCssClass("workflow");
			entry.setContentManagementId(source.getWorkflowInstanceId());

			if (source.getProperties() == null) {
				return;
			}


			ScheduleEntryUtil.copyBaselineMetadata(source.getProperties(), entry);
			ScheduleEntryUtil.updateScheduleEntry(entry, source.getProperties());

			fillTitle(source, entry);
		}
	}

	/**
	 * Specific converter for {@link StandaloneTaskInstance} to {@link ScheduleEntry}.
	 *
	 * @author BBonev
	 */
	public class StandaloneTaskInstanceToScheduleEntryConverter extends
			InstanceToScheduleEntryConverter<StandaloneTaskInstance> {

		/**
		 * Instantiates a new standalone task instance to schedule entry converter.
		 *
		 * @param converter
		 *            the converter
		 */
		public StandaloneTaskInstanceToScheduleEntryConverter(TypeConverter converter) {
			super(converter);
		}

		/**
		* {@inheritDoc}
		*/
		@Override
		protected void copySpecificProperties(StandaloneTaskInstance source, ScheduleEntry entry) {
			entry.setLeaf(true);
			entry.setCssClass("standalonetask");
			entry.setContentManagementId(source.getTaskInstanceId());

			if (source.getProperties() == null) {
				return;
			}
//			String propertyToCopy = null;
//			if (source.getProperties().containsKey(TaskProperties.ACTUAL_END_DATE)) {
//				propertyToCopy = TaskProperties.ACTUAL_END_DATE;
//			} else {
//				propertyToCopy = TaskProperties.PLANNED_END_DATE;
//			}
//			CollectionUtils.copyValue(source, propertyToCopy, entry, ScheduleEntryProperties.BASELINE_END_DATE);
//			CollectionUtils.copyValue(source, propertyToCopy, entry, ScheduleEntryProperties.PLANNED_END_DATE);
//			//set the end date
//			entry.setEndDate((Date) source.getProperties().get(propertyToCopy));
//
//			// CollectionUtils.copyValue(source, TaskProperties.TASK_OWNER, entry, DefaultProperties.CREATED_BY);
//			if (source.getProperties().get(TaskProperties.ACTUAL_START_DATE) == null) {
//				propertyToCopy = TaskProperties.PLANNED_START_DATE;
//			} else {
//				propertyToCopy = TaskProperties.ACTUAL_START_DATE;
//				CollectionUtils.copyValue(source, propertyToCopy, entry, propertyToCopy);
//			}
//			//set the start date
//			entry.setStartDate((Date) source.getProperties().get(propertyToCopy));
//			CollectionUtils.copyValue(source, TaskProperties.PLANNED_START_DATE, entry, ScheduleEntryProperties.PLANNED_START_DATE);
//			CollectionUtils.copyValue(source, propertyToCopy, entry, ScheduleEntryProperties.BASELINE_START_DATE);

			ScheduleEntryUtil.copyBaselineMetadata(source.getProperties(), entry);
			ScheduleEntryUtil.updateScheduleEntry(entry, source.getProperties());
			fillTitle(source, entry);
		}
	}

	/**
	 * Specific converter for {@link TaskInstance} to {@link ScheduleEntry}.
	 *
	 * @author BBonev
	 */
	public class TaskInstanceToScheduleEntryConverter extends
			InstanceToScheduleEntryConverter<TaskInstance> {

		/**
		 * Instantiates a new task instance to schedule entry converter.
		 *
		 * @param converter
		 *            the converter
		 */
		public TaskInstanceToScheduleEntryConverter(TypeConverter converter) {
			super(converter);
		}

		/**
		* {@inheritDoc}
		*/
		@Override
		protected void copySpecificProperties(TaskInstance source, ScheduleEntry entry) {
			entry.setLeaf(true);
			entry.setCssClass("task");
			entry.setContentManagementId(source.getTaskInstanceId());

			if (source.getProperties() == null) {
				return;
			}

//			CollectionUtils.copyValue(source, TaskProperties.TASK_OWNER, entry, DefaultProperties.CREATED_BY);
//			CollectionUtils.copyValue(source, TaskProperties.MODIFIED_ON, entry, ScheduleEntryProperties.PLANNED_START_DATE);
//			CollectionUtils.copyValue(source, TaskProperties.MODIFIED_ON, entry, ScheduleEntryProperties.BASELINE_START_DATE);
//
//			if (source.getProperties().containsKey(TaskProperties.ACTUAL_END_DATE)) {
//				CollectionUtils.copyValue(source, TaskProperties.ACTUAL_END_DATE, entry, ScheduleEntryProperties.PLANNED_END_DATE);
//				CollectionUtils.copyValue(source, TaskProperties.ACTUAL_END_DATE, entry, ScheduleEntryProperties.BASELINE_END_DATE);
//			} else {
//				CollectionUtils.copyValue(source, TaskProperties.PLANNED_END_DATE, entry, ScheduleEntryProperties.BASELINE_END_DATE);
//			}

			ScheduleEntryUtil.copyBaselineMetadata(source.getProperties(), entry);
			ScheduleEntryUtil.updateScheduleEntry(entry, source.getProperties());
			fillTitle(source, entry);
		}

	}

	/**
	 * Fill title.
	 *
	 * @param source
	 *            the source
	 * @param entry
	 *            the entry
	 */
	protected void fillTitle(Instance source, ScheduleEntry entry) {
		if (!source.getProperties().containsKey(DefaultProperties.TITLE)) {
			String type = getCodelistDisplayValue(source, DefaultProperties.TYPE);
			entry.getProperties().put(DefaultProperties.TITLE, type);
		}
	}


	/**
	 * Filter out external properties.
	 *
	 * @param source
	 *            the source
	 */
	protected void filterOutExternalProperties(PropertyModel source) {
		for (Iterator<Map.Entry<String, Serializable>> it = source.getProperties().entrySet()
				.iterator(); it.hasNext();) {
			Entry<String, Serializable> next = it.next();
			if (!DefaultProperties.DEFAULT_HEADERS.contains(next.getKey())
					&& (next.getKey().indexOf('_', 1) > 1)) {
				it.remove();
			}
		}
	}

	/**
	 * Gets the codelist display value.
	 *
	 * @param model
	 *            the model
	 * @param property
	 *            the property
	 * @return the codelist display value
	 */
	protected String getCodelistDisplayValue(PropertyModel model, String property) {
		String type = (String) model.getProperties().get(property);
		if (type == null) {
			return null;
		}
		type = WorkflowHelper.stripEngineId(type);
		PropertyDefinition definition = null;
		definition = dictionaryService.getProperty(property, model.getRevision(), model);
		if ((definition != null) && (definition.getCodelist() != null)) {
			String description = codelistService.getDescription(definition.getCodelist(), type);
			if (StringUtils.isNotNullOrEmpty(description)) {
				return description;
			}
		}
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		this.typeConverter = converter;
		converter.addConverter(ScheduleEntry.class, CaseInstance.class,
				new CaseInstanceLoadConverter(caseInstanceService));
		converter.addConverter(ScheduleEntry.class, WorkflowInstanceContext.class,
				new WorkflowInstanceLoadConverter(workflowService));
		converter.addConverter(ScheduleEntry.class, TaskInstance.class,
				new TaskInstanceLoadConverter(workflowTaskService));
		converter.addConverter(ScheduleEntry.class, ProjectInstance.class,
				new ProjectInstanceLoadConverter(projectService));
		converter.addConverter(ScheduleEntry.class, StandaloneTaskInstance.class,
				new StandaloneTaskInstanceLoadConverter(standaloneTaskService));

		converter.addConverter(CaseInstance.class, ScheduleEntry.class,
				new CaseInstanceToScheduleEntryConverter(converter));
		converter.addConverter(WorkflowInstanceContext.class, ScheduleEntry.class,
				new WorkflowInstanceToScheduleEntryConverter(converter));
		converter.addConverter(TaskInstance.class, ScheduleEntry.class,
				new TaskInstanceToScheduleEntryConverter(converter));
		converter.addConverter(ProjectInstance.class, ScheduleEntry.class,
				new ProjectInstanceToScheduleEntryConverter(converter));
		converter.addConverter(StandaloneTaskInstance.class, ScheduleEntry.class,
				new StandaloneTaskInstanceToScheduleEntryConverter(converter));
	}

}
