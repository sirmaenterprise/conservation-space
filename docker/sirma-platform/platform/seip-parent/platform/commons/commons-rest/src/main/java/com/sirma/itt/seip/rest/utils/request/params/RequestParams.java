package com.sirma.itt.seip.rest.utils.request.params;

import static com.sirma.itt.seip.rest.utils.request.params.RequestParamConverters.FIRST_ITEM;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParamConverters.QUERY_LIMIT_CONVERTER;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParamConverters.TO_BOOLEAN;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParamConverters.TO_INTEGER;
import static com.sirma.itt.seip.rest.utils.request.params.RequestParamConverters.TO_LIST;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.rest.utils.request.params.RequestParam.Type;

/**
 * Utility class containing param keys and {@link RequestParam} instances for these the keys.
 *
 * @author yasko
 */
public final class RequestParams {

	public static final String KEY_ID = "id";
	public static final String KEY_LIMIT = "limit";
	public static final String KEY_OFFSET = "offset";
	public static final String KEY_LANG = "lang";
	public static final String KEY_INSTANCE_ID = "instance-id";
	public static final String KEY_PLACEHOLDER = "placeholder";
	public static final String KEY_CONTEXT_ID = "context-id";
	public static final String KEY_PATH = "path";
	public static final String KEY_DOWNLOAD_PURPOSE = "purpose";
	public static final String KEY_DEFINITION_ID = "definition-id";
	public static final String KEY_PARENT_INSTANCE_ID = "parent-instance-id";
	public static final String KEY_OPERATION = "operation";
	public static final String KEY_PROPERTY_NAME = "propertyName";
	public static final String KEY_PROPERTIES = "properties";
	public static final String KEY_ALLOW_DELETED = "deleted";
	public static final String KEY_START = "start";
	public static final String KEY_END = "end";
	public static final String KEY_TENANT = "tenant";
	/**
	 * Query parameter that specifies when the current request should be ignored in statistics or not. If present the
	 * request will be excluded from the statistics collecting
	 */
	public static final String KEY_DISABLE_METRICS = "disableMetrics";

	public static final RequestParam<String> PATH_ID = new RequestParam<>(KEY_ID, Type.PATH, FIRST_ITEM);

	/** Limit search results. Default 25, -1 for unlimited (passed in as "all"). **/
	public static final RequestParam<Integer> QUERY_LIMIT = new RequestParam<>(KEY_LIMIT, Type.QUERY, QUERY_LIMIT_CONVERTER, 25);
	public static final RequestParam<Integer> QUERY_OFFSET = new RequestParam<>(KEY_OFFSET, Type.QUERY, TO_INTEGER, 0);
	public static final RequestParam<List<String>> QUERY_FIELDS = new RequestParam<>(KEY_OFFSET, Type.QUERY, TO_LIST,
			Arrays.asList("id", "definitionId", "domainClass"));
	public static final RequestParam<String> QUERY_LANG = new RequestParam<>(KEY_LANG, Type.QUERY, FIRST_ITEM);
	public static final RequestParam<String> QUERY_INSTANCE_ID = new RequestParam<>(KEY_INSTANCE_ID, Type.QUERY, FIRST_ITEM);
	public static final RequestParam<String> QUERY_PLACEHOLDER = new RequestParam<>(KEY_PLACEHOLDER, Type.QUERY, FIRST_ITEM);
	public static final RequestParam<String> QUERY_CONTEXT_ID = new RequestParam<>(KEY_CONTEXT_ID, Type.QUERY, FIRST_ITEM);
	public static final RequestParam<String> QUERY_OPERATION = new RequestParam<>(KEY_OPERATION, Type.QUERY, FIRST_ITEM);
	public static final RequestParam<List<String>> QUERY_PATH = new RequestParam<>(KEY_PATH, Type.QUERY, TO_LIST, Collections.emptyList());
	public static final RequestParam<List<String>> QUERY_IDS = new RequestParam<>(KEY_ID, Type.QUERY, TO_LIST, Collections.emptyList());
	public static final RequestParam<List<String>> PROPERTY_NAMES = new RequestParam<>(KEY_PROPERTIES, Type.QUERY,
			TO_LIST, Collections.emptyList());
	public static final RequestParam<Boolean> ALLOW_DELETED = new RequestParam<>(KEY_ALLOW_DELETED, Type.QUERY,
			TO_BOOLEAN, Boolean.FALSE);

	/** Gets download purpose from query parameter from the request. */
	public static final RequestParam<String> QUERY_DOWNLOAD_PURPOSE = new RequestParam<>(KEY_DOWNLOAD_PURPOSE, Type.QUERY, FIRST_ITEM);
	public static final RequestParam<String> QUERY_START = new RequestParam<>(KEY_START, Type.QUERY, FIRST_ITEM);
	public static final RequestParam<String> QUERY_END = new RequestParam<>(KEY_END, Type.QUERY, FIRST_ITEM);

	private RequestParams() {
		// utility
	}

}
