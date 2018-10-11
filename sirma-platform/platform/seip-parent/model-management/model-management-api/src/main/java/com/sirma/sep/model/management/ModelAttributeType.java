package com.sirma.sep.model.management;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds the supported {@link ModelAttribute} types which should be used to set {@link ModelAttribute#setType(String)}.
 *
 * @author Mihail Radkov
 */
public class ModelAttributeType {

	public static final String STRING = "string";
	public static final String BOOLEAN = "boolean";
	public static final String LABEL = "label";
	public static final String IDENTIFIER = "identifier";
	public static final String MULTI_LANG_STRING = "multiLangString";
	public static final String INTEGER = "integer";
	public static final String URI = "uri";
	public static final String SEMANTIC_TYPE = "semanticType";
	public static final String TYPE = "type";
	public static final String CODE_LIST = "codeList";
	public static final String DISPLAY_TYPE = "displayType";

	/**
	 * Collection of all attributes that should be represented by a map
	 */
	public static final Set<String> MAP_ATTRIBUTES = new HashSet<>();

	static {
		MAP_ATTRIBUTES.add(LABEL);
		MAP_ATTRIBUTES.add(MULTI_LANG_STRING);
	}

	private ModelAttributeType() {
		// Preventing instantiation.
	}

}
