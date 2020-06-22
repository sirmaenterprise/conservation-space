package com.sirma.itt.seip.resources;

import com.sirma.itt.seip.domain.instance.DefaultProperties;

/**
 * Class holding generic resource property names
 *
 * @author BBonev
 */
public class ResourceProperties implements DefaultProperties {

	public static final String GROUP_PREFIX = "GROUP_";

	public static final String FIRST_NAME = "firstName";

	public static final String LAST_NAME = "lastName";

	public static final String USER_ID = "userId";

	public static final String GROUP_ID = "groupId";

	public static final String EMAIL = "email";

	public static final String LANGUAGE = "language";

	public static final String TIMEZONE = "timezone";

	public static final String AVATAR = "avatar";

	public static final String JOB_TITLE = "jobtitle";

	public static final String INITIALS = "initials";

	public static final String ROLE = "role";

	public static final String ENABLED = "enabled";

	public static final String HAS_MEMBER = "hasMember";

	public static final String IS_MEMBER_OF = "isMemberOf";

	private ResourceProperties() {
		// constants class
	}

}
