package com.sirma.sep.model.management.operation;

import java.util.ArrayList;
import java.util.stream.Stream;

import com.sirma.itt.seip.Copyable;
import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.ModelAttribute;
import com.sirma.sep.model.management.Models;

/**
 * Model change set operation for removing any empty {@link ModelNode}. Empty nodes are considered nodes whose method
 * {@link ModelNode#isEmpty() isEmpty()} returns {@code true}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 28/11/2018
 */
public class RemoveModelNodeChangeSetOperation extends RemoveNodeChangeSetOperation<ModelNode> {

	@Override
	public boolean isAccepted(Object target) {
		return target instanceof ModelNode;
	}

	@Override
	public Stream<ModelChangeSetInfo> applyChange(Models models, ModelNode targetNode, ModelChangeSet changeSet) {
		if (targetNode.isDetached()) {
			// already deleted, nothing to do
			return Stream.empty();
		}
		// already empty node so nothing else to do just remove it
		if (targetNode.isEmpty()) {
			targetNode.detach();
			return Stream.empty();
		}
		// the node is not empty and it has non visible attributes like labelId
		// we should remove these attributes and remove the node
		if (hasOnlyNonVisibleAttributes(targetNode)) {
			// check if removing of all non editable properties the node will be considered empty
			// if so remove all attributes and remove the node
			// this is a special case for complex nodes like regions, where attribute removal is not enough
			// in that case the region will be removed only when the region has no fields in it
			boolean isNodeEligibleForRemoval = Copyable.copy(targetNode)
					.filter(node -> removeAttributes(node).isEmpty())
					.isPresent();
			if (isNodeEligibleForRemoval) {
				removeAttributes(targetNode).detach();
			}
		}
		return Stream.empty();
	}

	private static ModelNode removeAttributes(ModelNode targetNode) {
		new ArrayList<>(targetNode.getAttributes()).forEach(ModelAttribute::detach);
		return targetNode;
	}

	private static boolean hasOnlyNonVisibleAttributes(ModelNode modelNode) {
		return modelNode.getAttributes().stream().noneMatch(attribute -> attribute.getMetaInfo().isVisible());
	}
}
