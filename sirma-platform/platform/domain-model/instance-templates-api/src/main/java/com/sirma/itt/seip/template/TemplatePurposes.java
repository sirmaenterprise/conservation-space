package com.sirma.itt.seip.template;

/**
 * The possible purposes for a template. They might be creatable or uploadable. Will be stored and retrieved from the
 * relational database.
 *
 * @author Vilizar Tsonev
 */
public interface TemplatePurposes {

	String UPLOADABLE = "uploadable";

	String CREATABLE = "creatable";
}
