package com.sirma.itt.cmf.services.impl.dao;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.cmf.beans.definitions.TemplateDefinition;
import com.sirma.itt.cmf.beans.definitions.impl.TemplateDefinitionImpl;
import com.sirma.itt.emf.definition.dao.DefinitionAccessor;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.template.TemplateInstance;

/**
 * Default definition accessor for template definitions. The accessor cannot be used for
 * saving/loading of definition.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class TemplateDefinitionAccessor implements DefinitionAccessor {
	/** The Constant SUPPORTED_OBJECTS. */
	private static final Set<Class<?>> SUPPORTED_OBJECTS = Collections
			.unmodifiableSet(new HashSet<Class<?>>(Arrays.asList(TemplateInstance.class,
					TemplateDefinition.class, TemplateDefinitionImpl.class)));

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
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
		return getDefinition(container, defId);
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
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int computeHash(DefinitionModel model) {
		return 0;
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
