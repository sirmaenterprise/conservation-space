package com.sirma.itt.emf.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.TreeNode;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Utility class for helper methods for working with JSON objects REVIEW - 1. may be move in emf core 2. exception could
 * be thrown if key is not found
 *
 * @author BBonev
 */
public class JsonTreeBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonTreeBuilder.class);

	/**
	 * Instantiates a new json util.
	 */
	private JsonTreeBuilder() {
		// utility class
	}

	/**
	 * Builds a tree from the given list of nodes.
	 *
	 * @param <T>
	 *            the node to build the tree from
	 * @param <I>
	 *            the id type
	 * @param entries
	 *            the entries
	 * @param converter
	 *            the converter
	 * @return the jSON object
	 */
	public static <I, T extends TreeNode<I>> JSONArray buildTree(List<T> entries, TypeConverter converter) {
		JSONArray tasksTree = new JSONArray();
		JSONObject sortedByParentTasks = new JSONObject();
		try {
			tasksTree = makeTree(entries, tasksTree, sortedByParentTasks, converter, null);
		} catch (JSONException e) {
			LOGGER.warn("Failed to build tree due to " + e.getMessage());
			LOGGER.trace("", e);
			return new JSONArray(Arrays.asList(mapError(e.getMessage())));
		}

		return tasksTree;
	}

	/**
	 * Builds the tree representation same as the given template as JSON.
	 *
	 * @param <T>
	 *            the node to build the tree from
	 * @param <I>
	 *            the id type
	 * @param template
	 *            the template
	 * @param entries
	 *            the entries
	 * @param converter
	 *            the converter
	 * @return the jSON array
	 */
	public static <I, T extends TreeNode<I>> JSONArray buildTreeAs(JSONArray template, List<T> entries,
			TypeConverter converter) {
		JSONArray tasksTree = new JSONArray();
		JSONObject sortedByParentTasks = new JSONObject();
		try {
			tasksTree = makeTree(entries, tasksTree, sortedByParentTasks, converter, template);
		} catch (JSONException e) {
			LOGGER.warn("Failed to make tree due to " + e.getMessage());
			LOGGER.trace("", e);
			return new JSONArray(Arrays.asList(mapError(e.getMessage())));
		}
		return tasksTree;
	}

	/**
	 * Make tree.
	 *
	 * @param <T>
	 *            the generic type
	 * @param <I>
	 *            the generic type
	 * @param list
	 *            the list
	 * @param tasksTree
	 *            The tasks tree.
	 * @param sortedByParentTasks
	 *            The sorted by parent tasks.
	 * @param converter
	 *            the converter
	 * @param template
	 *            the template
	 * @return the jSON array
	 * @throws JSONException
	 *             the jSON exception
	 */
	private static <I, T extends TreeNode<I>> JSONArray makeTree(List<T> list, JSONArray tasksTree,
			JSONObject sortedByParentTasks, TypeConverter converter, JSONArray template) throws JSONException {

		Iterator<T> listIterator = list.iterator();
		String parentId;

		while (listIterator.hasNext()) {
			T task = listIterator.next();
			JSONArray equalParentId;
			parentId = "" + task.getParentId();
			JSONObject taskJSON = converter.convert(JSONObject.class, task);

			if (sortedByParentTasks.has(parentId)) {
				sortedByParentTasks.accumulate(parentId, taskJSON);
			} else {
				equalParentId = new JSONArray();
				equalParentId.put(taskJSON);
				sortedByParentTasks.put(parentId, equalParentId);
			}
		}

		if (!list.isEmpty()) {
			if (template == null) {
				if (sortedByParentTasks.has("null")) {
					addNodes(sortedByParentTasks.getJSONArray("null"), tasksTree, sortedByParentTasks);
				} else {
					String next = (String) sortedByParentTasks.keys().next();
					addNodes(sortedByParentTasks.getJSONArray(next), tasksTree, sortedByParentTasks);
				}
			} else {
				for (int i = 0; i < template.length(); i++) {
					JSONObject object = template.getJSONObject(i);
					String parent = com.sirma.itt.seip.json.JsonUtil.getStringValue(object,
							DefaultProperties.PARENT_ID);
					if (parent == null) {
						if (sortedByParentTasks.has("null")) {
							addNodes(sortedByParentTasks.getJSONArray("null"), tasksTree, sortedByParentTasks);
						} else {
							throw new EmfRuntimeException("Invalid parent: \"null\"");
						}
					} else {
						if (sortedByParentTasks.has(parent)) {
							addNodes(sortedByParentTasks.getJSONArray(parent), tasksTree, sortedByParentTasks);
						} else {
							throw new EmfRuntimeException("Invalid parent: " + parent);
						}
					}
				}
			}

		}

		return tasksTree;
	}

	/**
	 * Adds the nodes.
	 *
	 * @param nodes
	 *            the nodes
	 * @param tasksTree
	 *            the tasks tree
	 * @param sortedByParentTasks
	 *            the sorted by parent tasks
	 * @throws JSONException
	 *             the jSON exception
	 */
	private static void addNodes(JSONArray nodes, JSONArray tasksTree, JSONObject sortedByParentTasks)
			throws JSONException {
		for (int i = 0, l = nodes.length(); i < l; i++) {
			JSONObject taskChildrenObject = nodes.getJSONObject(i);
			listHierarchy(taskChildrenObject, sortedByParentTasks);
			tasksTree.put(taskChildrenObject);
		}
	}

	/**
	 * List hierarchy.
	 *
	 * @param task
	 *            the task
	 * @param sortedByParentTasks
	 *            the sorted by parent tasks
	 * @throws JSONException
	 *             the jSON exception
	 */
	private static void listHierarchy(JSONObject task, JSONObject sortedByParentTasks) throws JSONException {
		JSONArray childrenArray = new JSONArray();

		Object id = com.sirma.itt.seip.json.JsonUtil.getValueOrNull(task, DefaultProperties.ID);
		if (id == null) {
			return;
		}
		JSONArray childNodes = com.sirma.itt.seip.json.JsonUtil.getJsonArray(sortedByParentTasks, "" + id);

		if (childNodes != null && childNodes.length() > 0) {
			for (int i = 0, l = childNodes.length(); i < l; i++) {
				JSONObject childObject = childNodes.getJSONObject(i);
				childrenArray.put(childObject);
				try {
					task.put("children", childrenArray);
					task.put(DefaultProperties.LEAF, false);
					task.put("expanded", true);
				} catch (JSONException e) {
					LOGGER.error("Failed to sava given values", e);
				}

				listHierarchy(childObject, sortedByParentTasks);
			}
		}
	}

	/**
	 * Generates modelMap to return in the modelAndView in case of exception.
	 *
	 * @param msg
	 *            message
	 * @return the string
	 */
	public static JSONObject mapError(String msg) {

		JSONObject modelMap = new JSONObject();
		com.sirma.itt.seip.json.JsonUtil.addToJson(modelMap, "message", msg);
		com.sirma.itt.seip.json.JsonUtil.addToJson(modelMap, "success", Boolean.FALSE);
		return modelMap;
	}

}
