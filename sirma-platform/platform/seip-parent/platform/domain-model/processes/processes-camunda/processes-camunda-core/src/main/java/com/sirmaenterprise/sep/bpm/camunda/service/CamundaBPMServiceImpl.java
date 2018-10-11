package com.sirmaenterprise.sep.bpm.camunda.service;

import static com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService.isProcess;
import static com.sirmaenterprise.sep.bpm.camunda.bpmn.service.CamundaBPMNServiceImpl.loadHistoricProcessInstanceById;
import static com.sirmaenterprise.sep.bpm.camunda.bpmn.service.CamundaBPMNServiceImpl.loadProcessInstanceById;
import static com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService.getActivityId;
import static com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService.getSingleValue;
import static com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService.isActivity;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.service.CamundaBPMNServiceImpl;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;

/**
 * Implementation of the generic BPM service. Contains couple of static methods for lightweight usage. The included
 * logic is related to BPMN, CMMN, DMN processing
 *
 * @author bbanchev
 */
@ApplicationScoped
public class CamundaBPMServiceImpl implements CamundaBPMService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final int TASK_LOAD_RETRY_COUNT = 3;

	private static final long DELAY_BEFORE_NEXT_LOAD_ATTEMPT = 300;

	@Inject
	private ProcessEngine processEngine;

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public Optional<Task> loadTask(Instance activity) {
		return loadTask(processEngine.getTaskService(), activity);
	}

	/**
	 * Gets the Camunda task - {@link Task} that is the representation of activity in the process engine. Used as
	 * standalone method to reduce intercepter chains and proxies
	 *
	 * @param taskService the task service to be used for loading
	 * @param activity the activity part of some existing process.
	 * @return the {@link Task} for the given activity or null if completed/not found. Throws runtime exception on found
	 *         more than 1 instances.
	 */
	public static Optional<Task> loadTask(TaskService taskService, Instance activity) {
		if (activity == null || !isActivity(activity)) {
			return Optional.empty();
		}
		String activityId = getActivityId(activity);
		List<Task> tasks = getWithRetry(loadTaskFunction(taskService), activityId);
		return Optional.ofNullable(getSingleValue(tasks, () -> new CamundaIntegrationRuntimeException(
				"Found " + tasks.size() + " activities for id " + activityId)));
	}

	private static Function<String, List<Task>> loadTaskFunction(TaskService taskService) {
		return id -> taskService.createTaskQuery().taskId(id).list();
	}

	/**
	 * Executes the passed function, if the result collection is empty, the function will be called
	 * {@value #TASK_LOAD_RETRY_COUNT} more times with specified delay ({@value #DELAY_BEFORE_NEXT_LOAD_ATTEMPT} ms)
	 * between each retry.
	 *
	 * @param function to execute
	 * @param arg to pass to the function
	 * @return the result from the function execution as {@link Collection}
	 */
	private static <T, R extends Collection<?>> R getWithRetry(Function<T, R> function, T arg) {
		R result = function.apply(arg);
		int count = 0;
		while (result.isEmpty() && count < TASK_LOAD_RETRY_COUNT) {
			try {
				Thread.sleep(DELAY_BEFORE_NEXT_LOAD_ATTEMPT);
			} catch (InterruptedException e) {
				LOGGER.trace("Sleep was interrupted, but the process will continue..", e);
			}
			result = function.apply(arg);
			count++;
		}
		return result;
	}

	/**
	 * Gets the Camunda historic task - {@link HistoricTaskInstance} that is the representation of activity in the
	 * process engine. Used as standalone method to reduce intercepter chains and proxies
	 *
	 * @param historyService the historic task service to be used for loading
	 * @param activity the activity part of some existing process.
	 * @return the {@link HistoricTaskInstance} for the given activity or null if not found. Throws runtime exception on
	 *         found more than 1 instances.
	 */
	public static Optional<HistoricTaskInstance> loadHistoricTask(HistoryService historyService, Instance activity) {
		if (activity == null || !isActivity(activity)) {
			return Optional.empty();
		}
		String activityId = getActivityId(activity);
		List<HistoricTaskInstance> historicTasks = historyService
				.createHistoricTaskInstanceQuery()
					.taskId(activityId)
					.list();
		return Optional.ofNullable(getSingleValue(historicTasks, () -> new CamundaIntegrationRuntimeException(
				"Found " + historicTasks.size() + " historic activities for id " + activityId)));
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public Optional<ActivityDetails> getActivityDetails(Instance activity) {
		if (activity == null || !isActivity(activity)) {
			// it is not activity - just skip it
			return Optional.empty();
		}
		if (isProcess(activity)) {
			return Optional.ofNullable(buildProcessDetails(activity));
		}
		Optional<Task> task = loadTask(processEngine.getTaskService(), activity);
		if (task.isPresent()) {
			return Optional.of(buildActivityDetails(activity, task.get()));
		}
		Optional<HistoricTaskInstance> historicalTask = loadHistoricTask(processEngine.getHistoryService(), activity);
		if (!historicalTask.isPresent()) {
			throw new CamundaIntegrationRuntimeException(
					"Activity details could not loaded for activity with id: " + activity.getId());
		}
		return Optional.of(buildHistoricalActivityDetails(activity, historicalTask.get()));
	}

	private ActivityDetails buildProcessDetails(Instance activity) {
		Optional<ProcessInstance> process = loadProcessInstanceById(processEngine.getRuntimeService(),
				getActivityId(activity));
		if (process.isPresent()) {
			return new ActivityDetails(process.get(), activity);
		}
		Optional<HistoricProcessInstance> historicProcess = CamundaBPMNServiceImpl
				.loadHistoricProcessInstanceById(processEngine.getHistoryService(), getActivityId(activity));
		if (historicProcess.isPresent()) {
			return new ActivityDetails(historicProcess.get(), activity);
		}
		return null;
	}

	private ActivityDetails buildActivityDetails(Instance activity, Task task) {
		Optional<ProcessInstance> process = Optional.empty();
		// in future add caseInstanceId
		if (task.getProcessInstanceId() != null) {
			process = loadProcessInstanceById(processEngine.getRuntimeService(), task.getProcessInstanceId());
		}
		if (process.isPresent()) {
			return new ActivityDetails(process.get(), activity, task);
		}
		throw new CamundaIntegrationRuntimeException(
				"Found active task " + task.getId() + " for inactive/not found process!");
	}

	private ActivityDetails buildHistoricalActivityDetails(Instance activity, HistoricTaskInstance historicTask) {
		Optional<ProcessInstance> process = loadProcessInstanceById(processEngine.getRuntimeService(),
				historicTask.getProcessInstanceId());
		if (process.isPresent()) {
			return new ActivityDetails(process.get(), activity, historicTask);
		}
		Optional<HistoricProcessInstance> historicProcessInstance = loadHistoricProcessInstanceById(
				processEngine.getHistoryService(), historicTask.getProcessInstanceId());
		if (historicProcessInstance.isPresent()) {
			return new ActivityDetails(historicProcessInstance.get(), activity, historicTask);
		}
		throw new CamundaIntegrationRuntimeException(
				"Historic process not found for historic activity with id: " + historicTask.getProcessInstanceId());
	}

	@Override
	@SecureProcessEngine(notInitializedAccepted = false)
	public Optional<TaskFormData> getTaskFormData(Instance activity) {
		Optional<Task> task = loadTask(processEngine.getTaskService(), activity);
		if (task.isPresent()) {
			return Optional.of(processEngine.getFormService().getTaskFormData(task.get().getId()));
		}
		return Optional.empty();
	}

}
