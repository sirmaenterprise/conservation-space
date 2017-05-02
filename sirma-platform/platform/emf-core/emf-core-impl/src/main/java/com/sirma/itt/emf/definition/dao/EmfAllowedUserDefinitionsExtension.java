package com.sirma.itt.emf.definition.dao;

import java.util.List;

import com.sirma.itt.seip.definition.AllowedAuthorityDefinitionsExtension;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.plugin.Extension;

/**
 * The EmfAllowedUserDefinitionsExtension is simply the default implementation that is executed last if no specific
 * filter is already invoked. Returns all models without further filtering.
 *
 * @author bbanchev
 */
@Extension(target = AllowedAuthorityDefinitionsExtension.TARGET_NAME, order = 100)
public class EmfAllowedUserDefinitionsExtension implements AllowedAuthorityDefinitionsExtension {

	@Override
	public <D extends DefinitionModel> boolean isSupported(List<D> model) {
		return model != null;
	}

	@Override
	public <D extends DefinitionModel> List<D> getAllowedDefinitions(List<D> model) {
		return model;
	}

}