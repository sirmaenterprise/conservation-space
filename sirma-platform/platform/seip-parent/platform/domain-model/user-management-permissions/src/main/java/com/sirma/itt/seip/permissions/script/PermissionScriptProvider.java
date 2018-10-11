package com.sirma.itt.seip.permissions.script;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.role.ResourceRole;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.script.GlobalBindingsExtension;
import com.sirma.itt.seip.script.ScriptInstance;

/**
 * Script provider for permissions service. It is used in the definitions with the "permission" call in the scripts. For
 * now its use is to get the permissions of a specific instance.
 *
 * @author siliev
 *
 */
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 69)
public class PermissionScriptProvider implements GlobalBindingsExtension {

	@Inject
	private PermissionService service;
	@Inject
	private ResourceService resourceService;


	@Override
	public Map<String, Object> getBindings() {
		return Collections.singletonMap("permissions", this);
	}

	@Override
	public Collection<String> getScripts() {
		return Collections.emptyList();
	}

	/**
	 * Finds the user role for a specific node.
	 *
	 * @param node
	 *            the node we want to evaluate
	 * @param userId
	 *            the user id
	 * @return the user role or null if nothing could not be found
	 */
	public ResourceRole getUserRole(ScriptInstance node, String userId) {
		return getUserRole(node.toReference(), userId);
	}

	/**
	 * Finds the user role for a specific node.
	 *
	 * @param intanceId
	 *            the instance we want to evaluate
	 * @param userId
	 *            the user id
	 * @return the user role or null if nothing could not be found
	 */
	public ResourceRole getUserRole(String intanceId, String userId) {
		Instance loadedInstane = resourceService.loadByDbId(intanceId);
		if (loadedInstane != null) {
			return getUserRole(loadedInstane.toReference(), userId);
		}
		return null;
	}


	/**
	 * Finds the user role for a specific node.
	 *
	 * @param reference
	 *            the reference we want to evaluate
	 * @param userId
	 *            the user id
	 * @return the user role or null if nothing could not be found
	 */
	private ResourceRole getUserRole(InstanceReference reference, String userId) {
		return service.getPermissionAssignment(reference, userId);
	}
}
