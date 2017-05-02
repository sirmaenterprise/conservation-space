package com.sirma.itt.seip.template;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.seip.definition.DefinitionAccessor;
import com.sirma.itt.seip.definition.DeletedDefinitionInfo;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.TopLevelDefinition;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Default definition accessor for template definitions. The accessor cannot be used for saving/loading of definition.
 *
 * @author BBonev
 */
@ApplicationScoped
public class TemplateDefinitionAccessor implements DefinitionAccessor {
	private static final Set<Class<?>> SUPPORTED_OBJECTS = Collections.unmodifiableSet(new HashSet<Class<?>>(
			Arrays.asList(TemplateInstance.class, TemplateDefinition.class, TemplateDefinitionImpl.class)));

	@Override
	public Set<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
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
		return getDefinition(defId);
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
		return 0;
	}

	@Override
	public String getDefaultDefinitionId(Object target) {
		if (target instanceof TemplateInstance) {
			return ((TemplateInstance) target).getIdentifier();
		}
		return null;
	}
}
