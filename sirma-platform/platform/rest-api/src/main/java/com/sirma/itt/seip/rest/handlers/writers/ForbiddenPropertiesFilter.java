package com.sirma.itt.seip.rest.handlers.writers;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.DEFAULT_HEADERS;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.THUMBNAIL_IMAGE;
import static com.sirma.itt.seip.util.EqualsHelper.getOrDefault;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sirma.itt.seip.domain.definition.DefinitionModel;

/**
 * Properties filter that filter out properties that should not be send to the client in the properties section. The
 * filter could be chained with other builder. The property name should pass both filters in order to be approved
 *
 * @author BBonev
 */
public class ForbiddenPropertiesFilter implements PropertiesFilterBuilder {

	/** Singleton instance that filter out the forbidden properties */
	public static final PropertiesFilterBuilder INSTANCE = new ForbiddenPropertiesFilter();

	/**
	 * Matches properties that are bind to key which start and end with '$'. This keys are used to show that the
	 * property is actually temporary, used only for internal operations.
	 */
	private static final Predicate<String> TEMP_PROPERTIES_FILTER = p -> p.startsWith("$") && p.endsWith("$");

	/** The downstream filter builder if needed. */
	private final PropertiesFilterBuilder downstream;

	// we have specific section for headers and we should removed them from here
	// it is filtered in PropertiesConverter, but just in case it start to handle it
	// content property should not be send to the user. If not defined in the definition the content is in the format
	// emf:content and it's returned to the user and increases the response size
	private static final Predicate<String> SKIPPED_PROPERTIES_FILTER = PropertiesFilterBuilder.onlyProperties(Stream
			.concat(DEFAULT_HEADERS.stream(), Stream.of(THUMBNAIL_IMAGE, CONTENT, "emf:" + CONTENT, "emf:viewContent"))
				.collect(Collectors.toList())).negate();

	/**
	 * Instantiates a new forbidden properties filter.
	 */
	public ForbiddenPropertiesFilter() {
		this(null);
	}

	/**
	 * Instantiates a new forbidden properties filter.
	 *
	 * @param downstream
	 *            the downstream filter builder to use
	 */
	public ForbiddenPropertiesFilter(PropertiesFilterBuilder downstream) {
		this.downstream = getOrDefault(downstream, PropertiesFilterBuilder.MATCH_ALL);
	}

	@Override
	public Predicate<String> buildFilter(DefinitionModel model) {
		return SKIPPED_PROPERTIES_FILTER.and(TEMP_PROPERTIES_FILTER.negate()).and(downstream.buildFilter(model));
	}

}
