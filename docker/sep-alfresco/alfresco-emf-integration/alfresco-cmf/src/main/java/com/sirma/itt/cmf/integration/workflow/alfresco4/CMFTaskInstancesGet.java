/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package com.sirma.itt.cmf.integration.workflow.alfresco4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.repo.workflow.WorkflowReportServiceImpl;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery.OrderBy;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.util.ModelUtil;
import org.alfresco.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.StringUtils;

import com.sirma.itt.cmf.integration.model.CMFModel;
import com.sirma.itt.cmf.integration.webscript.BaseAlfrescoScript;

/**
 * Webscript impelementation to return workflow task instances.
 *
 * @author Nick Smith
 * @author Gavin Cornwell
 * @author bbanchev
 * @since 3.4
 */
public class CMFTaskInstancesGet extends BaseAlfrescoScript {

	/** The Constant EMPTY. */
	public static final String EMPTY = "";

	/** The Constant NULL. */
	public static final String NULL = "null";

	/** The Constant PARAM_MAX_ITEMS. */
	public static final String PARAM_MAX_ITEMS = "maxItems";

	/** The Constant PARAM_SKIP_COUNT. */
	public static final String PARAM_SKIP_COUNT = "skipCount";

	/** The Constant PARAM_EXCLUDE. */
	public static final String PARAM_EXCLUDE = "exclude";

	// used for results pagination: indicates that all items from list should be
	// returned
	/** The Constant DEFAULT_MAX_ITEMS. */
	public static final int DEFAULT_MAX_ITEMS = -1;

	// used for results pagination: indicates that no items should be skipped
	/** The Constant DEFAULT_SKIP_COUNT. */
	public static final int DEFAULT_SKIP_COUNT = 0;
	/** The Constant PARAM_AUTHORITY. */
	public static final String PARAM_AUTHORITY = "authority";

	/** The Constant PARAM_STATE. */
	public static final String PARAM_STATE = "state";

	/** The Constant PARAM_PRIORITY. */
	public static final String PARAM_PRIORITY = "priority";

	/** The Constant PARAM_DUE_BEFORE. */
	public static final String PARAM_DUE_BEFORE = "dueBefore";

	/** The Constant PARAM_DUE_AFTER. */
	public static final String PARAM_DUE_AFTER = "dueAfter";

	/** The Constant PARAM_PROPERTIES. */
	public static final String PARAM_PROPERTIES = "properties";

	/** The Constant PARAM_POOLED_TASKS. */
	public static final String PARAM_POOLED_TASKS = "pooledTasks";

	/** The Constant VAR_WORKFLOW_INSTANCE_ID. */
	public static final String VAR_WORKFLOW_INSTANCE_ID = "workflow_instance_id";

	/** The task comparator. */
	private WorkflowTaskDueAscComparator taskComparator = new WorkflowTaskDueAscComparator();

	/**
	 * Execute internal. Wrapper for system user action.
	 *
	 * @param req
	 *            the original request
	 * @param status
	 *            the status
	 * @param cache
	 *            the used cache
	 * @return the updated model
	 */
	protected Map<String, Object> executeInternal(WebScriptRequest req) {
		WorkflowModelBuilder modelBuilder = new WorkflowModelBuilder(getNamespaceService(), nodeService,
				getAuthenticationService(), getPersonService(), getWorkflowService(), getWorkflowReportService());
		Map<String, Object> model = new HashMap<String, Object>();
		String content = null;
		try {
			model.put(KEY_WORKING_MODE, "default");
			// specific search for cases
			String servicePath = req.getServicePath();
			if (servicePath.contains("/cmf/search/task")) {
				model.put(KEY_WORKING_MODE, "light");
				content = req.getContent().getContent();
				JSONObject request = new JSONObject(content);
				debug("Task Search Request ", request);
				// general search
				JSONObject paging = null;
				if (request.has("paging")) {
					paging = request.getJSONObject("paging");
				}
				JSONObject additional = null;
				JSONArray sort = null;
				if (request.has("sort")) {
					sort = request.getJSONArray("sort");
				}
				Pair<String, String> context = null;
				String query = request.getString(KEY_QUERY);
				if (request.has(KEY_CONTEXT)) {
					context = new Pair<String, String>(CMFModel.PROP_CONTAINER.toString(),
							request.getString(KEY_CONTEXT));
				}
				if (!org.apache.commons.lang.StringUtils.isBlank(query)) {
					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append("PATH:\"").append(WorkflowReportServiceImpl.SYSTEM_TASK_INDEXES_SPACE)
							.append("\"");
					additional = new JSONObject();
					additional.put("FILTER", stringBuilder.toString());
				}
				Pair<List<NodeRef>, Map<String, Object>> nodeRefs = cmfService.search(context, query, paging, sort,
						additional);

				model.put("results", nodeRefs.getFirst());
				model.put("paging", nodeRefs.getSecond());
				return model;

			} else {
				return buildModel(modelBuilder, req);
			}
		} catch (Exception e) {
			if (e.getMessage() != null) {
				throw new WebScriptException(500,
						"Unexpected error occurred during task search operation: " + e.getMessage());
			}
			throw new WebScriptException(500,
					"Unexpected error occurred during task search operation: " + e.getClass().getName(), e);
		}
	}

