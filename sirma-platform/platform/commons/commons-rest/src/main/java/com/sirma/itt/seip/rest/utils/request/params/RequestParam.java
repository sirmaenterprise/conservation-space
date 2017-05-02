package com.sirma.itt.seip.rest.utils.request.params;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Common class for request parameters.
 * @author yasko
 *
 * @param <T> Type of the converted parameter.
 */
public final class RequestParam<T> {

	/** Request parameter type **/
	public enum Type {
		PATH, QUERY, HEADER
	}

	/** param key as passed in the request **/
	public final String key;

	/** param type - where it's provided **/
	public final Type type;

	/** param converter to java type **/
	public final BiFunction<RequestInfo, List<String>, T> converter;

	/** default value **/
	public final T defaultValue;

	/**
	 * Constructor.
	 * @param key Parameter key as passed in the request.
	 * @param type Parameter type (one of {@link Type}).
	 * @param converter Parameter converter to java class.
	 */
	public RequestParam(String key, Type type, BiFunction<RequestInfo, List<String>, T> converter) {
		this(key, type, converter, null);
	}

	/**
	 * Constructor.
	 * @param key Parameter key as passed in the request.
	 * @param type Parameter type (one of {@link Type}).
	 * @param converter Parameter converter to java class.
	 * @param defaultValue Parameter default value.
	 */
	public RequestParam(String key, Type type, BiFunction<RequestInfo, List<String>, T> converter, T defaultValue) {
		this.key = key;
		this.type = type;
		this.converter = converter;
		this.defaultValue = defaultValue;
	}

	/**
	 * Getter for the converted value of this request parameter.
	 *
	 * @param request
	 *            {@link RequestInfo} instance from which to retrieve the value.
	 * @return If the request parameter is not provided in the request the
	 *         parameter's default value is returned. Otherwise the converter
	 *         for this parameter is called with the provided value.
	 */
	public T get(RequestInfo request) {
		List<String> value = getRawValue(request, key, type);
		T convert = converter.apply(request, value);
		if (convert == null) {
			return defaultValue;
		}
		return convert;
	}

	private static List<String> getRawValue(RequestInfo request, String key, Type type) {
		if (type == Type.HEADER) {
			return request.getHeaders().getRequestHeader(key);
		}

		if (type == Type.PATH) {
			return request.getUriInfo().getPathParameters().get(key);
		}

		if (type == Type.QUERY) {
			return request.getUriInfo().getQueryParameters().get(key);
		}
		return Collections.emptyList();
	}
}
