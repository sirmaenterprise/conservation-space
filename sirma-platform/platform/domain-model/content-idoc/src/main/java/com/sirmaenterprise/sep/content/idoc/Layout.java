package com.sirmaenterprise.sep.content.idoc;

/**
 * Represents IDOC Layouts. Provices layout columns and layout manager of IDOC content.
 * 
 * @author Hristo Lungov
 */
public interface Layout extends ContentNode {

	String LAYOUT_MANAGER_CLASS = "layoutmanager";
	String LAYOUT_COLUMN_CLASS = "layout-column-editable";

}
