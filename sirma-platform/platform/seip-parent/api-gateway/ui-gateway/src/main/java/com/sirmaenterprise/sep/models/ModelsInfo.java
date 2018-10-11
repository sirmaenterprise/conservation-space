package com.sirmaenterprise.sep.models;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Collection of model information used for generating the output model of the models info rest service.
 *
 * @author BBonev
 */
public class ModelsInfo implements Iterable<ModelInfo> {

	private Collection<ModelInfo> models = new LinkedHashSet<>();
	private String errorMessage;
	/**
	 * Merge the given models to a new single instance
	 *
	 * @param source1
	 *            the source1
	 * @param source2
	 *            the source2
	 * @return the models info
	 */
	public static ModelsInfo merge(ModelsInfo source1, ModelsInfo source2) {
		ModelsInfo result = new ModelsInfo();
		source1.models.forEach(result::add);
		source2.models.forEach(result::add);
		return result;
	}

	/**
	 * Adds the information about the given info to the model
	 *
	 * @param info
	 *            the info
	 * @return the models info
	 */
	public ModelsInfo add(ModelInfo info) {
		models.add(info);
		return this;
	}

	/**
	 * Validate the model information and clean the invalid entries such as entries without model class and default
	 * model information. Also makes sure the returned elements match the required business logic for the allowed
	 * resulting tree structure:
	 * <ul>
	 * <li>classes cannot have only classes as children
	 * <li>class cannot be leaf
	 * <li>only definition can be a leaf
	 * <li>definition cannot have other definition or class as children
	 * <li>you can have a class and definition on the same level
	 * </ul>
	 * <p>
	 * Note that this method modifies the internal structure
	 *
	 * @return the same instance
	 */
	public ModelsInfo validateAndCleanUp() {
		removeEmptyEntries();
		Map<String, ModelInfoNode> nodes = buildTree();
		cleanupTree(nodes);
		removeInvalidNodes(nodes);
		removeInaccessibleNodes(nodes);
		return this;
	}

	private void removeInaccessibleNodes(Map<String, ModelInfoNode> nodes) {
		// remove any nodes that are inaccessible and all their children are inaccessible
		for (Iterator<ModelInfo> it = models.iterator(); it.hasNext();) {
			ModelInfo entry = it.next();
			ModelInfoNode node = nodes.get(entry.getId());
			if (!node.validateAccessible()) {
				it.remove();
			}
		}
	}

	/**
	 * Validate the model information and clean the invalid entries such as entries without model class and default
	 * model information. Also makes sure the returned elements match the required business logic for the allowed
	 * resulting tree structure:
	 * <ul>
	 * <li>classes can have only classes as children
	 * <li>class can be leaf
	 * <li>definition cannot have other definition or class as children
	 * <li>you can have a class and definition on the same level
	 * </ul>
	 * <p>
	 * Note that this method modifies the internal structure
	 *
	 * @return the same instance
	 */
	public ModelsInfo validateAndCleanUpForSearch() {
		removeEmptyEntries();
		Map<String, ModelInfoNode> nodes = buildTree();
		cleanupTree(nodes);
		return this;
	}

	private void removeEmptyEntries() {
		for (Iterator<ModelInfo> it = models.iterator(); it.hasNext();) {
			ModelInfo entry = it.next();
			// remove invalid entries
			if (!entry.validate()) {
				it.remove();
			}
		}
	}

	private Map<String, ModelInfoNode> buildTree() {
		Map<String, ModelInfoNode> nodes = createHashMap(models.size());
		for (ModelInfo modelInfo : models) {
			nodes.put(modelInfo.getId(), new ModelInfoNode(modelInfo));
		}

		// link tree nodes (parent-child)
		for (ModelInfoNode node : nodes.values()) {
			if (node.parentId != null) {
				ModelInfoNode parentNode = nodes.get(node.parentId);
				if (parentNode != null) {
					parentNode.children.add(node);
					node.parent = parentNode;
				} else {
					node.parentId = null;
				}
			}
		}
		return nodes;
	}

	private void cleanupTree(Map<String, ModelInfoNode> nodes) {
		for (Entry<String, ModelInfoNode> entry : nodes.entrySet()) {
			if (entry.getValue().parentId == null) {
				// trigger cleanup from the root nodes
				// this will remove children that are not needed and
				// after that the validation will return correct results
				entry.getValue().cleanUp();
			}
		}
	}

	private void removeInvalidNodes(Map<String, ModelInfoNode> nodes) {
		// remove any nodes that are invalid after the cleanup
		for (Iterator<ModelInfo> it = models.iterator(); it.hasNext();) {
			ModelInfo entry = it.next();
			ModelInfoNode node = nodes.get(entry.getId());
			// remove invalid entries
			if (!node.validate()) {
				it.remove();
			}
		}
	}

	@Override
	public Iterator<ModelInfo> iterator() {
		return models.iterator();
	}

	/**
	 * Checks if there are any valid models.
	 *
	 * @return true, if is empty and no valid models are contained
	 */
	public boolean isEmpty() {
		return models.isEmpty();
	}

	/**
	 * The number of valid models
	 *
	 * @return the models count
	 */
	public int size() {
		return models.size();
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		return new StringBuilder(2048).append("Models [models=").append(models).append("]").toString();
	}

	/**
	 * Represent a tree node element use for model validation
	 *
	 * @author BBonev
	 */
	private static class ModelInfoNode {
		String parentId;
		ModelInfoNode parent;
		boolean allowLeaf;
		boolean isAccessible;
		Set<ModelInfoNode> children = new LinkedHashSet<>();


		ModelInfoNode(ModelInfo entry) {
			parentId = entry.getParentId();
			allowLeaf = entry.isDefinition();
			isAccessible = entry.getIsAccessible();
		}

		void cleanUp() {
			if (allowLeaf) {
				// leaf elements does not allow children
				children.clear();
				return;
			}
			for (Iterator<ModelInfoNode> it = children.iterator(); it.hasNext();) {
				ModelInfoNode child = it.next();
				child.cleanUp();
				if (!child.validate()) {
					it.remove();
				}
			}
		}

		boolean validate() {
			if (allowLeaf) {
				// definition leafs cannot have children
				if (parent != null && parent.allowLeaf) {
					return false;
				}
				return children.isEmpty();
			}
			// do not allow non leaf element to be a leaf
			if (children.isEmpty()) {
				return false;
			}
			// find at least one children that is valid leaf
			return children
					.stream()
						.flatMap(ModelInfoNode::childrenStream)
						.filter(ModelInfoNode::isValidLeaf)
						.findAny()
						.isPresent();
		}

		boolean validateAccessible(){
			return isAccessible || children
					.stream()
						.flatMap(ModelInfoNode::childrenStream)
						.filter(node -> node.isAccessible)
						.findAny()
						.isPresent();
		}

		private boolean isValidLeaf() {
			return allowLeaf && children.isEmpty();
		}

		/**
		 * @return a stream of the current node and all children and their children all the way to the bottom of the
		 *         tree
		 */
		Stream<ModelInfoNode> childrenStream() {
			if (children.isEmpty()) {
				return Stream.of(this);
			}
			return Stream.concat(Stream.of(this), children.stream().flatMap(ModelInfoNode::childrenStream));
		}
	}
}
