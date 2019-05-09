package com.sirma.sep.model.management;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.sirma.sep.model.ModelNode;

/**
 * Store for detached model nodes. The nodes are stored and retrievable using absolute node path. Adding node to the
 * store first and then to a model will change their absolute path and they will not be retrievable any more using the
 * same path.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 22/11/2018
 */
class DetachedModelNodesStore {

	/**
	 * No operation singleton store instance used for null object patern in the model node implementations during
	 * node creation and before assigning it to any {@link Models} subtree. <br>
	 * It's realized with a anonymous class as using a explicit sub class rises a sonar issue.
	 */
	static final DetachedModelNodesStore NO_OP_INSTANCE = new DetachedModelNodesStore() {
		@Override
		public void addDetached(ModelNode modelNode) {
			// no operation
		}

		@Override
		public void addDetached(ModelAttribute modelAttribute) {
			// no operation
		}

		@Override
		public void removeDetached(ModelNode context, String type, String name) {
			// no operation
		}

		@Override
		void removeDetached(ModelNode node) {
			// no operation
		}

		@Override
		void removeDetached(ModelAttribute attribute) {
			// no operation
		}
	};

	private Map<String, ModelNode> detachedNodes = new HashMap<>(64);
	private Map<String, ModelAttribute> detachedAttributes = new HashMap<>(128);

	/**
	 * Register the given model node as detached under it's own path. The node should be part of the model tree and had
	 * it's id set.
	 *
	 * @param modelNode the node to register
	 */
	void addDetached(ModelNode modelNode) {
		if (modelNode == null) {
			// skip null nodes otherwise this check should be before all calls
			return;
		}
		detachedNodes.put(modelNode.getPath().toString(), modelNode);
	}

	/**
	 * Register the given model attribute as detached under it's own path.
	 *
	 * @param modelAttribute to register
	 */
	void addDetached(ModelAttribute modelAttribute) {
		if (modelAttribute == null) {
			// skip null nodes otherwise this check should be before all calls
			return;
		}
		detachedAttributes.put(modelAttribute.getPath().toString(), modelAttribute);
	}

	/**
	 * Check if the store contains a node or attribute described using the given parameters.
	 *
	 * @param context the context of the node or attribute
	 * @param type the node type or attribute
	 * @param name the node name
	 * @return true if the store contains a node or attribute that has the given context is of the given type and has
	 * the given name
	 */
	boolean hasDetached(ModelNode context, String type, String name) {
		String key = createKey(context, type, name);
		return detachedNodes.containsKey(key) || detachedAttributes.containsKey(key);
	}

	/**
	 * Check if the store contains a node or attribute corresponding to the given path.
	 *
	 * @param path the path to check
	 * @return true if the store contains a node or attribute that has the given path
	 */
	boolean hasDetached(Path path) {
		String key = Objects.requireNonNull(path, "Path cannot be null").toString();
		return detachedNodes.containsKey(key) || detachedAttributes.containsKey(key);
	}

	private static String createKey(ModelNode context, String type, String name) {
		Objects.requireNonNull(context, "Context is required for path building");
		Objects.requireNonNull(context.getId(),
				"Context node " + context.getClass().getSimpleName() + " should have an id set.");
		return context.getPath().append(Path.create(type, name)).toString();
	}

	/**
	 * Retrieves a node from the store that matches the given criteria.
	 *
	 * @param context the context of the node
	 * @param type the node type
	 * @param name the node name
	 * @return the node if the store contains a node that has the given context is of the given type and has
	 * the given name
	 */
	Optional<ModelNode> getDetachedNode(ModelNode context, String type, String name) {
		String key = createKey(context, type, name);
		return Optional.ofNullable(detachedNodes.get(key));
	}

	/**
	 * Retrieves a node from the store that matches the path.
	 *
	 * @param path the path to the node to return
	 * @return the found node or empty optional
	 */
	Optional<ModelNode> getDetachedNode(Path path) {
		String key = Objects.requireNonNull(path, "Request path cannot be null").toString();
		return Optional.ofNullable(detachedNodes.get(key));
	}

	/**
	 * Retrieves an attribute from the store that matches the given criteria.
	 *
	 * @param context the context of the attribute
	 * @param type the attribute type
	 * @param name the attribute name
	 * @return the attribute if the store contains a attribute that has the given context is of the given type and has
	 * the given name
	 */
	Optional<ModelAttribute> getDetachedAttribute(ModelNode context, String type, String name) {
		String key = createKey(context, type, name);
		return Optional.ofNullable(detachedAttributes.get(key));
	}

	/**
	 * Retrieves an attribute from the store that matches the path.
	 *
	 * @param path the path to the requested attribute to return if present
	 * @return the found attribute or empty optional
	 */
	Optional<ModelAttribute> getDetachedAttribute(Path path) {
		String key = Objects.requireNonNull(path, "Request path cannot be null").toString();
		return Optional.ofNullable(detachedAttributes.get(key));
	}

	/**
	 * Removes detached node or attribute from the store that matches the given criteria.
	 *
	 * @param context the context of the node or attribute
	 * @param type the node or attribute type
	 * @param name the node or attribute name
	 */
	void removeDetached(ModelNode context, String type, String name) {
		String key = createKey(context, type, name);
		ModelNode removedNode = detachedNodes.remove(key);
		if (removedNode == null) {
			detachedAttributes.remove(key);
		}
	}

	/**
	 * Removes a detached node that matches the path of the given node. Does not need to be the same node that's been
	 * stored, just one with the same path
	 * @param node the node to use for path resolving of the node that needs to be removed from the store
	 */
	void removeDetached(ModelNode node) {
		if (node != null) {
			detachedNodes.remove(node.getPath().toString());
		}
	}

	/**
	 * Removes a detached attribute that matches the path of the given attribute. Does not need to be the same node
	 * that's been stored, just one with the same path
	 * @param attribute the attribute to use for path resolving of the attribute that needs to be removed from the store
	 */
	void removeDetached(ModelAttribute attribute) {
		if (attribute != null) {
			detachedAttributes.remove(attribute.getPath().toString());
		}
	}
}
