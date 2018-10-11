package com.sirma.itt.emf.rest;

import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.CTX_STACK_TRACE;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.CTX_STATUS_MESSAGE;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.EXECUTE_ATOMICALLY;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.EXECUTE_IN_NEW_TRANSACTION;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.MESSAGE;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.OPERATION;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.OPERATIONS;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.RESPONSE;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.RESPONSE_STATE;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.STACK_TRACE;
import static com.sirma.itt.seip.instance.actions.ExecutableOperationProperties.STATUS;
import static com.sirma.itt.seip.instance.actions.OperationStatus.COMPLETED;
import static com.sirma.itt.seip.instance.actions.OperationStatus.FAILED;
import static com.sirma.itt.seip.instance.actions.OperationStatus.NOT_RUN;
import static com.sirma.itt.seip.instance.actions.OperationStatus.PENDING;
import static com.sirma.itt.seip.instance.actions.OperationStatus.ROLLBACKED;
import static com.sirma.itt.seip.instance.actions.OperationStatus.ROLLBACK_FAILED;
import static com.sirma.itt.seip.instance.actions.OperationStatus.RUNNING;
import static com.sirma.itt.seip.instance.actions.OperationStatus.SKIPPED;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.instance.actions.ExecutableOperation;
import com.sirma.itt.seip.instance.actions.ExecutableOperationFactory;
import com.sirma.itt.seip.instance.actions.OperationContext;
import com.sirma.itt.seip.instance.actions.OperationResponse;
import com.sirma.itt.seip.instance.actions.OperationStatus;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Rest service for executing batch operations at any sort.
 * <p>
 * The minimal expected information and format:
 *
 * <pre>
 * <code>{
 *      operations: [
 *      executeAtomic: true,
 *      {
 *         operation: "operationId1"
 *      }, {
 *         operation: "operationId2"
 *      }
 *      ...
 *  ]
 * }</code>
 * </pre>
 *
 * And the response will be in the format:
 *
 * <pre>
 * <code>{
 *      operations: [{
 *         operation: "operationId1"
 *         response: {
 *             key: "value"
 *         },
 *         responseState: {
 *             status: "COMPLETED",
 *             message: "All done"
 *         }
 *      }, {
 *         operation: "operationId2"
 *         response: "something to return",
 *         responseState: {
 *             status: "COMPLETED",
 *             message: "Done"
 *         }
 *      }
 *     ],
 *     responseState: {
 *         status: "COMPLETED"
 *     }
 * }</code>
 * </pre>
 *
 * The implementation should guarantee that the response matches the request in number of the passed operations and to
 * keep the order of the operations. All execution should happen as atomic operations and if any of them failed all
 * changes made from the previous operations in the same call should be reverted if possible. If not possible
 * appropriate message should be returned.
 *
 * @author BBonev
 */