	/**
	 * Processes the given date filter parameter from the provided webscript
	 * request.
	 *
	 * If the parameter is present but set to an empty string or to "null" the
	 * date is added to the given filters Map as "", if the parameter contains
	 * an ISO8601 date it's added as a Date object to the filters.
	 *
	 * @param req
	 *            The WebScript request
	 * @param paramName
	 *            The name of the parameter to look for
	 * @param filters
	 *            Map of filters to add the date to
	 */
	protected void processDateFilter(WebScriptRequest req, String paramName, Map<String, Object> filters) {
		String dateParam = req.getParameter(paramName);
		if (dateParam != null) {
			Object date = EMPTY;
			if (!EMPTY.equals(dateParam) && !NULL.equals(dateParam)) {
				date = getDateParameter(req, paramName);
			}
			filters.put(paramName, date);
		}
	}

	/**
	 * Retrieves the named paramter as a date.
	 *
	 * @param req
	 *            The WebScript request
	 * @param paramName
	 *            The name of parameter to look for
	 * @return The request parameter value or null if the parameter is not
	 *         present
	 */
	protected Date getDateParameter(WebScriptRequest req, String paramName) {
		String dateString = req.getParameter(paramName);

		if (dateString != null) {
			try {
				return new Date(Long.parseLong(dateString));
			} catch (Exception e) {
				try {
					return ISO8601DateFormat.parse(dateString.replaceAll(" ", "+"));
				} catch (Exception e1) {
					String msg = "Invalid date value: " + dateString;
					throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
				}
			}
		}
		return null;
	}

	/**
	 * Retrieves the named parameter as an integer, if the parameter is not
	 * present the default value is returned.
	 *
	 * @param req
	 *            The WebScript request
	 * @param paramName
	 *            The name of parameter to look for
	 * @param defaultValue
	 *            The default value that should be returned if parameter is not
	 *            present in request or if it is not positive
	 * @return The request parameter or default value
	 */
	protected int getIntParameter(WebScriptRequest req, String paramName, int defaultValue) {
		String paramString = req.getParameter(paramName);

		if (paramString != null) {
			try {
				int param = Integer.valueOf(paramString);

				if (param > 0) {
					return param;
				}
			} catch (NumberFormatException e) {
				throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			}
		}

		return defaultValue;
	}

	/**
	 * Builds the results model, applying pagination to the results if
	 * necessary.
	 *
	 * @param req
	 *            The WebScript request
	 * @param dataPropertyName
	 *            The name of the property to use in the model
	 * @param results
	 *            The full set of results
	 *
	 * @return List of results to return to the callee
	 */
	protected Map<String, Object> createResultModel(WebScriptRequest req, String dataPropertyName,
			List<Map<String, Object>> results) {
		int totalItems = results.size();
		int maxItems = getIntParameter(req, PARAM_MAX_ITEMS, DEFAULT_MAX_ITEMS);
		int skipCount = getIntParameter(req, PARAM_SKIP_COUNT, DEFAULT_SKIP_COUNT);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put(dataPropertyName, applyPagination(results, maxItems, skipCount));

		if ((maxItems != DEFAULT_MAX_ITEMS) || (skipCount != DEFAULT_SKIP_COUNT)) {
			// maxItems or skipCount parameter was provided so we need to
			// include paging into response
			model.put("paging", ModelUtil.buildPaging(totalItems, maxItems == DEFAULT_MAX_ITEMS ? totalItems : maxItems,
					skipCount));
		}

		return model;
	}

