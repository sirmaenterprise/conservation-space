package com.sirma.itt.emf.rest;

import static com.sirma.itt.emf.executors.ExecutableOperationProperties.CTX_STACK_TRACE;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.CTX_STATUS_MESSAGE;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.MESSAGE;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.OPERATION;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.OPERATIONS;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.RESPONSE;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.RESPONSE_STATE;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.STACK_TRACE;
import static com.sirma.itt.emf.executors.ExecutableOperationProperties.STATUS;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.Triplet;
import com.sirma.itt.emf.executors.ExecutableOperation;
import com.sirma.itt.emf.executors.ExecutableOperationFactory;
import com.sirma.itt.emf.executors.OperationResponse;
import com.sirma.itt.emf.scheduler.SchedulerContext;
import com.sirma.itt.emf.scheduler.SchedulerEntryStatus;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Rest service for executing batch operations at any sort.
 * <p>
 * The minimal expected information and format:
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
 * The implementation should guarantee that the response matches the request in number of the passed
 * operations and to keep the order of the operations. All execution should happen as atomic
 * operations and if any of them failed all changes made from the previous operations in the same
 * call should be reverted if possible. If not possible appropriate message should be returned.
 *
 * @author BBonev
 */
@Stateless
@Path("/executor")
@Produces(MediaType.APPLICATION_JSON)
public class ExecutorRestService extends EmfRestService {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorRestService.class);

	/** The factory. */
	@Inject
	private ExecutableOperationFactory factory;

	/**
	 * Executes the given operation request.
	 *
	 * @param data
	 *            the data to execute
	 * @return the response. The response code will be {@link Status#BAD_REQUEST} if the arguments
	 *         could not be parsed as JSON, does not confront the specified format or there are not
	 *         operations to execute. In all other cases the response will be {@link Status#OK}. To
	 *         check the actual status check the responseState field in the main result or in each
	 *         operation.
	 */
	@POST
	@Path("/")
	@Secure
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public Response execute(String data) {
		// read request and extract the valid operation information
		List<Pair<String, JSONObject>> operations = parseRequest(data);

		if (operations.isEmpty()) {
			return buildResponse(Status.BAD_REQUEST,
					"The provided request data does not contains valid operations!");
		}

		// execute operations that left
		Pair<Status,JSONObject> object = executeActions(operations);

		return buildResponse(object.getFirst(), object.getSecond().toString());
	}

	/**
	 * Execute actions.
	 *
	 * @param operations
	 *            the operations
	 * @return the Pair with the status and json array
	 */
	private Pair<Status,JSONObject> executeActions(List<Pair<String, JSONObject>> operations) {

		// prepare the temporary list with states that will be used to track each operation
		// if used in monitoring system the list could show the current state of execution of each
		// operation
		// TODO: change this to a custom object that combines the information from the 2 lists:
		// operations and state
		List<Triplet<SchedulerEntryStatus, SchedulerContext, Object>> state = initState(operations
				.size());

		boolean hasErrors = false;

		for (int i = 0; i < operations.size(); i++) {
			if (hasErrors) {
				// if error has occurred skip all other operations and mark them as such
				SchedulerContext context = new SchedulerContext();
				context.put(CTX_STATUS_MESSAGE, "Skipped due to previous errors");
				state.get(i).setSecond(context);
				setState(state, i, i, SchedulerEntryStatus.SKIPPED);
				continue;
			}
			Pair<String, JSONObject> pair = operations.get(i);
			setState(state, i, i, SchedulerEntryStatus.PENDING);

			ExecutableOperation operation = factory.getExecutor(pair.getFirst());

			// invalid operation or not implemented, yet
			if (operation == null) {
				SchedulerContext context = new SchedulerContext();
				context.put(CTX_STATUS_MESSAGE, "Operation " + pair.getFirst() + " not found");
				state.get(i).setSecond(context);

				rollbackFrom(state, operations, i);
				hasErrors = true;
				continue;
			}

			SchedulerContext context = null;
			try {
				// try to parse request
				context = operation.parseRequest(pair.getSecond());
				setState(state, i, i, SchedulerEntryStatus.RUNNING);
				state.get(i).setSecond(context);

				// and now lets try to execute it
				OperationResponse response = operation.execute(context);
				// update response on success
				Object responseData = null;
				SchedulerEntryStatus status = SchedulerEntryStatus.COMPLETED;
				if (response != null) {
					// the user could override the default implementation by returning non null
					// response object
					responseData = response.getResponse();
					status = response.getStatus();
				}
				state.get(i).setThird(responseData);
				setState(state, i, i, status);
			} catch (Exception e) {
				LOGGER.error("Operation error:" + e.getMessage(), e);
				// on error update status and rollback if possible
				setState(state, i, i, SchedulerEntryStatus.FAILED);
				hasErrors = true;
				// if we does not have a context then we failed at parsing so rollback from the
				// previous operation
				int rollbackFrom = i;
				if (context == null) {
					context = new SchedulerContext();
					context.put(
							CTX_STATUS_MESSAGE,
							"Errors while executing " + operation.getOperation() + " with "
									+ e.getMessage());
					addStackTrace(context, e);
					state.get(i).setSecond(context);
					rollbackFrom--;
				}
				rollbackFrom(state, operations, rollbackFrom);
			}
		}

		// build response after the execution
		Pair<Status,JSONObject> result = populateResultsFromExecution(state, operations);

		return result;
	}

	/**
	 * Populate results from execution.
	 *
	 * @param state
	 *            the state
	 * @param operations
	 *            the operations
	 * @return the Pair with the status and json array
	 */
	private Pair<Status, JSONObject> populateResultsFromExecution(
			List<Triplet<SchedulerEntryStatus, SchedulerContext, Object>> state,
			List<Pair<String, JSONObject>> operations) {

		JSONArray result = new JSONArray();
		Set<SchedulerEntryStatus> entryStatus = CollectionUtils.createHashSet(state.size());

		for (int i = 0; i < state.size(); i++) {
			Triplet<SchedulerEntryStatus, SchedulerContext, Object> triplet = state.get(i);
			JSONObject jsonObject = new JSONObject();
			// add action we have processed
			JsonUtil.addToJson(jsonObject, OPERATION, operations.get(i).getFirst());

			// build response object where is the status and the message
			JSONObject response = new JSONObject();
			JsonUtil.addToJson(jsonObject, RESPONSE_STATE, response);
			JsonUtil.addToJson(response, STATUS, triplet.getFirst().toString());

			entryStatus.add(triplet.getFirst());

			switch (triplet.getFirst()) {
				case COMPLETED:
					appendResponse(jsonObject, triplet.getThird());
					break;
				case ROLLBACKED:
				case FAILED:
					appendResponse(jsonObject, triplet.getThird());
					break;
				case ROLLBACK_FAILED:
				case SKIPPED:
				case NOT_RUN:
					if (triplet.getSecond() != null) {
						// copy the error from the exception
						JsonUtil.addToJson(response, MESSAGE, triplet.getSecond().getIfSameType(CTX_STATUS_MESSAGE, String.class));
						JsonUtil.addToJson(response, STACK_TRACE, triplet.getSecond().getIfSameType(CTX_STACK_TRACE, String.class));
					}
					break;
				default:
					LOGGER.warn("Not handled state " + triplet.getFirst());
					break;
			}
			result.put(jsonObject);
		}

		SchedulerEntryStatus status = determineOverallState(entryStatus);
		Status responseStatus;
		if(status == SchedulerEntryStatus.COMPLETED){
			responseStatus = Status.OK;
		} else {
			responseStatus = Status.INTERNAL_SERVER_ERROR;
		}
		JSONObject responseObject = new JSONObject();
		JsonUtil.addToJson(responseObject, OPERATIONS, result);
		JSONObject responseObjectStatus = new JSONObject();
		JsonUtil.addToJson(responseObject, RESPONSE_STATE, responseObjectStatus);
		JsonUtil.addToJson(responseObjectStatus, STATUS, status.toString());

		return new Pair<Response.Status, JSONObject>(responseStatus, responseObject);
	}

	/**
	 * Determine overall state based on the state priority.
	 *
	 * @param entryStatus
	 *            the entry status
	 * @return the scheduler entry status and never <code>null</code>.
	 */
	private SchedulerEntryStatus determineOverallState(Set<SchedulerEntryStatus> entryStatus) {
		if (entryStatus.contains(SchedulerEntryStatus.ROLLBACK_FAILED)) {
			return SchedulerEntryStatus.ROLLBACK_FAILED;
		} else if (entryStatus.contains(SchedulerEntryStatus.ROLLBACKED)) {
			return SchedulerEntryStatus.ROLLBACKED;
		} else if (entryStatus.contains(SchedulerEntryStatus.NOT_RUN)) {
			return SchedulerEntryStatus.NOT_RUN;
		} else if (entryStatus.contains(SchedulerEntryStatus.COMPLETED)) {
			return SchedulerEntryStatus.COMPLETED;
		}
		return SchedulerEntryStatus.FAILED;
	}

	/**
	 * Append response.
	 *
	 * @param jsonObject
	 *            the json object
	 * @param data
	 *            the data
	 */
	private void appendResponse(JSONObject jsonObject, Object data) {
		if ((data instanceof JSONObject) || (data instanceof JSONArray)) {
			JsonUtil.addToJson(jsonObject, RESPONSE, data);
		}
	}

	/**
	 * Rollback all elements from the given instance to the beginning of the lists including the
	 * element at the given index.
	 *
	 * @param state
	 *            the current state list
	 * @param operations
	 *            the list of operations that are being executed
	 * @param index
	 *            the index to begin the rollback from
	 */
	private void rollbackFrom(List<Triplet<SchedulerEntryStatus, SchedulerContext, Object>> state,
			List<Pair<String, JSONObject>> operations, int index) {
		if (index < 0) {
			// failed before the first element noting to rollback
			return;
		}

		for (int j = index; j >= 0; j--) {
			SchedulerContext context = state.get(j).getSecond();

			String operationId = operations.get(j).getFirst();
			ExecutableOperation operation = factory.getExecutor(operationId);

			try {
				SchedulerEntryStatus status;
				if (operation != null) {
					// try to rollback and update state
					if (operation.rollback(context)) {
						status = SchedulerEntryStatus.ROLLBACKED;
					} else {
						// fail internally
						status = SchedulerEntryStatus.ROLLBACK_FAILED;
					}
				} else {
					// if the operation is missing then we probably couldn't run it
					status = SchedulerEntryStatus.NOT_RUN;
				}
				setState(state, j, j, status);
			} catch (Exception e) {
				// if the rollback failed then mark it as such and skip the exception
				// we does not want to stop or throw exceptions
				setState(state, j, j, SchedulerEntryStatus.ROLLBACK_FAILED);
				if (operation != null) {
					context.put(
							CTX_STATUS_MESSAGE,
							"Failed to rollback " + operation.getOperation() + " due to "
									+ e.getMessage());
					addStackTrace(context, e);
					LOGGER.debug("Failed to rollback {} due to {}", operation.getOperation(),
							e.getMessage(), e);
				}
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
	private void addStackTrace(SchedulerContext context, Exception e) {
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		e.printStackTrace(printWriter);
		printWriter.close();
		if (!context.containsKey(CTX_STACK_TRACE)) {
			context.put(CTX_STACK_TRACE, writer.toString());
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
	private void setState(List<Triplet<SchedulerEntryStatus, SchedulerContext, Object>> state,
			int from, int to, SchedulerEntryStatus status) {
		if (from < 0) {
			return;
		}
		int localTo = Math.min(to, state.size());
		if (from == localTo) {
			state.get(from).setFirst(status);
			return;
		}
		if (from < localTo) {
			return;
		}
		for (int i = from; i < localTo; i++) {
			state.get(i).setFirst(status);
		}
	}

	/**
	 * Initializes the state list. The method should populate all elements of the returned list as
	 * {@link SchedulerEntryStatus#NOT_RUN}.
	 *
	 * @param length
	 *            the length of the expected list
	 * @return the list
	 */
	private List<Triplet<SchedulerEntryStatus, SchedulerContext, Object>> initState(int length) {
		List<Triplet<SchedulerEntryStatus, SchedulerContext, Object>> state = new ArrayList<>(
				length);
		for (int i = 0; i < length; i++) {
			state.add(new Triplet<SchedulerEntryStatus, SchedulerContext, Object>(
					SchedulerEntryStatus.NOT_RUN, null, null));
		}
		return state;
	}

	/**
	 * Parses and validates the request. Extracts the operation id from each entry and add it to the
	 * result.
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
	private List<Pair<String, JSONObject>> parseRequest(String data) {
		List<Pair<String, JSONObject>> result = Collections.emptyList();
		if (StringUtils.isNullOrEmpty(data)) {
			return result;
		}
		try {
			JSONObject object = new JSONObject(data);
			JSONArray array = JsonUtil.getJsonArray(object, OPERATIONS);
			if (array != null) {
				result = new ArrayList<>(array.length());

				for (int i = 0; i < array.length(); i++) {
					Object value = array.get(i);
					if (value instanceof JSONObject) {
						JSONObject jsonObject = (JSONObject) value;
						String operation = JsonUtil.getStringValue(jsonObject, OPERATION);
						if (StringUtils.isNotNullOrEmpty(operation)) {
							result.add(new Pair<>(operation, jsonObject));
						} else {
							LOGGER.warn("Recived operation entry with missing or empty operation "
									+ "field: {}", value);
						}
					} else {
						LOGGER.warn("Recieved operation entry that is not valid JSONObject: {}",
								value);
					}
				}
			}
		} catch (JSONException e) {
			LOGGER.error("Failed to read request due to error {}", e.getMessage(), e);
			return Collections.emptyList();
		}

		return result;
	}
}
