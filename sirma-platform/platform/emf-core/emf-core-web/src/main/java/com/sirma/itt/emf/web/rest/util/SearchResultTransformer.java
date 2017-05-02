package com.sirma.itt.emf.web.rest.util;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.web.header.InstanceHeaderBuilder;
import com.sirma.itt.emf.web.treeHeader.Size;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchablePropertyProperties;
import com.sirma.itt.seip.domain.search.facet.Facet;
import com.sirma.itt.seip.domain.search.facet.FacetProperties;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * @author yasko
 */
@Named
@ApplicationScoped
public class SearchResultTransformer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final Pattern HTML_A_TAG = Pattern.compile("<(/?)(a[^>]*)>", Pattern.CANON_EQ);

	/** The rendition service. */
	@Inject
	private InstanceHeaderBuilder treeHeaderBuilder;

	/**
	 * Transforms a list on {@link Instance} to a json array and sets it as 'values' property of a specified json
	 * object.
	 *
	 * @param total
	 *            Total number items that match the search criteria.
	 * @param instances
	 *            Instances to transform.
	 * @param fields
	 *            Fields to pluck from the instances
	 * @param result
	 *            Json object in which to set the transformed result
	 */
	public void transformResult(int total, Collection<Instance> instances, List<String> fields, JSONObject result) {
		transformResult(total, instances, fields, result, null);
	}

	/**
	 * Transforms a list on {@link Instance} to a json array and sets it as 'values' property of a specified json
	 * object. The method treats all instances as allowed for the user.
	 *
	 * @param total
	 *            Total number items that match the search criteria.
	 * @param instances
	 *            Instances to transform.
	 * @param fields
	 *            Fields to pluck from the instances
	 * @param result
	 *            Json object in which to set the transformed result
	 * @param facets
	 */
	public void transformResult(int total, Collection<Instance> instances, List<String> fields, JSONObject result,
			Map<String, Facet> facets) {
		JSONArray values = new JSONArray();
		for (Instance instance : instances) {
			JSONObject item = transformInstance(instance, false, fields);
			if (item != null) {
				values.put(item);
			}
		}
		appendMetaAndFacets(total, result, facets, values);
	}

	@SuppressWarnings("boxing")
	private void appendMetaAndFacets(int total, JSONObject result, Map<String, Facet> facets, JSONArray values) {
		JsonUtil.addToJson(result, "values", values);
		JsonUtil.addToJson(result, "resultSize", total);
		JsonUtil.addToJson(result, "success", true);
		JsonUtil.addToJson(result, "idProperty", "dbId");

		JSONObject meta4extjs = new JSONObject();
		JsonUtil.addToJson(meta4extjs, "root", "values");
		JsonUtil.addToJson(result, "metaData", meta4extjs);

		// Adds the facets if any
		if (facets != null) {
			JsonUtil.addToJson(result, "facets", transformFacets(facets));
		}
	}

	/**
	 * Transforms a list on {@link Instance} to a json array and sets it as 'values' property of a specified json
	 * object.
	 *
	 * @param total
	 *            Total number items that match the search criteria.
	 * @param instances
	 *            Instances to transform. A map with an {@link Instance} as a key and a boolean value indicating does
	 *            the user has permissions over the {@link Instance}
	 * @param fields
	 *            Fields to pluck from the instances
	 * @param result
	 *            Json object in which to set the transformed result
	 * @param facets
	 */
	@SuppressWarnings("boxing")
	public void transformResult(int total, Map<Instance, Boolean> instances, List<String> fields, JSONObject result,
			Map<String, Facet> facets) {
		JSONArray values = new JSONArray();
		for (Entry<Instance, Boolean> instanceEntry : instances.entrySet()) {
			JSONObject item = transformInstance(instanceEntry.getKey(), !instanceEntry.getValue(), fields);
			if (item != null) {
				values.put(item);
			}
		}
		appendMetaAndFacets(total, result, facets, values);
	}

	/**
	 * Transforms the provided mapping of {@link Facet} to a JSON array.
	 *
	 * @param facets
	 *            - the provided mapping
	 * @return a JSON array representing the facets
	 */
	public JSONArray transformFacets(Map<String, Facet> facets) {
		JSONArray result = new JSONArray();
		for (Facet facet : facets.values()) {
			result.put(JsonUtil.convertToJsonCompatibleValue(facet));
		}
		return result;
	}

	/**
	 * Retrieve all facet ids, states, titles and property types from the provided collection and transform them to a
	 * JSON array.
	 *
	 * @param facets
	 *            the facets
	 * @return all facet ids in a json format
	 */
	public JSONArray getFacets(Collection<Facet> facets) {
		JSONArray result = new JSONArray();
		for (Facet facet : facets) {
			JSONObject facetJSON = new JSONObject();
			JsonUtil.addToJson(facetJSON, SearchablePropertyProperties.ID, facet.getId());
			JsonUtil.addToJson(facetJSON, FacetProperties.STATE, facet.getFacetConfiguration().getState());
			JsonUtil.addToJson(facetJSON, SearchablePropertyProperties.TEXT, facet.getText());
			JsonUtil.addToJson(facetJSON, SearchablePropertyProperties.PROPERTY_TYPE, facet.getPropertyType());
			JsonUtil.addToJson(facetJSON, FacetProperties.ORDER, facet.getFacetConfiguration().getOrder());
			result.put(facetJSON);
		}
		return result;
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
	@SuppressWarnings("boxing")
	public JSONObject transformInstance(Instance instance, boolean isDisabled, List<String> fields) {
		Map<String, Serializable> properties = instance.getProperties();
		if (properties == null) {
			LOGGER.warn("Skipping {}={} from transformation due to missing properties!",
					instance.getClass().getSimpleName(), instance.getId());
			// for some reason some of the objects does not have properties
			// this should not happen
			return null;
		}
		// TODO REVIEW: See what what do we always need to be included
		// or rely solely on the fields from the search args
		JSONObject item = new JSONObject();
		// REVIEW FIXME: What is the difference between the next three
		// properties?
		JsonUtil.addToJson(item, "identifier", properties.get(DefaultProperties.UNIQUE_IDENTIFIER));
		JsonUtil.addToJson(item, "dbId", instance.getId());
		JsonUtil.addToJson(item, "name", instance.getId());
		JsonUtil.addToJson(item, "title", properties.get(DefaultProperties.TITLE));
		JsonUtil.addToJson(item, "type", instance.type().getCategory());
		JsonUtil.addToJson(item, "emfType", instance.type().getCategory());
		JsonUtil.addToJson(item, "disabled", isDisabled);
		JsonUtil.addToJson(item, "status", properties.get(DefaultProperties.STATUS));
		JsonUtil.addToJson(item, "domainClass", instance.type().getId());

		JsonUtil.addToJson(item, DefaultProperties.HEADER_DEFAULT,
				getHeader(instance, DefaultProperties.HEADER_DEFAULT, isDisabled));
		JsonUtil.addToJson(item, DefaultProperties.HEADER_BREADCRUMB,
				getHeader(instance, DefaultProperties.HEADER_BREADCRUMB, isDisabled));
		JsonUtil.addToJson(item, DefaultProperties.HEADER_COMPACT,
				getHeader(instance, DefaultProperties.HEADER_COMPACT, isDisabled));
		JsonUtil.addToJson(item, "icon", getIconForInstance(instance));

		if (fields != null && !fields.isEmpty()) {
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
		return treeHeaderBuilder.getIcon(instance, DefaultProperties.HEADER_DEFAULT, Size.BIGGER.getSize(), false);
	}

	/**
	 * Gets the header from the instance. If the header should not be clickable the last argument should be
	 * <code>true</code>.
	 *
	 * @param instance
	 *            the instance
	 * @param key
	 *            the key
	 * @param disabled
	 *            the disabled
	 * @return the header
	 */
	private static String getHeader(Instance instance, String key, boolean disabled) {
		String header = (String) instance.getProperties().get(key);
		return disabled ? disableClickableLinks(header) : header;
	}

	/**
	 * Disable clickable links.
	 *
	 * @param defaultHeader
	 *            the default header
	 * @return the string
	 */
	private static String disableClickableLinks(String defaultHeader) {
		if (defaultHeader == null) {
			return null;
		}
		return HTML_A_TAG.matcher(defaultHeader).replaceAll("<$1span>");
	}
}
