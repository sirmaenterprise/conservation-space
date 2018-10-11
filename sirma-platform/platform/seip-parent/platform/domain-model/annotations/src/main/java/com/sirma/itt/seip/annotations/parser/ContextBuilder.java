package com.sirma.itt.seip.annotations.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.github.jsonldjava.core.Context;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.utils.JsonUtils;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;

/**
 * Builder for {@link Context}s. The builder allows pluging other contexts from the one needed to be build. This can be
 * used for extending/added more context information before parsing json-ld input. The provided parsed context should
 * contain mapping of prefix definitions and field definitions at top level. If defined in file they could be loaded
 * using {@link JsonUtils} methods to load the information
 * <p>
 * Example for context:
 *
 * <pre>
 * {
	"emf": "http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#",
	"emf:modifiedBy": {
		"@type": "@id",
		"@id": "emf:modifiedBy"
	},
	"emf:reply": {
		"@type": "@id",
		"@id": "emf:reply",
		"@container": "@list"
	},
	"emf:modifiedOn": "emf:modifiedOn",
}
 * </pre>
 *
 * @author tdossev
 */
public class ContextBuilder {

	private Collection<ContextBuilderProvider> contextProviders = new ArrayList<>();
	private static final ContextBuilder INSTANCE = new ContextBuilder();

	/**
	 * Utility class constructor.
	 */
	private ContextBuilder() {
	}

	/**
	 * Returns a single instance
	 *
	 * @return a singleton instance
	 */
	public static ContextBuilder getInstance() {
		return INSTANCE;
	}

	/**
	 * Builds context for the given uri and options and appends the SEIP specific context to it.
	 *
	 * @param baseUri
	 *            the base uri of the context to read and parse
	 * @param options
	 *            the options options to use for context building
	 * @return the build context
	 */
	public Context build(Object baseUri, JsonLdOptions options) {
		try {
			List<Object> contexts = new LinkedList<>();
			contexts.add(baseUri);
			contextProviders.stream().map(provider -> provider.getSpecificContext()).forEach(contexts::add);
			return new Context(options).parse(contexts);
		} catch (JsonLdError e) {
			throw new EmfApplicationException("Could not parse context: " + baseUri, e);
		}
	}

	/**
	 * Registers context extension providers
	 *
	 * @param contextExtensionProvider
	 *            to register
	 */
	public void registerProvider(ContextBuilderProvider contextExtensionProvider) {
		contextProviders.add(contextExtensionProvider);
	}
}
