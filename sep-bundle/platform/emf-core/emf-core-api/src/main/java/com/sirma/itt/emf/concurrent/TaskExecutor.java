package com.sirma.itt.emf.concurrent;

import java.util.List;

/**
 * Task manager executor service. The service provides means for running asynchronous tasks on a
 * limited threads per user. <br>
 * <b>NOTE:</b> the thread per user means thread per method call. If the method running task calls
 * the executor again it will use another set of threads for now.
 *
 * @author BBonev
 */
public interface TaskExecutor {

	/**
	 * Executes the given list of tasks in parallel on up to.
	 * {@link com.sirma.itt.emf.configuration.EmfConfigurationProperties#ASYNCHRONOUS_TASK_PER_USER_POOL_SIZE}
	 * threads. The method will block until all tasks are executed. Will throw a runtime exception
	 * {@link com.sirma.itt.emf.exceptions.EmfRuntimeException} if any of the tasks fails. One
	 * failed tasks will not terminate the execution of the other tasks.
	 * 
	 * @param <T>
	 *            The concrete task type
	 * @param tasks
	 *            the list of tasks to execute
	 */
	<T extends GenericAsyncTask> void execute(List<T> tasks);

}