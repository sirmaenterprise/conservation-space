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
	public static final String ID = "id";
	/**
	 * Represents the attribute key for the definition label
	 */
	public static final String LABEL_ID = "labelId";
	public static final String LABEL = "label";
	public static final String TOOLTIP_ID = "tooltipId";
	public static final String TOOLTIP = "tooltip";
	public static final String NAME = "name";
	public static final String TYPE_OPTION = "typeOption";
	public static final String TYPE = "type";
	public static final String URI = "uri";
	public static final String VALUE = "value";
	public static final String RNC = "rnc";
	public static final String ORDER = "order";
	public static final String CODE_LIST = "codeList";
	public static final String DISPLAY_TYPE = "displayType";
	public static final String PREVIEW_EMPTY = "previewEmpty";
	public static final String MULTI_VALUED = "multiValued";
	public static final String MANDATORY = "mandatory";
	public static final String REGION_ID = "regionId";
	public static final String HEADER_TYPE = "headerType";
	public static final String PURPOSE = "purpose";
	public static final String CONFIRMATION_ID = "confirmationId";
	public static final String CONFIRMATION = "confirmation";
	public static final String ACTION_PATH = "actionPath";
	public static final String GROUP = "group";
	public static final String PARENT = "parent";
	public static final String PHASE = "phase";
	public static final String ASYNC = "async";
	public static final String PERSISTENT = "persistent";

	private DefinitionModelAttributes() {
		// Preventing instantiation.
	}
}
