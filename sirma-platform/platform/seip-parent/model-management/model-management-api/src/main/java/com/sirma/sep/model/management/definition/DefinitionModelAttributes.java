package com.sirma.sep.model.management.definition;

/**
 * Holds the supported attributes for {@link com.sirma.sep.model.management.ModelDefinition} and {@link com.sirma.sep.model.management.ModelField}.
 *
 * @author Mihail Radkov
 */
public class DefinitionModelAttributes {

	/**
	 * Represents the attribute key for the definition identifier
	 */
	public static final String IDENTIFIER = "identifier";
	public static final String ABSTRACT = "abstract";
	public static final String RDF_TYPE = "rdfType";
	/**
	 * Represents the attribute key for the definition label
	 */
	public static final String LABEL = "label";
	public static final String TOOLTIP = "tooltip";
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String URI = "uri";
	public static final String VALUE = "value";
	public static final String ORDER = "order";
	public static final String CODE_LIST = "codeList";
	public static final String DISPLAY_TYPE = "displayType";
	public static final String PREVIEW_EMPTY = "previewEmpty";
	public static final String MULTI_VALUED = "multiValued";
	public static final String MANDATORY = "mandatory";

	private DefinitionModelAttributes() {
		// Preventing instantiation.
	}
}
