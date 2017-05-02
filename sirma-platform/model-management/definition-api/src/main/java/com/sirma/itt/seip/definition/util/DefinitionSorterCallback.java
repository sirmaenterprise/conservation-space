package com.sirma.itt.seip.definition.util;

/**
 * Callback interface used to provide information for definition while performing sort algorithm.
 *
 * @author BBonev
 * @param <E>
 *            the definition element type
 */
public interface DefinitionSorterCallback<E> {

	/**
	 * Gets the identifier
	 *
	 * @param definition
	 *            the definition
	 * @return the id
	 */
	String getId(E definition);

	/**
	 * Gets the parent definition id.
	 *
	 * @param topLevelDefinition
	 *            the top level definition
	 * @return the parent definition id
	 */
	String getParentDefinitionId(E topLevelDefinition);

	/**
	 * Creates the definition id.
	 *
	 * @param definition
	 *            the definition
	 * @return the string
	 */
	String createDefinitionId(E definition);

	/**
	 * Creates the parent definition id.
	 *
	 * @param definition
	 *            the definition
	 * @return the string
	 */
	String createParentDefinitionId(E definition);
}