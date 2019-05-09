package com.sirma.sep.model.management;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds the supported {@link ModelAttribute} types which could be returned from {@link ModelAttribute#getType()}.
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
	public static final String TYPE_OPTION = "typeOption";
	public static final String TYPE = "type";
	public static final String CODE_LIST = "codeList";
	public static final String OPTION = "option";

	/**
	 * Collection of all multi language attributes that are represented by a map.
	 */
	public static final Set<String> LABEL_ATTRIBUTES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(LABEL, MULTI_LANG_STRING)));

	private ModelAttributeType() {
		// Preventing instantiation.
	}

}
