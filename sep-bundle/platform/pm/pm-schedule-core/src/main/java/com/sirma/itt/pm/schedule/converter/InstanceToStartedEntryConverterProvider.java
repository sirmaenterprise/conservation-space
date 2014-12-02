package com.sirma.itt.pm.schedule.converter;

import javax.inject.Inject;

import com.sirma.itt.cmf.beans.definitions.TaskDefinitionRef;
import com.sirma.itt.cmf.beans.definitions.WorkflowDefinition;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.services.StandaloneTaskService;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.cmf.services.WorkflowTaskService;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.emf.configuration.RuntimeConfiguration;
import com.sirma.itt.emf.configuration.RuntimeConfigurationProperties;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.emf.util.ReflectionUtils;
import com.sirma.itt.pm.schedule.model.StartInstance;
import com.sirma.itt.pm.schedule.model.StartedCaseInstance;
import com.sirma.itt.pm.schedule.model.StartedInstance;
import com.sirma.itt.pm.schedule.model.StartedStandaloneTaskInstance;
import com.sirma.itt.pm.schedule.model.StartedTaskInstance;
import com.sirma.itt.pm.schedule.model.StartedWorkflowInstance;

/**
 * Converter provider that converts {@link StartInstance} to {@link StartedInstance}. This is the
 * place where concrete 'start/commit' task algorithms are implemented.
 *
 * @author BBonev
 */
public class InstanceToStartedEntryConverterProvider implements TypeConverterProvider {

	/** The standalone task service. */
	@Inject
	StandaloneTaskService standaloneTaskService;

	/** The case instance service. */
	@Inject
	CaseService caseInstanceService;

	/** The workflow service. */
	@Inject
	WorkflowService workflowService;

	@Inject
	StateService stateService;

	@Inject
	private WorkflowTaskService workflowTaskService;

	@Inject
	private DictionaryService dictionaryService;

	/**
	 * Converter that instantiate a concrete {@link StartedInstance} based on the constructor
	 * argument
	 *
	 * @param <I>
	 *            the concrete instance type
	 */
	public class StartedInstanceConverter<I extends Instance> implements
			Converter<I, StartedInstance> {

		/** The instance. */
		private final Class<? extends StartedInstance> instanceClass;

		/**
		 * Instantiates a new started instance converter.
		 *
		 * @param instance
		 *            the instance
		 */
		public StartedInstanceConverter(Class<? extends StartedInstance> instance) {
			this.instanceClass = instance;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public StartedInstance convert(I source) {
			return ReflectionUtils.newInstance(instanceClass);
		}

	}

	/**
	 * Converter that performs a starting of a {@link StandaloneTaskInstance}.
	 *
	 * @author BBonev
	 */
	public class StandaloneTaskStartConverter implements
			Converter<StandaloneTaskInstance, StartedStandaloneTaskInstance> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public StartedStandaloneTaskInstance convert(StandaloneTaskInstance source) {
			setInitialState(source);

			standaloneTaskService.start(source, new Operation(ActionTypeConstants.APPROVE));
			return new StartedStandaloneTaskInstance(source);
		}
	}

	/**
	 * Sets the initial state.
	 *
	 * @param source
	 *            the new initial state
	 */
	private void setInitialState(Instance source) {
		String currentState = stateService.getState(PrimaryStates.SUBMITTED, source.getClass());
		source.getProperties().put(DefaultProperties.STATUS, currentState);
	}

	/**
	 * Converter that performs starting of a {@link WorkflowInstanceContext}.
	 *
	 * @author BBonev
	 */
	public class WorkflowStartConverter implements
			Converter<WorkflowInstanceContext, StartedWorkflowInstance> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public StartedWorkflowInstance convert(WorkflowInstanceContext source) {
			DefinitionModel model = dictionaryService.getInstanceDefinition(source);
			TaskDefinitionRef startTaskDefinition = null;
			if (model instanceof WorkflowDefinition) {
				startTaskDefinition = WorkflowHelper.getStartTask((WorkflowDefinition) model);
			}
			if (startTaskDefinition == null) {
				return new StartedWorkflowInstance();
			}
			TaskInstance startTaskInstance = workflowTaskService.createInstance(
					startTaskDefinition, source, new Operation(ActionTypeConstants.START_WORKFLOW));
			workflowService.startWorkflow(source, startTaskInstance);
			return new StartedWorkflowInstance(source);
		}
	}

	/**
	 * Converter that performs starting of a {@link TaskInstance}.
	 *
	 * @author BBonev
	 */
	public class TaskStartConverter implements Converter<TaskInstance, StartedTaskInstance> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public StartedTaskInstance convert(TaskInstance source) {
			// TODO Auto-generated method stub
			return new StartedTaskInstance(source);
		}
	}

	/**
	 * Converter that performs starting of a {@link CaseInstance}.
	 *
	 * @author BBonev
	 */
	public class CaseStartConverter implements Converter<CaseInstance, StartedCaseInstance> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public StartedCaseInstance convert(CaseInstance source) {
			setInitialState(source);
			try {
				RuntimeConfiguration.setConfiguration(
						RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN, Boolean.TRUE);
				CaseInstance instance = caseInstanceService.save(source, new Operation(
						ActionTypeConstants.APPROVE));
				return new StartedCaseInstance(instance);
			} finally {
				RuntimeConfiguration
						.clearConfiguration(RuntimeConfigurationProperties.DO_NOT_SAVE_CHILDREN);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(CaseInstance.class, StartedCaseInstance.class,
				new CaseStartConverter());
		converter.addConverter(TaskInstance.class, StartedTaskInstance.class,
				new TaskStartConverter());
		converter.addConverter(WorkflowInstanceContext.class, StartedWorkflowInstance.class,
				new WorkflowStartConverter());
		converter.addConverter(StandaloneTaskInstance.class, StartedStandaloneTaskInstance.class,
				new StandaloneTaskStartConverter());

		converter.addConverter(CaseInstance.class, StartedInstance.class,
				new StartedInstanceConverter<CaseInstance>(StartedCaseInstance.class));
		converter.addConverter(TaskInstance.class, StartedInstance.class,
				new StartedInstanceConverter<TaskInstance>(StartedTaskInstance.class));
		converter
				.addConverter(WorkflowInstanceContext.class, StartedInstance.class,
						new StartedInstanceConverter<WorkflowInstanceContext>(
								StartedWorkflowInstance.class));
		converter.addConverter(StandaloneTaskInstance.class, StartedInstance.class,
				new StartedInstanceConverter<StandaloneTaskInstance>(
						StartedStandaloneTaskInstance.class));

		converter.addDynamicTwoStageConverter(StartInstance.class, CaseInstance.class,
				StartedCaseInstance.class);
		converter.addDynamicTwoStageConverter(StartInstance.class, TaskInstance.class,
				StartedTaskInstance.class);
		converter.addDynamicTwoStageConverter(StartInstance.class, WorkflowInstanceContext.class,
				StartedWorkflowInstance.class);
		converter.addDynamicTwoStageConverter(StartInstance.class, StandaloneTaskInstance.class,
				StartedStandaloneTaskInstance.class);
	}
}
