package com.sirma.itt.objects.domain;

import static com.sirma.itt.seip.collections.CollectionUtils.isEmpty;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.definition.AllowedAuthorityDefinitionsExtension;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.library.LibraryProvider;
import com.sirma.itt.seip.plugin.Extension;

/**
 * The ObjectAllowedUserDefinitionsExtension filters only the object definitions that current user has access to (or to
 * its libraries)
 */
@Extension(target = AllowedAuthorityDefinitionsExtension.TARGET_NAME, order = 90)
public class ObjectAllowedUserDefinitionsExtension implements AllowedAuthorityDefinitionsExtension {
	@Inject
	private SemanticDefinitionService semanticDefinitionService;
	@Inject
	private LibraryProvider libraryProvider;

	@Override
	public <D extends DefinitionModel> boolean isSupported(List<D> model) {
		if (isEmpty(model)) {
			return false;
		}
		return true;
	}

	@Override
	public <D extends DefinitionModel> List<D> getAllowedDefinitions(List<D> models) {
		List<D> result = new LinkedList<>();
		List<ClassInstance> allowedLibraries = libraryProvider.getAllowedLibraries(LibraryProvider.OBJECT_LIBRARY,
				ActionTypeConstants.CREATE);
		Set<ClassInstance> libraries = new HashSet<>(allowedLibraries);
		for (D definition : models) {
			// optimization for this is when we have multiple definitions of the same class to store the allowed classes
			// for easy computation
			if (isAllowedLibrary(definition, libraries)) {
				result.add(definition);
			}
		}
		return result;
	}

	private boolean isAllowedLibrary(DefinitionModel definition, Set<ClassInstance> allowedLibraries) {
		return definition
				.getField(DefaultProperties.SEMANTIC_TYPE)
					.map(PropertyDefinition::getDefaultValue)
					.filter(StringUtils::isNotNullOrEmpty)
					.map(semanticDefinitionService::getClassInstance)
					.filter(allowedLibraries::contains)
					.isPresent();
	}
}