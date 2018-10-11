package com.sirma.itt.seip.rest.utils.request.params;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.List;
import java.util.function.BiFunction;

import com.sirma.itt.seip.rest.utils.request.RequestInfo;

/**
 * Utility class for common request param converters.
 *
 * @author yasko
 */
public final class RequestParamConverters {

	/**
	 * Special request parameter value that represents all requested items
	 */
	public static final String ALL_RESULTS = "all";

	public static final BiFunction<RequestInfo, List<String>, String> FIRST_ITEM = (request, list) -> isEmpty(list)
			? null : list.get(0);

	public static final BiFunction<RequestInfo, List<String>, Integer> TO_INTEGER = (request, list) -> isEmpty(list)
			? null : Integer.valueOf(list.get(0));

	public static final BiFunction<RequestInfo, List<String>, Boolean> TO_BOOLEAN = (request, list) -> isEmpty(list)
			? null : Boolean.valueOf(list.get(0));

	public static final BiFunction<RequestInfo, List<String>, List<String>> TO_LIST = (request, list) -> list;

	public static final BiFunction<RequestInfo, List<String>, Integer> QUERY_LIMIT_CONVERTER = (request, list) -> {
		if (isEmpty(list)) {
			return null;
		}

		String string = list.get(0);
		if (nullSafeEquals(ALL_RESULTS, string, true)) {
			return -1;
		}
		return TO_INTEGER.apply(request, list);
	};

	private RequestParamConverters() {
		// utility
	}
}
