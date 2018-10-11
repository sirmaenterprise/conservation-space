package com.sirma.itt.seip.annotations.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.github.jsonldjava.core.Context;

/**
 * Context store for parsed JSON-LD contexts. Once fetched that are stored in memory so they can be reused between
 * requests
 *
 * @author BBonev
 */
public class ContextStore {
	private static final Map<String, Context> CONTEXT_CACHE = new HashMap<>();

	private static final Map<String, String> LOCAL_CONTEXTS = new HashMap<>();

	static {
		// copy of some of the known contexts
		LOCAL_CONTEXTS.put("http://iiif.io/api/presentation/1/context.json", "annotations/iiif-context-1.json");
		LOCAL_CONTEXTS.put("http://iiif.io/api/presentation/2/context.json", "annotations/iiif-context-2.json");
	}

	/**
	 * Hides constructor for utility class
	 */
	private ContextStore() {
		// nothing to do
	}

	/**
	 * Gets stored context if not new one is created using the given address and options.
	 *
	 * @param contextUri
	 *            the context URI to resolve. It must be URI that can be read and parsed to context instance.
	 * @param builder
	 *            the builder to use to create new context if one identified by the given uri does not exists.
	 * @return the context or <code>null</code> if the context could not be read or is of invalid format
	 */
	public static Context get(String contextUri, Function<Object, Context> builder) {
		return CONTEXT_CACHE.computeIfAbsent(getLocalOrRemotePath(contextUri), builder);
	}

	private static String getLocalOrRemotePath(String contextUri) {
		String local = LOCAL_CONTEXTS.get(contextUri);
		if (local != null) {
			return ContextStore.class.getClassLoader().getResource(local).toString();
		}
		return contextUri;
	}
}
