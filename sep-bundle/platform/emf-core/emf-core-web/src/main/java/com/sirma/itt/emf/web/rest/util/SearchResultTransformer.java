package com.sirma.itt.emf.web.rest.util;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.emf.web.header.InstanceHeaderBuilder;
import com.sirma.itt.emf.web.treeHeader.Size;

/**
 * @author yasko
 */
@Named
@ApplicationScoped
public class SearchResultTransformer {

	private static final Pattern HTML_A_TAG = Pattern.compile("<(/?)(a[^>]*)>", Pattern.CANON_EQ);

	/** The rendition service. */
	@Inject
	private InstanceHeaderBuilder treeHeaderBuilder;

	@Inject
	private DictionaryService dictionaryService;

	/**
	 * Transforms a list on {@link Instance} to a json array and sets it as 'values' property of a
	 * specified json object.
	 *
	 * @param total
	 *            Total number items that match the search criteria.
	 * @param instances
	 *            Instances to transform.
	 * @param fields
	 *            Fields to pluck from the instances
	 * @param result
	 *            Json object in which to set the transformed result
	 * @throws JSONException
	 *             error during transformation
	 */
	public void transformResult(int total, List<Instance> instances, List<String> fields,
			JSONObject result) throws JSONException {
		Map<Instance, Boolean> instancesWithPermissions = new LinkedHashMap<>(instances.size());
		for (Instance instance : instances) {
			instancesWithPermissions.put(instance, true);
		}
		transformResult(total, instancesWithPermissions, fields, result);
	}

	/**
	 * Transforms a list on {@link Instance} to a json array and sets it as 'values' property of a
	 * specified json object.
	 *
	 * @param total
	 *            Total number items that match the search criteria.
	 * @param instances
	 *            Instances to transform. A map with an {@link Instance} as a key and a boolean
	 *            value indicating does the user has permissions over the {@link Instance}
	 * @param fields
	 *            Fields to pluck from the instances
	 * @param result
	 *            Json object in which to set the transformed result
	 * @throws JSONException
	 *             error during transformation
	 */
	public void transformResult(int total, Map<Instance, Boolean> instances, List<String> fields,
			JSONObject result) throws JSONException {
		// TODO: Move this method in some util class
		JSONArray values = new JSONArray();
		for (Entry<Instance, Boolean> instanceEntry : instances.entrySet()) {
			JSONObject item = transformInstance(instanceEntry.getKey(), !instanceEntry.getValue(),
					fields);
			if (item != null) {
				values.put(item);
			}
		}
		result.put("values", values);
		result.put("resultSize", total);
		result.put("success", true);
		result.put("idProperty", "dbId");

		JSONObject meta4extjs = new JSONObject();
		meta4extjs.put("root", "values");
		result.put("metaData", meta4extjs);
	}

	/**
	 * Transform a single instance to json for search result.
	 *
	 * @param instance
	 *            the instance
	 * @param isDisabled
	 *            the is disabled
	 * @param fields
	 *            the fields
	 * @return the JSON object
	 */
	public JSONObject transformInstance(Instance instance, boolean isDisabled, List<String> fields) {
		DataTypeDefinition dataTypeDefinition = dictionaryService.getDataTypeDefinition(instance
				.getClass().getName());

		Map<String, Serializable> properties = instance.getProperties();
		if (properties == null) {
			// for some reason some of the objects does not have properties
			// this should not happen
			return null;
		}
		// TODO REVIEW: See what what do we always need to be included
		// or rely solely on the fields from the search args
		JSONObject item = new JSONObject();
		// REVIEW FIXME: What is the difference between the next three properties?
		JsonUtil.addToJson(item, "identifier", properties.get(DefaultProperties.UNIQUE_IDENTIFIER));
		JsonUtil.addToJson(item, "dbId", instance.getId());
		JsonUtil.addToJson(item, "name", instance.getId());
		JsonUtil.addToJson(item, "title", properties.get(DefaultProperties.TITLE));
		JsonUtil.addToJson(item, "type", dataTypeDefinition.getName());
		JsonUtil.addToJson(item, "emfType", dataTypeDefinition.getName());
		JsonUtil.addToJson(item, "disabled", isDisabled);
		JsonUtil.addToJson(item, "status", properties.get(DefaultProperties.STATUS));
		JsonUtil.addToJson(item, "domainClass", dataTypeDefinition.getFirstUri());

		JsonUtil.addToJson(item, DefaultProperties.HEADER_DEFAULT, getHeader(instance, DefaultProperties.HEADER_DEFAULT, isDisabled));
		JsonUtil.addToJson(item, DefaultProperties.HEADER_BREADCRUMB, getHeader(instance, DefaultProperties.HEADER_BREADCRUMB, isDisabled));
		JsonUtil.addToJson(item, DefaultProperties.HEADER_COMPACT, getHeader(instance, DefaultProperties.HEADER_COMPACT, isDisabled));
		JsonUtil.addToJson(item, "icon", getIconForInstance(instance));

		if ((fields != null) && !fields.isEmpty()) {
			for (String field : fields) {
				Serializable fieldValue = properties.get(field);
				if (fieldValue instanceof Date) {
					// result to the call for help from bellow
					// this could be moved to upper level to convert
					// everything to string
					fieldValue = TypeConverterUtil.getConverter().convert(String.class, fieldValue);
				}
				JsonUtil.addToJson(item, field, fieldValue);
			}
		}
		return item;
	}

	/**
	 * Gets the icon for instance.
	 *
	 * @param instance
	 *            the instance
	 * @return the icon for instance
	 */
	private String getIconForInstance(Instance instance) {
		return treeHeaderBuilder.getIcon(instance, DefaultProperties.HEADER_DEFAULT,
				Size.BIGGER.getSize(), false);
	}

	/**
	 * Gets the header from the instance. If the header should not be clickable the last argument
	 * should be <code>true</code>.
	 *
	 * @param instance
	 *            the instance
	 * @param key
	 *            the key
	 * @param disabled
	 *            the disabled
	 * @return the header
	 */
	private String getHeader(Instance instance, String key, boolean disabled) {
		String header = (String) instance.getProperties().get(key);
		return disabled? disableClickableLinks(header) : header;
	}

	/**
	 * Disable clickable links.
	 *
	 * @param defaultHeader
	 *            the default header
	 * @return the string
	 */
	private String disableClickableLinks(String defaultHeader) {
		if (defaultHeader == null) {
			return null;
		}
		return HTML_A_TAG.matcher(defaultHeader).replaceAll("<$1span>");
	}
}