	/**
	 * Make the pagination for given list of objects.
	 *
	 * @param results
	 *            the initial list of objects for pagination
	 * @param maxItems
	 *            maximum count of elements that should be included in paging
	 *            result
	 * @param skipCount
	 *            the count of elements that should be skipped
	 * @return List of paginated results
	 */
	protected List<Map<String, Object>> applyPagination(List<Map<String, Object>> results, int maxItems,
			int skipCount) {
		if ((maxItems == DEFAULT_MAX_ITEMS) && (skipCount == DEFAULT_SKIP_COUNT)) {
			// no need to make pagination
			return results;
		}

		// Do the paging
		return ModelUtil.page(results, maxItems, skipCount);
	}

	/**
	 * Determines whether the given date is a match for the given filter value.
	 *
	 * @param date
	 *            The date to check against
	 * @param filterValue
	 *            The value of the filter, either an empty String or a Date
	 *            object
	 * @param dateBeforeFilter
	 *            true to test the date is before the filterValue, false to test
	 *            the date is after the filterValue
	 * @return true if the date is a match for the filterValue
	 */
	protected boolean isDateMatchForFilter(Date date, Object filterValue, boolean dateBeforeFilter) {
		boolean match = true;

		if (filterValue.equals(EMPTY)) {
			if (date != null) {
				match = false;
			}
		} else {
			if (date == null) {
				match = false;
			} else {
				if (dateBeforeFilter) {
					if (date.getTime() >= ((Date) filterValue).getTime()) {
						match = false;
					}
				} else {
					if (date.getTime() <= ((Date) filterValue).getTime()) {
						match = false;
					}
				}
			}
		}

		return match;
	}

	/**
	 * Helper class to check for excluded items.
	 */
	public class ExcludeFilter {

		/** The Constant WILDCARD. */
		private static final String WILDCARD = "*";

		/** The exact filters. */
		private List<String> exactFilters;

		/** The wilcard filters. */
		private List<String> wilcardFilters;

		/** The contains wildcards. */
		private boolean containsWildcards = false;

		/**
		 * Creates a new ExcludeFilter.
		 *
		 * @param filters
		 *            Comma separated list of filters which can optionally
		 *            contain wildcards
		 */
		public ExcludeFilter(String filters) {
			// tokenize the filters
			String[] filterArray = StringUtils.tokenizeToStringArray(filters, ",");

			// create a list of exact filters and wildcard filters
			exactFilters = new ArrayList<String>(filterArray.length);
			wilcardFilters = new ArrayList<String>(filterArray.length);

			for (String filter : filterArray) {
				if (filter.endsWith(WILDCARD)) {
					// at least one wildcard is present
					containsWildcards = true;

					// add the filter without the wildcard
					wilcardFilters.add(filter.substring(0, (filter.length() - WILDCARD.length())));
				} else {
					// add the exact filter
					exactFilters.add(filter);
				}
			}
		}

		/**
		 * Determines whether the given item matches one of the filters.
		 *
		 * @param item
		 *            The item to check
		 * @return true if the item matches one of the filters
		 */
		public boolean isMatch(String item) {
			// see whether there is an exact match
			boolean match = exactFilters.contains(item);

			// if there wasn't an exact match and wildcards are present
			if ((item != null) && !match && containsWildcards) {
				for (String wildcardFilter : wilcardFilters) {
					if (item.startsWith(wildcardFilter)) {
						match = true;
						break;
					}
				}
			}

			return match;
		}
	}

