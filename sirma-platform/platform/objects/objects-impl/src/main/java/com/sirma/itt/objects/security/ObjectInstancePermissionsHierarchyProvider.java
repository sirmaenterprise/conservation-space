package com.sirma.itt.objects.security;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.permissions.BaseInstancePermissionsHierarchyProvider;
import com.sirma.itt.seip.permissions.InstancePermissionsHierarchyProvider;

/**
 * Extension of {@link InstancePermissionsHierarchyProvider} to work with {@link ObjectInstance} - as parent provides
 * its class
 */
@Singleton
public class ObjectInstancePermissionsHierarchyProvider extends BaseInstancePermissionsHierarchyProvider {

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Override
	public InstanceReference getPermissionInheritanceFrom(InstanceReference reference) {
		if (reference.getType().is("classinstance")) {
			// class instance does not inherit
			return null;
		}
		// the only revisions has an object as their parent so the revision inherits the
		// permission from the parent
		return getNextValidParent(reference);
	}

	private InstanceReference getNextValidParent(InstanceReference reference) {
		InstanceReference lastValid = reference.getParent();
		while (lastValid != null && !isAllowedForPermissionSource(lastValid)) {
			lastValid = lastValid.getParent();
		}
		return lastValid;
	}

	@Override
	public boolean isInstanceRoot(String instanceId) {
		// if the instance is library this will return non null item
		return semanticDefinitionService.getClassInstance(instanceId) != null;
	}
}
