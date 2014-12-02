package com.sirma.itt.emf.executors;

import java.io.Serializable;
import java.util.Map;

import org.json.JSONObject;

import com.sirma.itt.emf.plugin.Plugin;
import com.sirma.itt.emf.scheduler.SchedulerContext;

/**
 * Base interface for each operation. Defines methods for parsing the JSON format to
 * {@link SchedulerContext}, executing the operation, and some additional methods used for
 * asynchronous execution(not supported for now).
 * <p>
 * <b>NOTE: </b>Implementations of {@link ExecutableOperation} must not store any internal state. If
 * information need to be passed from one method to other the provided {@link SchedulerContext} must
 * be used.
 * 
 * @author BBonev
 */
public interface ExecutableOperation extends Plugin {

	/** The target name. */
	String TARGET_NAME = "ExecutableOperation";

	/**
	 * Gets the operation.
	 * 
	 * @return the operation
	 */
	String getOperation();

	/**
	 * Parses the request to working data and stores them in the given context. The extracted data
	 * will be passed to all other methods
	 * <p>
	 * <b>NOTE</b> the provided context could will be serialized so the stored data should allow
	 * restoration.
	 * 
	 * @param data
	 *            the data
	 * @return the scheduler context
	 */
	SchedulerContext parseRequest(JSONObject data);

	/**
	 * Executes the operation logic. Anything returned will be added to the response.
	 * 
	 * @param data
	 *            the data
	 * @return the result of the operation. Preferable is JSON format.
	 */
	OperationResponse execute(SchedulerContext data);

	/**
	 * Called if the execute method fails and a rollback is required. The implementation must try to
	 * restore the state before executing the operation if possible. If not possible to restore to
	 * previous state an exception could be thrown in order to notify the invoker that a rollback
	 * was unsuccessful or to return false
	 * 
	 * @param data
	 *            the data
	 * @return true, if successful
	 */
	boolean rollback(SchedulerContext data);

	/**
	 * Tests if the current operation supports an asynchronous execution
	 * 
	 * @param data
	 *            the data
	 * @return <code>true</code>, if supported and <code>false</code> that requires a sequential
	 *         execution.
	 */
	boolean couldBeAsynchronous(SchedulerContext data);

	/**
	 * returns a mapping of instances on which the current operation depends different from the base
	 * target instance. Such instances are for example the parent instance (if any) when creating an
	 * instance. The value of the mapping the purpose of the used instance. The uses are defined in
	 * the {@link com.sirma.itt.emf.executors.Operation}
	 * 
	 * @param data
	 *            the data
	 * @return the dependencies
	 * @see Operation
	 */
	Map<Serializable, Operation> getDependencies(SchedulerContext data);

}
