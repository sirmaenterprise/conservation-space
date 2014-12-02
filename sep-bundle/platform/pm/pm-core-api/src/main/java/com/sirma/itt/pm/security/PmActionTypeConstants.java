package com.sirma.itt.pm.security;

import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;

/**
 * List of action ids of the default operations
 * 
 * @author BBonev
 */
public interface PmActionTypeConstants extends ActionTypeConstants {

	String PROJECTINSTANCE = "projectinstance";
	String CREATE_PROJECT = "createProject";
	String MANAGE_RESOURCES = "manageResources";
	String MANAGE_RELATIONS = "manageRelations";
	String EDIT_SCHEDULE = "editSchedule";
}
