/**
 *
 */
package com.sirma.itt.cmf.alfresco4;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.instance.model.DMSInstance;
import com.sirma.itt.emf.search.model.SearchArguments;

/**
 * The Class AlfrescoUtils contains methods that helps with common logic to provide alfresco
 * adapter.
 *
 * @author bbanchev
 */
public class AlfrescoUtils {

	/** the logger. */
	private static final Logger LOGGER = Logger.getLogger(AlfrescoUtils.class);
	/** example 2012-09-26T00:00:00. */
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	/** The Constant END. */
	public static final String SEARCH_END = " ) ";
	/** The Constant START. */
	public static final String SEARCH_START = " ( ";
	/** The Constant DATE_SUFFIX. */
	public static final String DATE_FROM_SUFFIX = "T00:00:00";
	/** The Constant OR. */
	public static final String SEARCH_OR = " OR ";
	/** The Constant OR. */
	public static final String SEARCH_AND = " AND ";

	/** The Constant END_VALUE. */
	public static final String SEARCH_END_VALUE = "\"";

	/** The Constant START_VALUE. */
	public static final String SEARCH_START_VALUE = ":\"";

	/** what is the query mode. */
	public static enum QueryMode {

		/** The db search. */
		DB_SEARCH,
		/** The lucene search. */
		LUCENE_SEARCH;
	}

	/**
	 * Json object to map.
	 *
	 * @param <V>
	 *            the value type
	 * @param jsonObject
	 *            the json object
	 * @return the map
	 * @throws JSONException
	 *             the jSON exception
	 */
	@SuppressWarnings("unchecked")
	public static final <V extends Serializable> Map<String, V> jsonObjectToMap(
			JSONObject jsonObject) throws JSONException {
		Map<String, V> result = new HashMap<String, V>(jsonObject.length());
		Iterator<String> keys = jsonObject.keys();
		while (keys.hasNext()) {
			String object = keys.next();
			if (!result.containsKey(object)) {
				Object value = jsonObject.get(object);
				if (value instanceof Serializable) {
					result.put(object, (V) value);
				} else if (value instanceof JSONArray) {
					result.put(object, (V) toList((JSONArray) value));
				}
				// else {
				// LOGGER.warn("SKIPPED: due to not serializable object: " +
				// value);
				// }
			} else {
				LOGGER.warn("SKIPPED during convert as duplicate: " + object);
			}
		}
		return result;
	}

	/**
	 * Converts json array to {@link ArrayList}
	 *
	 * @param <T>
	 *            the element type
	 * @param value
	 *            is the array
	 * @return array list of elements
	 * @throws JSONException
	 *             for conversion error
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Serializable> T toList(JSONArray value) throws JSONException {
		JSONArray arr = (JSONArray) value;
		Collection<Object> collection = new ArrayList<Object>(arr.length());
		for (int i = 0; i < arr.length(); i++) {
			collection.add(arr.get(i));
		}
		return (T) collection;
	}

	/**
	 * Populate paging from json alfresco result to search args.
	 *
	 * @param <E>
	 *            the element type
	 * @param result
	 *            the result
	 * @param args
	 *            the args
	 * @throws JSONException
	 *             the jSON exception
	 */
	public static final <E> void populatePaging(JSONObject result, SearchArguments<E> args)
			throws JSONException {
		if (result.has(AlfrescoCommunicationConstants.KEY_PAGING)) {
			JSONObject paging = result.getJSONObject(AlfrescoCommunicationConstants.KEY_PAGING);
			args.setTotalItems(paging.getInt("totalItems"));
		}
	}

	/**
	 * Synchronized format of date to string using {@link #DATE_FORMAT}
	 *
	 * @param date
	 *            is the date to format
	 * @return the formated date with {@link #DATE_FORMAT}
	 */
	public static String formatDate(Date date) {
		synchronized (DATE_FORMAT) {
			return DATE_FORMAT.format(date);
		}
	}

	/**
	 * Internal method to validate dmsinstance. Instance is checked if is null or the
	 * {@link DMSInstance#getDmsId()} returns null
	 *
	 * @param instance
	 *            is the instance to check
	 * @return true if this is valid dms instance
	 * @throws DMSException
	 *             if this is not a valid dms instance
	 */
	public static boolean validateExistingDMSInstance(final DMSInstance instance)
			throws DMSException {
		if ((instance == null) || (instance.getDmsId() == null)) {
			StringBuilder exceptionMsg = new StringBuilder();
			exceptionMsg.append("Invalid '");
			exceptionMsg.append(instance != null ? instance : "instance");
			exceptionMsg.append("' is provided: ");
			exceptionMsg.append((instance == null ? "null"
					: (instance.getDmsId() == null ? " missing DMS ID" : "unknown")));
			throw new DMSException(exceptionMsg.toString());
		}
		return true;
	}
}
