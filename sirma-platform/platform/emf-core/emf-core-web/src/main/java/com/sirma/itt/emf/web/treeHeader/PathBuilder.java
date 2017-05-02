package com.sirma.itt.emf.web.treeHeader;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.json.JsonUtil;

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

		List<Instance> path = InstanceUtil.getParentPath(currentInstance, true);

		for (Instance instance : path) {
			String type = instance.type().getCategory();

			JSONObject item = new JSONObject();
			JsonUtil.addToJson(item, "id", instance.getId());
			JsonUtil.addToJson(item, "type", type);
			JsonUtil.addToJson(item, "uri", instance.getProperties().get("uri"));
			JsonUtil.addToJson(item, "compactHeader", instance.getProperties().get(DefaultProperties.HEADER_COMPACT));

			JsonUtil.append(item, "rdfType", instance.type().getId());

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
		return InstanceUtil.getRootInstance(currentInstance);
	}

}
