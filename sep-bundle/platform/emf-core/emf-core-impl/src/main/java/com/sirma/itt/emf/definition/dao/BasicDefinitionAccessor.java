package com.sirma.itt.emf.definition.dao;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import com.sirma.itt.emf.db.EmfQueries;
import com.sirma.itt.emf.definition.model.BaseDefinition;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.hash.HashCalculator;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.util.EqualsHelper;

/**
 * Definition accessor class for {@link BaseDefinition}
 *
 * @author BBonev
 */
@Stateless
public class BasicDefinitionAccessor extends BaseDefinitionAccessor implements DefinitionAccessor {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 733115827873068847L;
	/** The Constant SUPPORTED_OBJECTS. */
	private static final Set<Class<?>> SUPPORTED_OBJECTS;
	static {
		SUPPORTED_OBJECTS = new HashSet<Class<?>>();
		SUPPORTED_OBJECTS.add(BaseDefinition.class);
	}

	@Inject
	protected HashCalculator hashCalculator;

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public Set<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends DefinitionModel> List<E> getAllDefinitions(String container) {
		DataTypeDefinition typeDefinition = getDataTypeDefinition(BaseDefinition.class);
		// going to fetch base definition without conversion
		return getDefinitionsInternal(EmfQueries.QUERY_ALL_DEFINITIONS_FILTERED_KEY, null,
				null, container, typeDefinition, Boolean.FALSE, false, false, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends DefinitionModel> E getDefinition(String container, String defId) {
		return getDefinition(container, defId, 0L);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends DefinitionModel> E getDefinition(String container, String defId, Long version) {
		List<DefinitionModel> allDefinitions = getAllDefinitions(container);
		if (allDefinitions.isEmpty()) {
			return null;
		}
		if (EqualsHelper.nullSafeEquals(allDefinitions.get(0).getIdentifier(), defId)) {
			return (E) allDefinitions.get(0);
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends DefinitionModel> List<E> getDefinitionVersions(String container, String defId) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public <E extends DefinitionModel> E getDefinition(Instance instance) {
		return (E) getAllDefinitions(SecurityContextManager.NO_CONTAINER).get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DataTypeDefinition detectDataTypeDefinition(DefinitionModel topLevelDefinition) {
		if (topLevelDefinition instanceof BaseDefinition) {
			return getDataTypeDefinition(BaseDefinition.class);
		}
		throw new EmfRuntimeException("Not supported object instance: "
				+ topLevelDefinition.getClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void updateCache(DefinitionModel definition, boolean propertiesOnly,
			boolean isMaxRevision) {
		// no cache to update
	}

	@Override
	public int computeHash(DefinitionModel model) {
		return hashCalculator.computeHash(model);
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public <E extends TopLevelDefinition> E saveDefinition(E definition) {
		return super.saveDefinition(definition, this);
	}

	@Override
	public Set<String> getActiveDefinitions() {
		return Collections.emptySet();
	}

	@Override
	public int updateDefinitionRevisionToMaxVersion(String... definitionIds) {
		// nothing to migrate
		return 0;
	}

}
