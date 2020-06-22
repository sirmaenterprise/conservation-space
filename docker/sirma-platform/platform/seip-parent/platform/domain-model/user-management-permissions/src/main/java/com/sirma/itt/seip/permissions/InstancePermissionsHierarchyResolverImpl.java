package com.sirma.itt.seip.permissions;

import java.lang.invoke.MethodHandles;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypes;

/**
 * Default implementation of the {@link InstancePermissionsHierarchyResolver} that uses the plugin API to collect all
 * providers and run them.
 *
 * @author BBonev
 */
@ApplicationScoped
public class InstancePermissionsHierarchyResolverImpl implements InstancePermissionsHierarchyResolver {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private InstancePermissionsHierarchyProvider hierarchyProvider;

	@Inject
	private InstanceTypes instanceTypes;

	@Override
	public InstanceReference getPermissionInheritanceFrom(InstanceReference reference) {
		loadRequiredReferenceData(reference);
		return hierarchyProvider.getPermissionInheritanceFrom(reference);
	}

	@Override
	public boolean isInstanceRoot(String reference) {
		return hierarchyProvider.isInstanceRoot(reference);
	}

	@Override
	public InstanceReference getLibrary(InstanceReference reference) {
		loadRequiredReferenceData(reference);
		return hierarchyProvider.getLibrary(reference);
	}

	@Override
	public boolean isAllowedForPermissionSource(InstanceReference reference) {
		loadRequiredReferenceData(reference);
		return hierarchyProvider.isAllowedForPermissionSource(reference);
	}

	private void loadRequiredReferenceData(InstanceReference reference) {
		// if type is missing load it
		if (reference.getType() == null) {
			LOGGER.trace("Got reference without loaded type. Loading it..");
			instanceTypes.from(reference);
		}
	}

}
