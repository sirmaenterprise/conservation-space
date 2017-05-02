package com.sirma.itt.seip.script.extensions;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Provider extension to add security.
 *
 * @author S.Djulgerova
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 27)
public class SecurityScriptProvider implements GlobalBindingsExtension {

	@Inject
	protected SecurityContext securityContext;

	@Override
	public Map<String, Object> getBindings() {
		return Collections.<String, Object> singletonMap("security", this);
	}

	@Override
	public Collection<String> getScripts() {
		return Collections.emptyList();
	}

	/**
	 * Gets the current logged user.
	 *
	 * @return the current logged user
	 */
	public User getCurrentLoggedUser() {
		return securityContext.getAuthenticated();
	}
}
