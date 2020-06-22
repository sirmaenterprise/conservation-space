package com.sirma.itt.emf.definition.dao;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.definition.DefinitionAccessor;
import com.sirma.itt.seip.definition.DeletedDefinitionInfo;
import com.sirma.itt.seip.definition.TopLevelDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.CommonInstance;

/**
 * Definition accessor for non existent {@link CommonInstance} definitions.
 *
 * @author BBonev
 */
@ApplicationScoped
public class CommonInstanceDefinitionAccessor implements DefinitionAccessor {

	@Override
	public Set<Class<?>> getSupportedObjects() {
		return new HashSet<>(Arrays.asList(CommonInstance.class));
	}

	@Override
	public <E extends DefinitionModel> List<E> getAllDefinitions() {
		return Collections.emptyList();
	}

	@Override
	public <E extends DefinitionModel> E getDefinition(String defId) {
		return null;
	}

	@Override
	public <E extends DefinitionModel> E getDefinition(String defId, Long version) {
		return null;
	}

	@Override
	public <E extends DefinitionModel> E getDefinition(Instance instance) {
		return null;
	}

	@Override
	public <E extends TopLevelDefinition> E saveDefinition(E definition) {
		return definition;
	}

	@Override
	public Collection<DeletedDefinitionInfo> removeDefinition(String definition, long version,
			DefinitionDeleteMode mode) {
		return Collections.emptyList();
	}

	@Override
	public int computeHash(DefinitionModel model) {
		return -1;
	}

	@Override
	public String getDefaultDefinitionId(Object target) {
		return null;
	}
}
