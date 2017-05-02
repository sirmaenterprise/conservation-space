package com.sirma.itt.emf.definition;

import java.util.Collection;

import javax.enterprise.inject.Alternative;

import com.sirma.itt.seip.definition.DeletedDefinitionInfo;
import com.sirma.itt.seip.definition.MutableDictionaryService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.TopLevelDefinition;

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
		// nothing to do
	}

	@Override
	public <E extends DefinitionModel> boolean isDefinitionEquals(E case1, E case2) {

		return false;
	}

	@Override
	public <E extends TopLevelDefinition> E saveDefinition(E definition) {

		return null;
	}

	@Override
	public <E extends TopLevelDefinition> E saveTemplateDefinition(E definitionModel) {

		return null;
	}

	@Override
	public DataTypeDefinition saveDataTypeDefinition(DataTypeDefinition typeDefinition) {

		return null;
	}

	@Override
	public PropertyDefinition savePropertyIfChanged(PropertyDefinition newProperty, PropertyDefinition oldProperty) {

		return null;
	}

	@Override
	public DeletedDefinitionInfo deleteDefinition(DefinitionModel model) {
		// implement me!
		return null;
	}

	@Override
	public DeletedDefinitionInfo deleteLastDefinition(Class<?> type, String definition) {
		// implement me!
		return null;
	}

	@Override
	public Collection<DeletedDefinitionInfo> deleteAllDefinitionRevisions(Class<?> type, String definition) {
		// implement me!
		return null;
	}

	@Override
	public Collection<DeletedDefinitionInfo> deleteOldDefinitionRevisions(Class<?> type, String definition) {
		// implement me!
		return null;
	}

	@Override
	public DeletedDefinitionInfo deleteDefinition(Class<?> type, String definition, Long revision) {
		// implement me!
		return null;
	}

}
