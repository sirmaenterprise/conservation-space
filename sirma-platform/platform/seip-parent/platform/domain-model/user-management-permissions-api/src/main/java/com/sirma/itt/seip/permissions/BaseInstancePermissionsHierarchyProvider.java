package com.sirma.itt.seip.permissions;

import java.util.Optional;

import javax.inject.Inject;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.context.InstanceContextService;

/**
 * Extension for {@link InstancePermissionsHierarchyProvider} working with parent-child assoc. This is the default impl
 */
public abstract class BaseInstancePermissionsHierarchyProvider implements InstancePermissionsHierarchyProvider {

	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	@Inject
	private TypeConverter typeConverter;

	@Inject
	private InstanceContextService contextService;

	@Override
	public InstanceReference getPermissionInheritanceFrom(InstanceReference instance) {
		Optional<InstanceReference> parent = contextService.getContext(instance);
		if (parent.isPresent() && !isAllowedForPermissionSource(parent.get())) {
			// cannot inherit permissions from users
			return null;
		}
		return parent.orElse(null);
	}

	@Override
	public InstanceReference getLibrary(InstanceReference reference) {
		ClassInstance library = semanticDefinitionService.getClassInstance(reference.getId());
		if (library != null) {
			// if the current instance is library we are at the top of the resolving
			// so we are out of here
			return null;
		}
		// if the current instance is not library than get the library
		// we return a copy of the library so that the types for resolving match to object instance
		library = semanticDefinitionService.getClassInstance(reference.getType().getId().toString());
		if (library != null) {
			ObjectInstance libraryCopy = new ObjectInstance();
			// the library URIs are in full format when fetched from the cacheNo permissions to see this object!
			ShortUri uri = typeConverter.convert(ShortUri.class, library.getId());
			libraryCopy.setId(uri.toString());
			libraryCopy.setType(library);
			return libraryCopy.toReference();
		}
		return null;
	}

	@Override
	public boolean isAllowedForPermissionSource(InstanceReference parent) {
		if (parent == null || parent.getType() == null) {
			// root instance or invalid type
			return false;
		}
		return !parent.getType().is(ObjectTypes.USER) && !parent.getType().is(ObjectTypes.GROUP)
				&& !parent.getType().is("classinstance");
	}

	@Override
	public boolean isInstanceRoot(String root) {
		return false;
	}

}
