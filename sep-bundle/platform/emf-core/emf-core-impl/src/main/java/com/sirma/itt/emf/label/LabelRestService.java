package com.sirma.itt.emf.label;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.util.JsonUtil;

/**
 * Rest service that provides access to internal labels.
 * 
 * @author BBonev
 */
@Path("/label")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class LabelRestService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(LabelRestService.class);

	/** The label provider. */
	@Inject
	private LabelProvider labelProvider;

	/**
	 * Gets the label.
	 * 
	 * @param labelId
	 *            the label id
	 * @param lang
	 *            the lang
	 * @return the label
	 */
	@GET
	@Path("/{labelId}")
	public String getLabel(@PathParam("labelId") String labelId, @QueryParam("lang") String lang) {
		JSONObject object = new JSONObject();
		if (StringUtils.isNullOrEmpty(lang)) {
			JsonUtil.addToJson(object, labelId, labelProvider.getLabel(labelId));
		} else {
			JsonUtil.addToJson(object, labelId, labelProvider.getLabel(labelId, lang));
		}
		return object.toString();
	}

	/**
	 * Gets the label.
	 * 
	 * @param labelId
	 *            the label id
	 * @return the label
	 */
	@GET
	@Path("/bundle/{labelId}")
	public String getBundleLabel(@PathParam("labelId") String labelId) {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, labelId, labelProvider.getValue(labelId));
		return object.toString();
	}

	/**
	 * Gets the labels.
	 * 
	 * @param data
	 *            the data
	 * @return the labels
	 */
	@POST
	@Path("/multi")
	public String getLabels(String data) {
		if (StringUtils.isNullOrEmpty(data)) {
			return "{}";
		}
		JSONObject result = new JSONObject();
		JSONArray jsonObject = JsonUtil.toJsonArray(data);
		for (int i = 0; i < jsonObject.length(); i++) {
			try {
				String key = jsonObject.getString(i);
				String label = labelProvider.getLabel(key);
				JsonUtil.addToJson(result, key, label);
			} catch (JSONException e) {
				LOGGER.warn("Failed to get json array element due to: ", e);
			}
		}
		return result.toString();
	}

}
