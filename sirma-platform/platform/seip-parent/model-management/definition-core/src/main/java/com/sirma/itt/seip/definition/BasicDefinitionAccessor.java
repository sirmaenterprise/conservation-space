package com.sirma.itt.seip.definition;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.definition.model.BaseDefinition;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.util.EqualsHelper;
import com.sirma.sep.definition.db.DefinitionEntry;

/**
 * Definition accessor class for {@link BaseDefinition}
 *
 * @author BBonev
 */
@ApplicationScoped
public class BasicDefinitionAccessor extends BaseDefinitionAccessor implements DefinitionAccessor {

	private static final long serialVersionUID = 733115827873068847L;

	private static final Set<Class<?>> SUPPORTED_OBJECTS = Collections.singleton(BaseDefinition.class);

	@Inject
	protected HashCalculator hashCalculator;

	@Override
	public Set<Class<?>> getSupportedObjects() {
		return SUPPORTED_OBJECTS;
	}

	@Override
	public <E extends DefinitionModel> List<E> getAllDefinitions() {
		DataTypeDefinition typeDefinition = getDataTypeDefinition(BaseDefinition.class);
		if (typeDefinition == null) {
			return Collections.emptyList();
		}
		// going to fetch base definition without conversion
		return getDefinitionsInternal(DefinitionEntry.QUERY_MAX_REVISION_OF_DEFINITIONS_BY_FILTER_KEY, null, null, typeDefinition,
				Boolean.FALSE, false, false, false);
	}

	@Override
	public <E extends DefinitionModel> E getDefinition(String defId) {
		return getDefinition(defId, 0L);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends DefinitionModel> E getDefinition(String defId, Long version) {
		List<DefinitionModel> allDefinitions = getAllDefinitions();
		if (allDefinitions.isEmpty()) {
			return null;
		}
		if (EqualsHelper.nullSafeEquals(allDefinitions.get(0).getIdentifier(), defId)) {
			return (E) allDefinitions.get(0);
		}
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E extends DefinitionModel> E getDefinition(Instance instance) {
		return (E) getAllDefinitions().get(0);
	}

	@Override
	protected DataTypeDefinition detectDataTypeDefinition(DefinitionModel topLevelDefinition) {
		if (topLevelDefinition instanceof BaseDefinition) {
			return getDataTypeDefinition(BaseDefinition.class);
		}
		throw new EmfRuntimeException("Not supported object instance: " + topLevelDefinition.getClass());
	}

	@Override
	protected void updateCache(DefinitionModel definition, boolean propertiesOnly, boolean isMaxRevision) {
		// no cache to update
	}

	@Override
	public int computeHash(DefinitionModel model) {
		return hashCalculator.computeHash(model).intValue();
	}

	@Override
	public <E extends TopLevelDefinition> E saveDefinition(E definition) {
		return super.saveDefinition(definition, this);
	}

	@Override
	public String getDefaultDefinitionId(Object instance) {
		if (instance instanceof BaseDefinition || BaseDefinition.class.equals(instance)) {
			return "$DEFAULT_DEFINITION$";
		}
		return null;
	}
}
