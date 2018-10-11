package com.sirma.itt.objects.security;

import com.sirma.itt.seip.domain.security.ActionTypeConstants;

/**
 * List of action ids of the default object operations.
 *
 * @author svelikov
 */
public interface ObjectActionTypeConstants extends ActionTypeConstants {

	String CREATE_OBJECT = "createObject";
	String CREATE_FILTER = "createFilter";
	String UPDATE_FILTER = "updateFilter";
}
