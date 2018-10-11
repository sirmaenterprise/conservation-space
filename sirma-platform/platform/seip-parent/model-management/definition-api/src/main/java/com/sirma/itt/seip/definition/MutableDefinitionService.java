package com.sirma.itt.seip.definition;

import java.io.Serializable;
import java.util.Collection;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * Service for persisting and updating object definitions.
 *
 * @author BBonev
 */
public interface MutableDefinitionService extends Serializable {

	/**
	 * Initialize base property definitions. These definitions will be used later as a base for building dynamic
	 * definitions
	 */
	void initializeBasePropertyDefinitions();

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
	 * Save data type definition.
	 *
	 * @param typeDefinition
	 *            the type definition
	 * @return the data type definition
	 */
	DataTypeDefinition saveDataTypeDefinition(DataTypeDefinition typeDefinition);

	/**
	 * Save property if changed. The method compares the two properties and if different saves the new one as new entry
	 * into the database. If the old properties didn't exists before that then the new one is always persisted. If
	 * properties are equal (except DB id) then on the new object is copied the ID from the old property.
	 *
	 * @param newProperty
	 *            the property that need to be saved.
	 * @param oldProperty
	 *            the old property to compare against
	 * @return the updated property definition that corresponds to the DB record
	 */
	PropertyDefinition savePropertyIfChanged(PropertyDefinition newProperty, PropertyDefinition oldProperty);

	/**
	 * Delete definition.
	 *
	 * @param model
	 *            the model
	 * @return the deleted definition info
	 */
	DeletedDefinitionInfo deleteDefinition(DefinitionModel model);

	/**
	 * Delete last definition.
	 *
	 * @param type
	 *            the type
	 * @param definition
	 *            the definition
	 * @return the deleted definition info
	 */
	DeletedDefinitionInfo deleteLastDefinition(Class<?> type, String definition);

	/**
	 * Delete all definition revisions.
	 *
	 * @param type
	 *            the type
	 * @param definition
	 *            the definition
	 * @return the collection
	 */
	Collection<DeletedDefinitionInfo> deleteAllDefinitionRevisions(Class<?> type, String definition);

	/**
	 * Delete old definition revisions.
	 *
	 * @param type
	 *            the type
	 * @param definition
	 *            the definition
	 * @return the collection
	 */
	Collection<DeletedDefinitionInfo> deleteOldDefinitionRevisions(Class<?> type, String definition);

	/**
	 * Delete definition.
	 *
	 * @param type
	 *            the type
	 * @param definition
	 *            the definition
	 * @param revision
	 *            the revision
	 * @return the deleted definition info
	 */
	DeletedDefinitionInfo deleteDefinition(Class<?> type, String definition, Long revision);

}
