package com.sirma.sep.model;

import com.sirma.sep.model.management.ModelAttribute;
import com.sirma.sep.model.management.Path;
import com.sirma.sep.model.management.Walkable;
import com.sirma.sep.model.management.meta.ModelMetaInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Defines the behaviour of a node in model hierarchy.
 *
 * @author Mihail Radkov
 */
public interface ModelNode extends Walkable {

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
	 * Checks if the current node has a parent.
	 *
	 * @return {@code true} if the node has a parent or {@code false} if not
	 */
	boolean hasParent();

	/**
	 * Check if the current node is deployed or is new non deployed node.
	 *
	 * @return true if is already deployed and false if not
	 */
	boolean isDeployed();

	/**
	 * Mark the current node as deployed. By default nodes are non deployed and if they are deployed should be marked
	 * as such. Deployed nodes are these nodes that are not created by a change set but rather from existing model object
	 */
	void setAsDeployed();

	/**
	 * Get nodes children if any.
	 *
	 * @param <M> the children type
	 * @return any children the current node haves. The returned collection is not guaranteed to be mutable.
	 */
	default <M extends ModelNode> List<M> getChildren() {
		return Collections.emptyList();
	}

	/**
	 * Register child to the current node. The current node may register itself as parent to the given child.<br>
	 * Note that if children are not supported the method may throw {@link UnsupportedOperationException}
	 *
	 * @param child the child to register to the current node
	 */
	default void addChild(ModelNode child) {
		throw new UnsupportedOperationException();
	}

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
	 * @return attributes related to the current node or empty collection if it has none
	 */
	Collection<ModelAttribute> getAttributes();

	/**
	 * Tries to find a {@link ModelAttribute} corresponding to the given name either in this model node or in the parent hierarchy if this
	 * one has a parent.
	 *
	 * @param name the attribute name to search for
	 * @return the attribute or {@link Optional#empty()} if there is no such attribute
	 */
	Optional<ModelAttribute> findAttribute(String name);

	/**
	 * Tries to find a {@link ModelAttribute} corresponding to the given name in the current model node's attributes.
	 * <p>
	 * This will not resolve the parent hierarchy, for that use {@link #findAttribute(String)}
	 *
	 * @param name the attribute name to search for
	 * @return the attribute or {@link Optional#empty()} if there is no such attribute
	 */
	Optional<ModelAttribute> getAttribute(String name);

	/**
	 * Inserts the provided value as {@link ModelAttribute} in the node's collection of attributes.
	 * <p>
	 * This could replace existing {@link ModelAttribute} if the name matches one that exist.
	 * <p>
	 * If the value is <code>null</code> it wont be inserted.
	 *
	 * @param name the attribute's name
	 * @param value the attribute's value
	 * @return replaced attribute or <code>null</code> if none existed before the insertion
	 */
	ModelAttribute addAttribute(String name, Object value);

	/**
	 * Checks if the current node contains a {@link ModelAttribute} with the provided name.
	 *
	 * @param name the name of an attribute to search for in the current node
	 * @return <code>true</code> if the current node contains such attribute or <code>false</code> if not
	 */
	boolean hasAttribute(String name);

	/**
	 * Checks if the current node has any {@link ModelAttribute} at all.
	 *
	 * @return true if it has at least one {@link ModelAttribute} or false if not
	 */
	default boolean hasAttributes() {
		return !getAttributes().isEmpty();
	}

	/**
	 * Remove attribute from the model node. This should remove only the attribute for the current node. It effectively
	 * restores inherited value. If attribute with the same name is requested after calling this method it may return
	 * an attribute instance only if the current node has a parent and that parent has such attribute or it's parent
	 * and so on.
	 *
	 * @param name the name of the attribute to remove
	 * @return the removed attribute or {@code null} if there was no attribute for the provided name
	 */
	ModelAttribute removeAttribute(String name);

	/**
	 * Get meta information about the attributes of the current node
	 *
	 * @return meta info mapping by name
	 */
	Map<String, ModelMetaInfo> getAttributesMetaInfo();

	/**
	 * Gets the path to the current node from the beginning of the model.
	 *
	 * @return the path to the model node. Never null.
	 */
	Path getPath();

	/**
	 * Returns the context of the current node. The context, if present, is other node that houses the current node in
	 * a way tree nodes are linked. In other words the context is the parent tree node in a tree structure.
	 *
	 * @return the context node if present
	 */
	ModelNode getContext();

	/**
	 * Checks if the current model node is empty. Empty conditions could be different for different node implementations.
	 * The base condition is the node to have no attributes
	 *
	 * @return if the node is empty.
	 */
	default boolean isEmpty() {
		return getAttributes().isEmpty();
	}

	/**
	 * Detaches the current node from it's context and from any other referenced nodes.
	 *
	 * @return true if the node was detached and false if called on already detached node or the node is not part of
	 * any context
	 */
	boolean detach();

	/**
	 * Checks if the current node is detached/remove from it's context. This means that the node is not accessible by
	 * it's parent get or find methods as well as iterating the corresponding children nodes. The node is only
	 * resolvable using the {@link Walkable#walk(Path)} method.
	 *
	 * @return true if the current node is detached/removed/deleted from it's context
	 */
	boolean isDetached();

	/**
	 * Checks if the given attribute is detached/removed/deleted from the current node. This means that the attribute
	 * is no longer resolvable via the get or find methods as well as from the {@link #getAttributes()} method.
	 * The attribute is only resolvable using the {@link Walkable#walk(Path)} method of the current node.
	 * Note that this method is not guaranteed to work for attributes that have never been part of the model graph.
	 *
	 * @param attribute to check
	 * @return true if the attribute is detached/removed/deleted from the current node
	 */
	boolean isDetached(ModelAttribute attribute);

	/**
	 * Checks if the given node is detached/removed.
	 *
	 * @param context the context of the node
	 * @param type the node type or attribute
	 * @param name the node name
	 * @return true if the current node is detached/removed/deleted from it's context
	 */
	boolean isDetached(ModelNode context, String type, String name);
}