	/**
	 * Builds the model.
	 *
	 * @param modelBuilder
	 *            the model builder
	 * @param req
	 *            the req from script
	 * @return the model map
	 */
	protected Map<String, Object> buildModel(WorkflowModelBuilder modelBuilder, WebScriptRequest req) {
		Map<String, String> params = req.getServiceMatch().getTemplateVars();
		Map<String, Object> filters = new HashMap<String, Object>(4);
		long start = System.currentTimeMillis();
		// authority is not included into filters list as it will be taken into
		// account before filtering
		String authority = getAuthority(req);

		// state is also not included into filters list, for the same reason
		WorkflowTaskState state = getState(req);

		// look for a workflow instance id
		String workflowInstanceId = params.get(VAR_WORKFLOW_INSTANCE_ID);

		// determine if pooledTasks should be included, when appropriate i.e.
		// when an authority is supplied
		Boolean pooledTasksOnly = getPooledTasks(req);

		// get list of properties to include in the response
		List<String> properties = getProperties(req);

		// get filter param values
		filters.put(PARAM_PRIORITY, req.getParameter(PARAM_PRIORITY));
		processDateFilter(req, PARAM_DUE_BEFORE, filters);
		processDateFilter(req, PARAM_DUE_AFTER, filters);

		String excludeParam = req.getParameter(PARAM_EXCLUDE);
		if ((excludeParam != null) && (excludeParam.length() > 0)) {
			filters.put(PARAM_EXCLUDE, new ExcludeFilter(excludeParam));
		}

		List<WorkflowTask> allTasks;

		if (workflowInstanceId != null) {
			// a workflow instance id was provided so query for tasks
			WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
			taskQuery.setActive(null);
			taskQuery.setProcessId(workflowInstanceId);
			taskQuery.setTaskState(state);
			taskQuery.setOrderBy(new OrderBy[] { OrderBy.TaskDue_Asc });

			if (authority != null) {
				taskQuery.setActorId(authority);
			}

			allTasks = getWorkflowService().queryTasks(taskQuery, true);
		} else {
			// default task state to IN_PROGRESS if not supplied
			// if (state == null) {
			// state = WorkflowTaskState.IN_PROGRESS;
			// }

			// no workflow instance id is present so get all tasks
			if (authority != null) {
				List<WorkflowTask> tasks = getWorkflowService().getAssignedTasks(authority, state);
				List<WorkflowTask> pooledTasks = getWorkflowService().getPooledTasks(authority);
				if (pooledTasksOnly != null) {
					if (pooledTasksOnly.booleanValue()) {
						// only return pooled tasks the user can claim
						allTasks = new ArrayList<WorkflowTask>(pooledTasks.size());
						allTasks.addAll(pooledTasks);
					} else {
						// only return tasks assigned to the user
						allTasks = new ArrayList<WorkflowTask>(tasks.size());
						allTasks.addAll(tasks);
					}
				} else {
					// include both assigned and unassigned tasks
					allTasks = new ArrayList<WorkflowTask>(tasks.size() + pooledTasks.size());
					allTasks.addAll(tasks);
					allTasks.addAll(pooledTasks);
				}

				// sort tasks by due date
				Collections.sort(allTasks, taskComparator);
			} else {
				// authority was not provided -> return all active tasks in the
				// system
				WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
				taskQuery.setTaskState(state);
				taskQuery.setActive(null);
				taskQuery.setOrderBy(new OrderBy[] { OrderBy.TaskDue_Asc });
				allTasks = getWorkflowService().queryTasks(taskQuery, true);
			}
		}

		// filter results
		ArrayList<Map<String, Object>> results = new ArrayList<Map<String, Object>>(allTasks.size());
		boolean included = false;
		for (int i = 0; i < allTasks.size(); i++) {
			WorkflowTask task = allTasks.get(i);
			if (matches(task, filters)) {
				results.add(modelBuilder.buildSimple(task, properties, !included, false));
				// only include workflowInstance if the tasks are from diff
				// workflows.
				included = workflowInstanceId != null;
			}
		}
		System.out.println("Exec TIME " + (System.currentTimeMillis() - start));
		// create and return results, paginated if necessary
		return createResultModel(req, "taskInstances", results);
	}

