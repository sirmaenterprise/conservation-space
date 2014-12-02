package com.sirma.itt.pm.schedule.security;

import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;

/**
 * List of actions used in resource allocation.
 */
public interface ResourceAllocationActionTypeConstants extends ActionTypeConstants {

	/** The resource allocation all users. */
	String RESOURCE_ALLOCATION_ALL_USERS = "resourceAllocationAllUsers";

	/** The resource allocation selected users. */
	String RESOURCE_ALLOCATION_SELECTED_USERS = "resourceAllocationSelectedUsers";
}
