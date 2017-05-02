package com.sirma.itt.seip.tenant.wizard;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.tenant.wizard.exception.TenantCreationException;

/**
 * Holder for tenant step process. To build instances of this type use {@link TenantInitializationModelBuilder}.
 *
 * @author bbanchev
 */
public class TenantInitializationModel implements JsonRepresentable {
	/** The models. */
	private Map<String, TenantStepData> models = new LinkedHashMap<>();

	/**
	 * Adds new tenant step.
	 *
	 * @param stepModel
	 *            the step model
	 */
	public void add(TenantStepData stepModel) {
		models.put(stepModel.getIdentifier(), stepModel);
	}

	/**
	 * List the current steps.
	 *
	 * @return the collection
	 */
	public Collection<TenantStepData> list() {
		return models.values();
	}

	/**
	 * Gets the.
	 *
	 * @param stepname
	 *            the stepname
	 * @return the tenant step data
	 */
	public TenantStepData get(String stepname) {
		return models.computeIfAbsent(stepname, TenantStepData::createEmpty);
	}

	/**
	 * Update the data for single step.
	 *
	 * @param data
	 *            the data
	 */
	public void update(TenantStepData data) {
		models.put(data.getIdentifier(), data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JSONObject toJSONObject() {
		JSONObject result = new JSONObject();
		JSONArray steps = new JSONArray();
		for (TenantStepData step : models.values()) {
			steps.put(step.toJSONObject());
		}
		JsonUtil.addToJson(result, "data", steps);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		try {
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			Map<String, TenantStepData> newModel = new LinkedHashMap<>(jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject step = jsonArray.getJSONObject(i);
				String identifier = step.getString("id");
				newModel.put(identifier, new TenantStepData(identifier, step));
			}
			models = newModel;
		} catch (JSONException e) {
			throw new TenantCreationException("Invalid model is provided!", e);
		}
	}

}
