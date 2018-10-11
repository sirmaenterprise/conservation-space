package com.sirma.itt.seip.tasks;

/**
 * Scheduler action definition. Implementation of this interface should be provided when registering automatic actions
 * to be executed from {@link SchedulerService}. The action will be instantiated when is going to be executed by the
 * proper executor.
 * <p>
 * When implementing a scheduler action the implementation should comply to one of the rules:
 * <ul>
 * <li>to be registered in the {@link com.sirma.itt.seip.serialization.kryo.KryoSerializationEngine} via
 * {@link com.sirma.itt.seip.serialization.kryo.KryoInitializer}. It should be noted that the action instance state will
 * not be saved in any way but only the class is going to be registered by the given Kryo identifier and retrieved later
 * by it.
 * <li>to be annotated with {@link javax.inject.Named} annotation
 * <li>If both are specified the {@link javax.inject.Named} annotation will be preferred
 * </ul>
 * <br>
 * NOTE: The action life cycle methods will be called on a single instance (unless the action implementation is
 * annotated with {@link javax.ejb.Stateless})
 *
 * @author BBonev
 */
public interface SchedulerAction {

	/**
	 * Method called before action execution. If an exception is thrown by the method the execution will not be
	 * performed.
	 *
	 * @param context
	 *            the context
	 * @throws Exception
	 *             the exception
	 */
	void beforeExecute(SchedulerContext context) throws Exception;

	/**
	 * Execute method.
	 *
	 * @param context
	 *            the context
	 * @throws Exception
	 *             the exception
	 */
	void execute(SchedulerContext context) throws Exception;

	/**
	 * Method called after completion of the {@link #execute(SchedulerContext)} method. The method is called no mater if
	 * the execution was successful or not. If the method throws an error the execution will not be considered as
	 * failure.
	 *
	 * @param context
	 *            the context
	 * @throws Exception
	 *             the exception
	 */
	void afterExecute(SchedulerContext context) throws Exception;

}
