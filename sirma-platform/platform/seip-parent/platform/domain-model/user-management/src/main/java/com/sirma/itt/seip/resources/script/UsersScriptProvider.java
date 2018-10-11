package com.sirma.itt.seip.resources.script;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.resources.ResourceType;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Script provider for users functionality like checking if value resolves to current user or user is part of a group
 * etc.
 *
 * @author BBonev
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 54)
public class UsersScriptProvider implements GlobalBindingsExtension {

	private static final String SCRIPT_LOCATION = "users.js";

	@Inject
	private ResourceService resourceService;
	@Inject
	private SecurityContext securityContext;

	/**
	 * Gets the bindings.
	 *
	 * @return the bindings
	 */
	@Override
	public Map<String, Object> getBindings() {
		return Collections.singletonMap("users", this);
	}

	/**
	 * Gets the scripts.
	 *
	 * @return the scripts
	 */
	@Override
	public Collection<String> getScripts() {
		return ResourceLoadUtil.loadResources(getClass(), SCRIPT_LOCATION);
	}

	/**
	 * Current user instance
	 *
	 * @return the user
	 */
	public User current() {
		return securityContext.getAuthenticated();
	}

	/**
	 * Checks if the two arguments match match the same user
	 *
	 * @param user1
	 *            the user1
	 * @param user2
	 *            the user2
	 * @return true, if users matches
	 */
	public boolean areEqual(Object user1, Object user2) {
		return resourceService.areEqual(user1, user2);
	}

	/**
	 * This executes {@link com.sirma.itt.seip.resources.ResourceService#getResource(String)}
	 *
	 * @param userId
	 *            the userId we want to resolve to user reference
	 * @return the user object or null if not found
	 */
	public User getUserById(String userId) {
		return resourceService.getResource(userId);
	}

	/**
	 * Checks if the given user is member of the given group.
	 *
	 * @param user
	 *            instance of user to check about membership
	 * @param group
	 *            id of the group to check the given user about membership
	 * @return true if the given user is member of the given group
	 */
	public boolean isMemberOf(Object user, Object group) {
		if (user != null && group != null) {
			Resource foundUser = resourceService.findResource((Serializable) user);
			Resource foundGroup = resourceService.findResource((Serializable) group);

			if (foundUser != null && foundGroup != null && foundGroup.getType().equals(ResourceType.GROUP)) {
				return resourceService.getContainedResourceIdentifiers(foundGroup).contains(foundUser.getId());
			}
		}

		return false;
	}

}
