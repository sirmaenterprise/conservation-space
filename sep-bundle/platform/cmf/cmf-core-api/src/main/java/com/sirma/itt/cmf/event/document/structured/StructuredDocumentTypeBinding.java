package com.sirma.itt.cmf.event.document.structured;

import javax.enterprise.util.AnnotationLiteral;

/**
 * Selector for the {@link StructuredDocumentType} annotation.
 * 
 * @author svelikov
 */
public class StructuredDocumentTypeBinding extends AnnotationLiteral<StructuredDocumentType>
		implements StructuredDocumentType {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 3178545446195664815L;

	/**
	 * Document type.
	 */
	private final String type;

	/**
	 * Instantiates a new structured document type binding.
	 * 
	 * @param type
	 *            The document type.
	 */
	public StructuredDocumentTypeBinding(String type) {
		this.type = type;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String type() {
		return this.type;
	}

}
