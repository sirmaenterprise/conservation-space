package com.sirma.sep.model.management;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains the supported {@link ModelHeader} types.
 *
 * @author Mihail Radkov
 */
public class ModelHeaderType {

	public static final String DEFAULT = "default_header";
	public static final String COMPACT = "compact_header";
	public static final String BREADCRUMB = "breadcrumb_header";
	public static final String TOOLTIP = "tooltip_header";

	/**
	 * Contains all {@link ModelHeader} types. Useful for {@link java.util.stream.Stream} operations.
	 */
	public static final Set<String> HEADERS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(DEFAULT, COMPACT, BREADCRUMB, TOOLTIP)));

	private ModelHeaderType() {
		// Utility class for constants, should not be instantiated
	}
}
