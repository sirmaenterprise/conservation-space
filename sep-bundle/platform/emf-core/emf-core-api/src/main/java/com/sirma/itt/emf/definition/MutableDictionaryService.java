package com.sirma.itt.emf.definition;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;

/**
 * Service for persisting and updating object definitions.
 * 
 * @author BBonev
 */
public interface MutableDictionaryService extends Serializable {

	/**
	 * Initialize base property definitions. These definitions will be used later as a base for
	 * building dynamic definitions
	 */
	public void initializeBasePropertyDefinitions();

	/**
	 * Checks if is definition equals.
	 * 
	 * @param <E>
	 *            the element type
	 * @param case1
	 *            the case1
	 * @param case2
	 *            the case2
	 * @return true, if is definition equals
	 */
	<E extends DefinitionModel> boolean isDefinitionEquals(E case1, E case2);

	/**
	 * Save definition.
	 * 
	 * @param <E>
	 *            the element type
	 * @param definition
	 *            the definition
	 * @return the case definition
	 */
	<E extends TopLevelDefinition> E saveDefinition(E definition);

	/**
	 * Save template definition.
	 * 
	 * @param <E>
	 *            the definition type
	 * @param definitionModel
	 *            the definition model
	 * @return the document definition
	 */
	<E extends TopLevelDefinition> E saveTemplateDefinition(E definitionModel);

	/**
	 * Save data type definition.
	 * 
	 * @param typeDefinition
	 *            the type definition
	 * @return the data type definition
	 */
	DataTypeDefinition saveDataTypeDefinition(DataTypeDefinition typeDefinition);

	/**
	 * Save property if changed. The method compares the two properties and if different saves the
	 * new one as new entry into the database. If the old properties didn't exists before that then
	 * the new one is always persisted. If properties are equal (except DB id) then on the new
	 * object is copied the ID from the old property.
	 * 
	 * @param newProperty
	 *            the property that need to be saved.
	 * @param oldProperty
	 *            the old property to compare against
	 * @return the updated property definition that corresponds to the DB record
	 */
	PropertyDefinition savePropertyIfChanged(PropertyDefinition newProperty,
			PropertyDefinition oldProperty);

	/**
	 * Removes the definitions without instances.
	 * 
	 * @param definitionsToCheck
	 *            the definitions to check
	 * @return the list
	 */
	List<Pair<String, String>> removeDefinitionsWithoutInstances(Set<String> definitionsToCheck);
}