	/**
	 * Retrieves the list of property names to include in the response.
	 *
	 * @param req
	 *            The WebScript request
	 * @return List of property names
	 */
	private List<String> getProperties(WebScriptRequest req) {
		String propertiesStr = req.getParameter(PARAM_PROPERTIES);
		if (propertiesStr != null) {
			return Arrays.asList(propertiesStr.split(","));
		}
		return null;
	}

	/**
	 * Retrieves the pooledTasks parameter.
	 *
	 * @param req
	 *            The WebScript request
	 * @return null if not present, Boolean object otherwise
	 */
	private Boolean getPooledTasks(WebScriptRequest req) {
		Boolean result = null;
		String includePooledTasks = req.getParameter(PARAM_POOLED_TASKS);

		if (includePooledTasks != null) {
			result = Boolean.valueOf(includePooledTasks);
		}

		return result;
	}

	/**
	 * Gets the specified {@link WorkflowTaskState}, null if not requested.
	 *
	 * @param req
	 *            the req
	 * @return the state
	 */
	private WorkflowTaskState getState(WebScriptRequest req) {
		String stateName = req.getParameter(PARAM_STATE);
		if (stateName != null) {
			try {
				return WorkflowTaskState.valueOf(stateName.toUpperCase());
			} catch (IllegalArgumentException e) {
				String msg = "Unrecognised State parameter: " + stateName;
				throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
			}
		}

		return null;
	}

	/**
	 * Returns the specified authority. If no authority is specified then
	 * returns the current Fully Authenticated user.
	 *
	 * @param req
	 *            the req
	 * @return the authority
	 */
	private String getAuthority(WebScriptRequest req) {
		String authority = req.getParameter(PARAM_AUTHORITY);
		if ((authority == null) || (authority.length() == 0)) {
			authority = null;
		}
		return authority;
	}

	/**
	 * Determine if the given task should be included in the response.
	 *
	 * @param task
	 *            The task to check
	 * @param filters
	 *            The list of filters the task must match to be included
	 * @return true if the task matches and should therefore be returned
	 */
	private boolean matches(WorkflowTask task, Map<String, Object> filters) {
		// by default we assume that workflow task should be included
		boolean result = true;

		for (String key : filters.keySet()) {
			Object filterValue = filters.get(key);

			// skip null filters (null value means that filter was not
			// specified)
			if (filterValue != null) {
				if (key.equals(PARAM_EXCLUDE)) {
					ExcludeFilter excludeFilter = (ExcludeFilter) filterValue;
					String type = task.getDefinition().getMetadata().getName().toPrefixString(getNamespaceService());
					if (excludeFilter.isMatch(type)) {
						result = false;
						break;
					}
				} else if (key.equals(PARAM_DUE_BEFORE)) {
					Date dueDate = (Date) task.getProperties().get(WorkflowModel.PROP_DUE_DATE);

					if (!isDateMatchForFilter(dueDate, filterValue, true)) {
						result = false;
						break;
					}
				} else if (key.equals(PARAM_DUE_AFTER)) {
					Date dueDate = (Date) task.getProperties().get(WorkflowModel.PROP_DUE_DATE);

					if (!isDateMatchForFilter(dueDate, filterValue, false)) {
						result = false;
						break;
					}
				} else if (key.equals(PARAM_PRIORITY)) {
					if (!filterValue.toString()
							.equals(task.getProperties().get(WorkflowModel.PROP_PRIORITY).toString())) {
						result = false;
						break;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Comparator to sort workflow tasks by due date in ascending order.
	 */
	class WorkflowTaskDueAscComparator implements Comparator<WorkflowTask> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(WorkflowTask o1, WorkflowTask o2) {
			Date date1 = (Date) o1.getProperties().get(WorkflowModel.PROP_DUE_DATE);
			Date date2 = (Date) o2.getProperties().get(WorkflowModel.PROP_DUE_DATE);

			long time1 = date1 == null ? Long.MAX_VALUE : date1.getTime();
			long time2 = date2 == null ? Long.MAX_VALUE : date2.getTime();

			long result = time1 - time2;

			return (result > 0) ? 1 : (result < 0 ? -1 : 0);
		}

	}

}
