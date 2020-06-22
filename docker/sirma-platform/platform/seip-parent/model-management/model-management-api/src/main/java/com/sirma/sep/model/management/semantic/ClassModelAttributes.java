package com.sirma.sep.model.management.semantic;

import org.openrdf.model.vocabulary.DCTERMS;

/**
 * Holds the supported attributes for {@link com.sirma.sep.model.management.ModelClass}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 12/07/2018
 */
public class ClassModelAttributes {

	public static final String LABEL = DCTERMS.TITLE.toString();

	private ClassModelAttributes() {
		// prevent instantiation
	}
}
