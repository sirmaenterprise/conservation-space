package com.sirma.itt.seip.annotations.parser;

import java.io.IOException;
import java.util.Map;

import com.github.jsonldjava.utils.JsonUtils;

/**
 * Default context extension that provides SEIP custom properties for Annotations.
 * This class is instantiated at startup  
 * @see {@link com.sirma.itt.seip.annotations.parser.CDIContextBuilderExtension} 
 *
 * @author BBonev
 */
@SuppressWarnings("unchecked")
public class DefaultSeipContextExtension implements ContextBuilderProvider {

	/** Location of the base context */
	public static final String BASE_CONTEXT = "annotations/seip-context.json";
	private static final Map<String, Object> PARSED_BASE_CONTEXT;

	static {
		try {
			PARSED_BASE_CONTEXT = (Map<String, Object>) JsonUtils
					.fromInputStream(DefaultSeipContextExtension.class.getClassLoader().getResourceAsStream(BASE_CONTEXT));
		} catch (IOException e) {
			throw new IllegalStateException("Could not read base context from: " + BASE_CONTEXT, e);
		}
	}

	@Override
	public Map<String, Object> getSpecificContext() {
		return PARSED_BASE_CONTEXT;
	}

}
