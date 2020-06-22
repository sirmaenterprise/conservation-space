package com.sirma.itt.seip.instance.archive;

import static com.sirma.itt.seip.collections.CollectionUtils.emptyList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.definition.DefinitionAccessor;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.DeletedDefinitionInfo;
import com.sirma.itt.seip.definition.TopLevelDefinition;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Implementation of {@link DefinitionAccessor} for {@link ArchivedInstance}s. Used for retrieving the definition of the
 * deleted instance.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class ArchiveDefinitionAccessor implements DefinitionAccessor {

	@Inject
	private DefinitionService definitionService;

	@Override
	public Set<Class<?>> getSupportedObjects() {
		return Collections.singleton(ArchivedInstance.class);
	}

	@Override
	public <E extends DefinitionModel> E getDefinition(String defId, Long version) {
		// nothing to execute
		return null;
	}

	@Override
	public <E extends DefinitionModel> E getDefinition(String defId) {
		// nothing to execute
		return null;
	}

	@Override
	public <E extends DefinitionModel> List<E> getAllDefinitions() {
		// nothing to execute
		return emptyList();
	}

	@Override
	public <E extends DefinitionModel> E getDefinition(Instance instance) {
		if (instance instanceof ArchivedInstance) {
			Instance newInstance = (Instance) ReflectionUtils
					.newInstance(((ArchivedInstance) instance).toReference().getReferenceType().getJavaClass());
			newInstance.setRevision(instance.getRevision());
			newInstance.setIdentifier(instance.getIdentifier());
			return definitionService.getInstanceDefinition(newInstance);
		}
		return null;
	}

	@Override
	public <E extends TopLevelDefinition> E saveDefinition(E definition) {
		return null;
	}

	@Override
	public Collection<DeletedDefinitionInfo> removeDefinition(String definition, long version,
			DefinitionDeleteMode mode) {
		return Collections.emptyList();
	}

	@Override
	public int computeHash(DefinitionModel model) {
		return 0;
	}

	@Override
	public String getDefaultDefinitionId(Object instance) {
		if (instance instanceof ArchivedInstance) {
			// if the instance does not have set definition id we do not resolve it at all
			return ((Identity) instance).getIdentifier();
		}
		return null;
	}
}