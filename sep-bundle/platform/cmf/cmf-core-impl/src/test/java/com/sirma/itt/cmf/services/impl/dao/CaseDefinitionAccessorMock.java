package com.sirma.itt.cmf.services.impl.dao;

import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Alternative;

import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * The Class CaseDefinitionAccessorMock.
 */
@Alternative
public class CaseDefinitionAccessorMock implements DefinitionAccessor {

	@Override
	public Set<Class<?>> getSupportedObjects() {

		return null;
	}

	@Override
	public <E extends DefinitionModel> List<E> getAllDefinitions(
			String container) {

		return null;
	}

	@Override
	public <E extends DefinitionModel> E getDefinition(String container,
			String defId) {

		return null;
	}

	@Override
	public <E extends DefinitionModel> E getDefinition(String container,
			String defId, Long version) {

		return null;
	}

	@Override
	public <E extends DefinitionModel> List<E> getDefinitionVersions(
			String container, String defId) {

		return null;
	}

	@Override
	public <E extends DefinitionModel> E getDefinition(Instance instance) {

		return null;
	}

	@Override
	public <E extends TopLevelDefinition> E saveDefinition(E definition) {

		return null;
	}

	@Override
	public boolean removeDefinition(String definition, long version) {

		return false;
	}

	@Override
	public int computeHash(DefinitionModel model) {

		return 0;
	}

	@Override
	public Set<String> getActiveDefinitions() {

		return null;
	}

	@Override
	public int updateDefinitionRevisionToMaxVersion(String... definitionIds) {
		return 0;
	}

}
