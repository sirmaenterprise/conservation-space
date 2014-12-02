package com.sirma.itt.objects.security;

import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;

/**
 * List of action ids of the default object operations.
 * 
 * @author svelikov
 */
public interface ObjectActionTypeConstants extends ActionTypeConstants {

	String OBJECT_MOVE_SAME_CASE = "objectMoveSameCase";
	String CREATE_OBJECT = "createObject";
	String ATTACH_OBJECT = "attachObject";
	String DETACH_OBJECT = "detachObject";
	String ADD_THUMBNAIL = "addThumbnail";
	String ADD_PRIMARY_IMAGE = "addPrimaryImage";
	String CREATE_OBJECTS_SECTION = "createObjectsSection";
	String SAVE_AS_TEMPLATE = "saveObjectAsTemplate";
	String UPLOAD_IN_OBJECT = "uploadInObject";
	String CREATE_FILTER = "createFilter";
	String UPDATE_FILTER = "updateFilter";
}
