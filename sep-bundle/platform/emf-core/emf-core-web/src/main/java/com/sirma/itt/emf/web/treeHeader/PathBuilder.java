package com.sirma.itt.emf.web.treeHeader;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.emf.instance.InstanceUtil;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.RootInstanceContext;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Path builder assembles path as json array for the instance hierarchy that are currently loaded.
 * 
 * @author svelikov
 */
@Named
@SessionScoped
public class PathBuilder implements Serializable {

	private static final long serialVersionUID = 8079984335358458046L;

	/**
	 * Builds the path as json array.
	 * 
	 * <pre>
	 * [
	 *     {
	 *         id: '',
	 *         type: '',
	 *         url: ''
	 *     },
	 *     {
	 *         id: '',
	 *         type: '',
	 *         url: ''
	 *     },
	 *     ...
	 * ]
	 * </pre>
	 * 
	 * @param currentInstance
	 *            the current instance
	 * @return the string
	 */
	public String buildPath(Instance currentInstance) {
		JSONArray array = new JSONArray();
		if (currentInstance == null) {
			return array.toString();
		}

		List<Instance> path = InstanceUtil.getParentPath(currentInstance, true, true);

		for (Instance instance : path) {
			JSONObject item = new JSONObject();
			JsonUtil.addToJson(item, "id", instance.getId());
			JsonUtil.addToJson(item, "type", instance.getClass().getSimpleName().toLowerCase());
			JsonUtil.addToJson(item, "uri", instance.getProperties().get("uri"));
			JsonUtil.addToJson(item, "compactHeader",
					instance.getProperties().get(DefaultProperties.HEADER_COMPACT));
			array.put(item);
		}

		return array.toString();
	}

	/**
	 * Gets the root instance.
	 * 
	 * @param currentInstance
	 *            the current instance
	 * @return the root instance
	 */
	public Instance getRootInstance(Instance currentInstance) {
		if (currentInstance instanceof RootInstanceContext) {
			return currentInstance;
		}
		Instance rootInstance = InstanceUtil.getRootInstance(currentInstance, true);
		return rootInstance;
	}

}
