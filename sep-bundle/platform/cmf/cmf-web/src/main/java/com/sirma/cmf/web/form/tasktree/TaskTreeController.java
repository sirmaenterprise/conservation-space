package com.sirma.cmf.web.form.tasktree;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.myfaces.extensions.cdi.core.api.scope.conversation.ViewAccessScoped;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.cmf.web.Action;
import com.sirma.itt.cmf.beans.model.AbstractTaskInstance;
import com.sirma.itt.cmf.beans.model.StandaloneTaskInstance;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.emf.converter.SerializableConverter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * TaskTreeController is responsible for providing data for task tree component.
 * 
 * @author svelikov
 */
@Named
@ViewAccessScoped
public class TaskTreeController extends Action implements Serializable {

	private static final long serialVersionUID = -7174074266353673141L;

	@Inject
	private TaskService taskService;

	@Inject
	@SerializableConverter
	private TypeConverter typeConverter;

	/**
	 * Task tree data.
	 * 
	 * @return the string
	 */
	public String taskTreeData() {
		StandaloneTaskInstance instance = getDocumentContext().getInstance(
				StandaloneTaskInstance.class);
		String taskTreeJson = "[]";
		if (instance != null) {
			List<AbstractTaskInstance> owningInstanceTasks = taskService.getSubTasks(instance);
			taskTreeJson = getTaskTreeJson(instance, owningInstanceTasks);
		}
		return taskTreeJson;
	}

	/**
	 * Gets the task tree json.
	 * 
	 * @param instance
	 *            the instance
	 * @param tasklist
	 *            the tasklist
	 * @return the task tree json
	 */
	protected String getTaskTreeJson(StandaloneTaskInstance instance,
			List<AbstractTaskInstance> tasklist) {
		JSONObject root = new JSONObject();
		JsonUtil.addToJson(root, DefaultProperties.PARENT_ID, instance.getParentId());
		JSONArray template = new JSONArray();
		template.put(root);

		JSONArray tasktree = JsonUtil.buildTreeAs(template, tasklist, typeConverter);
		// remove the root itself if it the only task. otherwise is needed for the tree
		try {
			if (tasktree.length() == 0) {
				return "[]";
			}

			JSONObject treeRoot = tasktree.getJSONObject(0);
			JSONArray children = JsonUtil.getJsonArray(treeRoot, "children");
			if (children != null) {
				return children.toString();
			} else if (EqualsHelper.nullSafeEquals(JsonUtil.getStringValue(treeRoot, "Id"),
					instance.getId())) {
				// if there are no children and the id is the same as the current task then we
				// should not return anything due to bug report that current root task should not be
				// listed in the tree.
				return "[]";
			}
		} catch (JSONException e) {
			log.warn("Failed to retrive root level 'children' from tree: " + tasktree, e);
		}
		return tasktree.toString();
	}

	/**
	 * Gets the context.
	 * 
	 * @param currentInstance
	 *            the current instance
	 * @return the context
	 */
	protected Instance getContext(Instance currentInstance) {
		return InstanceUtil.getContext(currentInstance);
	}

}
