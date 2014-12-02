package com.sirma.itt.emf.definition;

import java.util.List;
import java.util.Set;

import javax.enterprise.inject.Alternative;

import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;

/**
 * The Class MutableDictionaryServiceMock.
 */
@Alternative
public class MutableDictionaryServiceMock implements MutableDictionaryService {

	/**
	 *
	 */
	private static final long serialVersionUID = 5146679217285809841L;

	@Override
	public void initializeBasePropertyDefinitions() {


	}

	@Override
	public <E extends DefinitionModel> boolean isDefinitionEquals(E case1,
			E case2) {

		return false;
	}

	@Override
	public <E extends TopLevelDefinition> E saveDefinition(E definition) {

		return null;
	}

	@Override
	public <E extends TopLevelDefinition> E saveTemplateDefinition(
			E definitionModel) {

		return null;
	}

	@Override
	public DataTypeDefinition saveDataTypeDefinition(
			DataTypeDefinition typeDefinition) {

		return null;
	}

	@Override
	public PropertyDefinition savePropertyIfChanged(
			PropertyDefinition newProperty, PropertyDefinition oldProperty) {

		return null;
	}

	@Override
	public List<Pair<String, String>> removeDefinitionsWithoutInstances(
			Set<String> definitionsToCheck) {

		return null;
	}

}
