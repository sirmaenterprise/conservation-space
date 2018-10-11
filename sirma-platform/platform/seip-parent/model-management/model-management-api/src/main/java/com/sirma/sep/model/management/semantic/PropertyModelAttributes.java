package com.sirma.sep.model.management.semantic;

import org.openrdf.model.vocabulary.RDFS;

/**
 * Holds the supported attributes for {@link com.sirma.sep.model.management.ModelProperty}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/07/2018
 */
public class PropertyModelAttributes {

	public static final String LABEL = RDFS.LABEL.toString();

	private PropertyModelAttributes() {
		// prevent instantiation
	}
}
