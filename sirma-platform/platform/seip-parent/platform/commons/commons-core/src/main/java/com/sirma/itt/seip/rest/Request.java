package com.sirma.itt.seip.rest;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper object for {@link MultivaluedMap} instance to add more usable methods for reading the REST requests.
 *
 * @author BBonev
 */
public class Request implements Serializable {
	private static final long serialVersionUID = -6743357516273312640L;
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private Map<String, List<String>> params;

	/**
	 * Instantiates a new request.
	 */
	public Request() {
		// default constructor
	}

	/**
	 * Instantiates a new request.
	 *
	 * @param request
	 *            the request
	 */
	public Request(Map<String, List<String>> request) {
		params = request;
	}

	/**
	 * Getter method for request.
	 *
	 * @return the request
	 */
	public Map<String, List<String>> getRequest() {
		return params;
	}

	/**
	 * Setter method for request.
	 *
	 * @param request
	 *            the request to set
	 */
	public void setRequest(Map<String, List<String>> request) {
		params = request;
	}

	/**
	 * Gets non null list from the request map.
	 *
	 * @param key
	 *            the key
	 * @return the list
	 */
	public List<String> get(String key) {
		if (getRequest() == null) {
			return Collections.emptyList();
		}
		List<String> list = getRequest().get(key);
		if (list == null) {
			list = Collections.emptyList();
		}
		return list;
	}

	/**
	 * Gets the first value for the given key
	 *
	 * @param key
	 *            the key
	 * @return the first
	 */
	public String getFirst(String key) {
		return getOrDefault(key, null);
	}

	/**
	 * Gets the first value or the default value if the value is <code>null</code>.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the value or the default value
	 */
	public String getOrDefault(String key, String defaultValue) {
		if (getRequest() == null) {
			return defaultValue;
		}
		List<?> values = getRequest().get(key);
		if (values != null && !values.isEmpty()) {
			return (String) values.get(0);
		}
		return defaultValue;
	}

	/**
	 * Gets the first element as integer or null if not.
	 *
	 * @param key
	 *            the key
	 * @return the first integer
	 */
	public Integer getFirstInteger(String key) {
		return getIntegerOrDefault(key, null);
	}

	/**
	 * Gets the first element as integer or the default value if not passed.
	 *
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value
	 * @return the first integer or the given default value
	 */
	public Integer getIntegerOrDefault(String key, Integer defaultValue) {
		return getFirstValueAs(key, defaultValue, Integer::valueOf);
	}

	/**
	 * Gets integer values.
	 *
	 * @param key
	 *            the key
	 * @return {@link List} with all integer values
	 */
	public List<Integer> getIntegers(String key) {
		return getValuesAs(key, Integer::valueOf);
	}

	/**
	 * Gets the first element as boolean.
	 *
	 * @param key
	 *            the key
	 * @return the first values as a boolean
	 */
	public boolean getFirstBoolean(String key) {
		return getFirstValueAs(key, Boolean.FALSE, Boolean::valueOf).booleanValue();
	}

	/**
	 * Gets the first value converted using the given converter instance. Note that the converter will not be called if
	 * the value is blank (not present or just empty string or white space). The converter will be called with the
	 * trimmed value.
	 *
	 * @param <E>
	 *            the element type
	 * @param key
	 *            the key
	 * @param defaultValue
	 *            the default value to return if the key is not present or fail to convert
	 * @param converter
	 *            the converter to use
	 * @return the first value converted using the converter or the default value
	 */
	protected <E> E getFirstValueAs(String key, E defaultValue, Function<String, E> converter) {
		String stringValue = getFirst(key);
		String value = StringUtils.trimToNull(stringValue);
		try {
			if (value != null) {
				return converter.apply(value);
			}
		} catch (RuntimeException re) {
			LOGGER.error("Parameter {}={} could not be converted", key, value, re);
		}
		return defaultValue;
	}

	/**
	 * Gets all values converted using the given converter function. Values that fail to convert will be skipped from
	 * the result. Note that the converter will not be called if the value is blank (not present or just empty string or
	 * white space). The converter will be called with the trimmed value.
	 *
	 * @param <E>
	 *            the element type
	 * @param key
	 *            the key
	 * @param converter
	 *            the converter to use
	 * @return the values converted to the type returned by the converter function
	 */
	protected <E> List<E> getValuesAs(String key, Function<String, E> converter) {
		List<String> list = get(key);
		List<E> result = new ArrayList<>(list.size());
		for (String stringValue : list) {
			String value = StringUtils.trimToNull(stringValue);
			try {
				if (value != null) {
					result.add(converter.apply(value));
				}
			} catch (RuntimeException re) {
				LOGGER.error("Parameter {}={} could not be converted.", key, value, re);
			}
		}
		return result;
	}
}