@Transactional
@Path("/executor")
@Produces(MediaType.APPLICATION_JSON)
public class ExecutorRestService extends EmfRestService {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorRestService.class);

	private static final OperationStatus[] OVERALL_STATES_BY_PRIORITY = { ROLLBACK_FAILED, ROLLBACKED, NOT_RUN,
			FAILED, COMPLETED };
	/** The factory. */
	@Inject
	private ExecutableOperationFactory factory;

	@Inject
	private TransactionSupport dbDao;

	/**
	 * Executes the given operation request.
	 *
	 * @param data
	 *            the data to execute
	 * @return the response. The response code will be {@link Status#BAD_REQUEST} if the arguments could not be parsed
	 *         as JSON, does not confront the specified format or there are not operations to execute. In all other
	 *         cases the response will be {@link Status#OK}. To check the actual status check the responseState field in
	 *         the main result or in each operation.
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response execute(String data) {
		LOGGER.trace("Executing operation {}", data);
		// read request and extract the valid operation information
		ExecutionContext operations = parseRequest(data);

		if (operations.isEmpty()) {
			return buildResponse(Status.BAD_REQUEST, "The provided request data does not contains valid operations!");
		}

		// execute operations that left
		executeActions(operations);

		return populateResultsFromExecution(operations);
	}

	/**
	 * Execute actions.
	 *
	 * @param operations
	 *            the operations
	 */
	private void executeActions(final ExecutionContext operations) {

		// prepare the temporary list with states that will be used to track each operation
		// if used in monitoring system the list could show the current state of execution of each
		// operation

		Boolean hasErrors = Boolean.FALSE;

		for (int i = 0; i < operations.size(); i++) {
			final ExecutionOperation operation = operations.get(i);
			if (operations.isAtomic() && hasErrors.booleanValue()) {
				// if error has occurred skip all other operations and mark them as such
				OperationContext context = new OperationContext();
				context.put(CTX_STATUS_MESSAGE, "Skipped due to previous errors");
				operation.setContext(context);
				operation.setStatus(SKIPPED);
				continue;
			}
			final int index = i;
			Callable<Boolean> callable = () -> !processOperation(index, operations, operation);
			if (operation.shouldExecuteInNewTx()) {
				hasErrors = dbDao.invokeInNewTx(callable);
			} else {
				try {
					hasErrors = callable.call();
				} catch (Exception e) {
					throw new EmfApplicationException(e.getMessage(), e);
				}
			}

		}
	}

	/**
	 * Processing each operation.
	 *
	 * @param i
	 *            - the operation number
	 * @param executionContext
	 *            - the state for the each operation
	 * @param toExecute
	 *            the to execute
	 * @return true, if successful
	 */
	private static boolean processOperation(int i, ExecutionContext executionContext, ExecutionOperation toExecute) {

		// invalid operation or not implemented, yet
		if (!toExecute.initializeForExecution()) {
			OperationContext context = new OperationContext();
			context.put(CTX_STATUS_MESSAGE, "Operation " + toExecute.getOperation() + " not found");
			toExecute.setContext(context);

			if (executionContext.isAtomic()) {
				rollbackFrom(executionContext, i);
			}
			return false;
		}

		OperationContext context = null;
		try {
			// try to parse request
			context = toExecute.parseRequest();
			// and now lets try to execute it
			toExecute.execute();
		} catch (Exception e) {
			LOGGER.error("Operation error:" + e.getMessage(), e);
			// if we does not have a context then we failed at parsing so rollback from the
			// previous operation
			int rollbackFrom = i;
			if (context == null) {
				rollbackFrom--;
			}
			toExecute.onExceptionExecution(e);
			if (executionContext.isAtomic()) {
				rollbackFrom(executionContext, rollbackFrom);
			}
			return false;
		}
		return true;
	}

	/**
	 * Populate results from execution.
	 *
	 * @param executionContext
	 *            the state
	 * @return the Pair with the status and json array
	 */
	private Response populateResultsFromExecution(ExecutionContext executionContext) {

		JSONArray result = new JSONArray();
		Set<OperationStatus> entryStatus = CollectionUtils.createHashSet(executionContext.size());

		for (int i = 0; i < executionContext.size(); i++) {
			ExecutionOperation operation = executionContext.get(i);
			result.put(readOperationResult(entryStatus, operation));
		}

		Status responseStatus = Status.INTERNAL_SERVER_ERROR;
		OperationStatus status = determineOverallState(entryStatus);
		if (status == COMPLETED || !executionContext.isAtomic()) {
			responseStatus = Status.OK;
		}

		JSONObject responseObject = new JSONObject();
		JsonUtil.addToJson(responseObject, OPERATIONS, result);
		JSONObject responseObjectStatus = new JSONObject();
		JsonUtil.addToJson(responseObject, RESPONSE_STATE, responseObjectStatus);
		JsonUtil.addToJson(responseObjectStatus, STATUS, status.toString());

		return buildResponse(responseStatus, responseObject.toString());
	}

	/**
	 * Read operation result.
	 *
	 * @param entryStatus
	 *            the entry status
	 * @param operation
	 *            the operation
	 * @return the JSON object
	 */
	private static JSONObject readOperationResult(Set<OperationStatus> entryStatus, ExecutionOperation operation) {
		JSONObject jsonObject = new JSONObject();
		// add action we have processed
		JsonUtil.addToJson(jsonObject, OPERATION, operation.operationId);

		// build response object where is the status and the message
		JSONObject response = new JSONObject();
		JsonUtil.addToJson(jsonObject, RESPONSE_STATE, response);
		OperationStatus status = operation.status;
		JsonUtil.addToJson(response, STATUS, status);

		entryStatus.add(status);

		switch (status) {
			case COMPLETED:
				operation.appendResponseTo(jsonObject);
				break;
			case ROLLBACKED:
				notRun(operation, response);
				break;
			case FAILED:
				operation.appendResponseTo(jsonObject);
				break;
			case ROLLBACK_FAILED:
			case SKIPPED:
			case NOT_RUN:
				notRun(operation, response);
				break;
			default:
				LOGGER.warn("Not handled state {}", status);
				break;
		}
		return jsonObject;
	}

	private static void notRun(ExecutionOperation operation, JSONObject response) {
		OperationContext context = operation.getContext();
		if (context != null) {
			// copy the error from the exception
			JsonUtil.addToJson(response, MESSAGE, context.getIfSameType(CTX_STATUS_MESSAGE, String.class));
			JsonUtil.addToJson(response, STACK_TRACE, context.getIfSameType(CTX_STACK_TRACE, String.class));
		}
	}

	/**
	 * Determine overall state based on the state priority.
	 *
	 * @param entryStatus
	 *            the entry status
	 * @return the scheduler entry status and never <code>null</code>.
	 */
	private static OperationStatus determineOverallState(Set<OperationStatus> entryStatus) {
		for (int i = 0; i < OVERALL_STATES_BY_PRIORITY.length; i++) {
			OperationStatus status = OVERALL_STATES_BY_PRIORITY[i];
			if (entryStatus.contains(status)) {
				return status;
			}
		}
		return FAILED;
	}

	/**
	 * Rollback all elements from the given instance to the beginning of the lists including the element at the given
	 * index.
	 *
	 * @param executionContext
	 *            the current state list
	 * @param index
	 *            the index to begin the rollback from
	 */
	private static void rollbackFrom(ExecutionContext executionContext, int index) {
		if (index < 0) {
			// failed before the first element nothing to rollback
			return;
		}

		for (int j = index; j >= 0; j--) {
			ExecutionOperation toExecute = executionContext.get(j);
			try {
				toExecute.rollback();
			} catch (Exception e) {
				// if the rollback failed then mark it as such and skip the exception
				// we does not want to stop or throw exceptions
				toExecute.onExceptionRollback(e);
				LOGGER.trace("Failed operation rollback", e);
			}
		}
	}

	/**
	 * Adds the stack trace.
	 *
	 * @param context
	 *            the context
	 * @param e
	 *            the e
	 */
	private static void addStackTrace(OperationContext context, Exception e) {
		try (StringWriter writer = new StringWriter(); PrintWriter printWriter = new PrintWriter(writer)) {
			e.printStackTrace(printWriter); // NOSONAR
			if (!context.containsKey(CTX_STACK_TRACE)) {
				context.put(CTX_STACK_TRACE, writer.toString());
			}
		} catch (IOException e1) {
			LOGGER.trace("Could not close writer", e1);
		}
	}

	/**
	 * Sets the state.
	 *
	 * @param state
	 *            the state
	 * @param from
	 *            the from
	 * @param to
	 *            the to
	 * @param status
	 *            the status
	 */
	static void setState(ExecutionContext state, int from, int to, OperationStatus status) {
		if (from < 0) {
			return;
		}
		int localTo = Math.min(to, state.size());
		if (from == localTo) {
			state.get(from).setStatus(status);
			return;
		}
		if (from < localTo) {
			return;
		}
		for (int i = from; i < localTo; i++) {
			state.get(i).setStatus(status);
		}
	}

	/**
	 * Parses and validates the request. Extracts the operation id from each entry and add it to the result.
	 * <p>
	 * The expected format is:
	 *
	 * <pre>
	 * <code>{
	 *      operations: [{
	 *         operation: "operationId1"
	 *      }, {
	 *         operation: "operationId2"
	 *      }
	 *      ...
	 *  ]
	 * }</code>
	 * </pre>
	 *
	 * @param data
	 *            the data to parse
	 * @return the list of operations in order of their appearance in the request.
	 */
	private ExecutionContext parseRequest(String data) {
		ExecutionContext executionContext = new ExecutionContext(factory);
		if (StringUtils.isBlank(data)) {
			return executionContext.initEmptyData();
		}
		try {
			JSONObject object = new JSONObject(data);
			return executionContext.initialize(object);
		} catch (JSONException e) {
			LOGGER.error("Failed to read request due to error {}", e.getMessage(), e);
			return executionContext.initEmptyData();
		}
	}

	/**
	 * Context for a single execution call to the service. Stores all request data and operations that are processed.
	 *
	 * @author BBonev
	 */
	private static class ExecutionContext {

		/** If operations should be treated atomically. */
		boolean atomic = true;
		/** The parsed operations and their data. */
		List<ExecutionOperation> operations;
		ExecutableOperationFactory operationFactory;

		/**
		 * Instantiates a new execution context.
		 *
		 * @param factory
		 *            the factory
		 */
		public ExecutionContext(ExecutableOperationFactory factory) {
			operationFactory = factory;
		}

		/**
		 * Initialize the empty/no operations. The operation processing could not continue.
		 *
		 * @return the execution context
		 */
		public ExecutionContext initEmptyData() {
			operations = Collections.emptyList();
			return this;
		}

		/**
		 * Checks if operations should be atomic.
		 *
		 * @return true, if is atomic
		 */
		public boolean isAtomic() {
			return atomic;
		}

		/**
		 * Gets an operation on the requested index.
		 *
		 * @param index
		 *            the request index
		 * @return the execution operation
		 */
		public ExecutionOperation get(int index) {
			return operations.get(index);
		}

		/**
		 * Returns the number of parsed operations
		 *
		 * @return the operations number
		 */
		public int size() {
			return operations.size();
		}

		/**
		 * Checks if there are any operations read.
		 *
		 * @return true, if is empty
		 */
		public boolean isEmpty() {
			return operations == null || operations.isEmpty();
		}

		/**
		 * Sets the atomic execution mode. Only if non <code>null</code>.
		 *
		 * @param executeAtomic
		 *            the new atomic
		 */
		public void setAtomic(Boolean executeAtomic) {
			atomic = executeAtomic.booleanValue();
		}

		/**
		 * Initialize the context using the given request.
		 *
		 * @param object
		 *            the object
		 * @return the execution context
		 * @throws JSONException
		 *             the JSON exception
		 */
		public ExecutionContext initialize(JSONObject object) throws JSONException {
			JSONArray array = JsonUtil.getJsonArray(object, OPERATIONS);
			if (array == null) {
				return initEmptyData();
			}
			operations = new ArrayList<>(array.length());
			for (int i = 0; i < array.length(); i++) {
				Object value = array.get(i);
				if (value instanceof JSONObject) {
					addOperation((JSONObject) value);
				} else {
					LOGGER.warn("Recieved operation entry that is not valid JSONObject: {}", value);
				}
			}
			// by default the operations are atomic
			setAtomic(JsonUtil.getBooleanValue(object, EXECUTE_ATOMICALLY, Boolean.TRUE));
			return this;
		}

		/**
		 * Adds an operation to the execution context. Called from the {@link #initialize(JSONObject)} method.
		 *
		 * @param jsonObject
		 *            the json object
		 */
		void addOperation(JSONObject jsonObject) {
			String operation = JsonUtil.getStringValue(jsonObject, OPERATION);
			if (StringUtils.isNotBlank(operation)) {
				operations.add(new ExecutionOperation(this, operation, jsonObject));
			} else {
				LOGGER.warn("Recived operation entry with missing or empty operation " + "field: {}", jsonObject);
			}
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ExecutionContext [atomic=");
			builder.append(atomic);
			builder.append(", operations=");
			builder.append(operations);
			builder.append("]");
			return builder.toString();
		}

	}

	/**
	 * The data for a single operation
	 *
	 * @author BBonev
	 */
	private static class ExecutionOperation {

		/** The operation identifier send via request. */
		String operationId;
		/** The operation request data */
		JSONObject data;
		/** The Operation execution context. Contains the parsed data. */
		OperationContext context;
		/** The response to be returned to the caller. */
		Object responseData;
		/** The operation execution status. */
		OperationStatus status = NOT_RUN;
		/** If should be executed in new transaction */
		boolean inNewTx;
		/** The resolved executor. */
		private ExecutableOperation executor;
		/** The immediate operation. */
		private boolean immediate;
		private ExecutionContext executionContext;

		/**
		 * Instantiates a new execution operation.
		 *
		 * @param executionContext
		 *            the execution context
		 * @param operation
		 *            the operation
		 * @param jsonObject
		 *            the json object
		 */
		public ExecutionOperation(ExecutionContext executionContext, String operation, JSONObject jsonObject) {
			this.executionContext = executionContext;
			operationId = operation;
			data = jsonObject;
			inNewTx = Boolean.TRUE.equals(JsonUtil.getBooleanValue(jsonObject, EXECUTE_IN_NEW_TRANSACTION));
			immediate = Boolean.TRUE.equals(JsonUtil.getBooleanValue(jsonObject, "immediate"));
		}

		/**
		 * Initialize for execution by setting the proper state and trying to resolve the executor. If no executor is
		 * resolve then the method returns <code>false</code> and this means that no further executions are possible.
		 *
		 * @return true, if initialized successfully and <code>false</code> if no executor has been resolved.
		 */
		public boolean initializeForExecution() {
			setStatus(PENDING);
			return resolveExecutor() != null;
		}

		/**
		 * Parses the request data using the resolved executor.
		 *
		 * @return the scheduler context that contains the parsed data.
		 */
		public OperationContext parseRequest() {
			setStatus(RUNNING);
			context = resolveExecutor().parseRequest(data);
			return context;
		}

		/**
		 * Calls the execution of the resolved executor using the parsed request data produces by the method
		 * {@link #parseRequest()}. After execution sets the response status and response data returned from the
		 * executor. Method does not handle exception handling
		 *
		 * @return the operation response
		 */
		public OperationResponse execute() {
			OperationResponse response = resolveExecutor().execute(context);
			// update response on success
			OperationStatus executionStatus = COMPLETED;
			if (response != null) {
				// the user could override the default implementation by returning non null
				// response object
				setResponse(response.getResponse());
				executionStatus = response.getStatus();
			}

			setStatus(executionStatus);
			return response;
		}

		/**
		 * Perform rollback on the resolved executor. Does not perform exception handling.
		 */
		public void rollback() {
			ExecutableOperation operation = resolveExecutor();
			if (operation != null) {
				// try to rollback and update state
				if (operation.rollback(context)) {
					setStatus(ROLLBACKED);
				} else {
					// fail internally
					setStatus(ROLLBACK_FAILED);
				}
			} else {
				// if the operation is missing then we probably couldn't run it
				setStatus(NOT_RUN);
			}
		}

		/**
		 * Register exception that occur during operation execution.
		 *
		 * @param e
		 *            the exception to register
		 */
		public void onExceptionExecution(Exception e) {
			setStatus(FAILED);
			if (context == null) {
				context = new OperationContext();
			} else {
				setResponse(data);
			}
			context.put(CTX_STATUS_MESSAGE, e.getMessage());
			addStackTrace(context, e);
		}

		/**
		 * Register exception that occur during rollback.
		 *
		 * @param e
		 *            the exception to register
		 */
		public void onExceptionRollback(Exception e) {
			setStatus(ROLLBACK_FAILED);
			if (executor != null) {
				context.put(CTX_STATUS_MESSAGE,
						"Failed to rollback " + executor.getOperation() + " due to " + e.getMessage());
				addStackTrace(context, e);
				LOGGER.debug("Failed to rollback {} due to {}", executor.getOperation(), e.getMessage(), e);
			}
		}

		/**
		 * Gets the operation identifier
		 *
		 * @return the operation
		 */
		public String getOperation() {
			return operationId;
		}

		/**
		 * Appends the response to the given json object
		 *
		 * @param jsonObject
		 *            the json object
		 */
		public void appendResponseTo(JSONObject jsonObject) {
			if (responseData instanceof JSONObject || responseData instanceof JSONArray) {
				JsonUtil.addToJson(jsonObject, RESPONSE, responseData);
			}
		}

		/**
		 * Gets the current context.
		 *
		 * @return the context
		 */
		public OperationContext getContext() {
			return context;
		}

		/**
		 * Sets the response data
		 *
		 * @param responseData
		 *            the new response
		 */
		public void setResponse(Object responseData) {
			this.responseData = responseData;
		}

		/**
		 * Resolve executor based on the current operation. Try to get an executor from the executor factory. If no
		 * executor is returned, then check if operation is immediate and if this is the case then get an executor for
		 * immediate operations.
		 *
		 * @return the executable operation
		 */
		public ExecutableOperation resolveExecutor() {
			if (executor == null) {
				executor = executionContext.operationFactory.getExecutor(operationId);
				if (executor == null && immediate) {
					executor = executionContext.operationFactory.getImmediateExecutor();
				}
			}
			return executor;
		}

		/**
		 * Should execute the operation in new transaction.
		 *
		 * @return true, if successful
		 */
		public boolean shouldExecuteInNewTx() {
			return inNewTx;
		}

		/**
		 * Updates the status.
		 *
		 * @param entryStatus
		 *            the new status
		 */
		public void setStatus(OperationStatus entryStatus) {
			status = entryStatus;
		}

		/**
		 * Sets the context.
		 *
		 * @param ctx
		 *            the ctx
		 */
		public void setContext(OperationContext ctx) {
			context = ctx;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ExecutionOperation [operationId=");
			builder.append(operationId);
			builder.append(", status=");
			builder.append(status);
			builder.append(", inNewTx=");
			builder.append(inNewTx);
			builder.append(", immediate=");
			builder.append(immediate);
			builder.append("]");
			return builder.toString();
		}

	}
}
