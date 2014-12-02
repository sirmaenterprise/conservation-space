package com.sirma.itt.emf.definition.dao;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.instance.model.Instance;

/**
 * Definition accessor for non existent {@link CommonInstance} definitions.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class CommonInstanceDefinitionAccessor implements DefinitionAccessor {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Class<?>> getSupportedObjects() {
		return new HashSet<Class<?>>(Arrays.asList(CommonInstance.class));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends DefinitionModel> List<E> getAllDefinitions(String container) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends DefinitionModel> E getDefinition(String container, String defId) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends DefinitionModel> E getDefinition(String container, String defId, Long version) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends DefinitionModel> List<E> getDefinitionVersions(String container, String defId) {
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends DefinitionModel> E getDefinition(Instance instance) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <E extends TopLevelDefinition> E saveDefinition(E definition) {
		return definition;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeDefinition(String definition, long version) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int computeHash(DefinitionModel model) {
		return -1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> getActiveDefinitions() {
		return Collections.emptySet();
	}

	@Override
	public int updateDefinitionRevisionToMaxVersion(String... definitionIds) {
		// nothing to migrate - all template definitions are without revision
		return 0;
	}

}
