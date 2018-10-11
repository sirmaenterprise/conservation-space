package com.sirma.sep.model;

import com.sirma.sep.model.management.ModelAttribute;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Defines the behaviour of a node in model hierarchy.
 *
 * @author Mihail Radkov
 */
public interface ModelNode {

	/**
	 * Returns the identifier for the current node.
	 *
	 * @return identifier as {@link String}
	 */
	String getId();

	/**
	 * Returns the identifier of the current node's parent if there is any.
	 *
	 * @return parent's identifier as {@link String} or <code>null</code> if the current node has no parent
	 */
	String getParent();

	/**
	 * Returns the current node's parent reference if there is a parent.
	 *
	 * @return the parent reference or <code>null</code> if the current node has no parent.
	 */
	ModelNode getParentReference();

	/**
	 * Returns the current node's corresponding labels mapping.
	 * <p>
	 * The mapping keys should be the language abbreviation where the values the corresponding translation of the current model node.
	 *
	 * @return labels mapping or empty map if the current node has no labels
	 */
	Map<String, String> getLabels();

	/**
	 * Returns the current node's attributes.
	 *
	 * @return attributes related to the current node or empty list if it has none
	 */
	Collection<ModelAttribute> getAttributes();

	/**
	 * Tries to find a {@link ModelAttribute} corresponding to the given name either in this model node or in the parent hierarchy if this
	 * one has a parent.
	 *
	 * @param name
	 * 		the attribute name to search for
	 * @return the attribute or {@link Optional#empty()} if there is no such attribute
	 */
	Optional<ModelAttribute> getAttribute(String name);
}
