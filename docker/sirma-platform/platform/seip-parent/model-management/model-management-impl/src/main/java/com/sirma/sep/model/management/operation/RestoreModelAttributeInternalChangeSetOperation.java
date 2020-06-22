package com.sirma.sep.model.management.operation;

import java.util.stream.Stream;

import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.ModelAttribute;
import com.sirma.sep.model.management.ModelField;
import com.sirma.sep.model.management.Models;
import com.sirma.sep.model.management.Path;
import com.sirma.sep.model.management.definition.DefinitionModelAttributes;

/**
 * Model change set operation for restoring a single model attribute. The operation just remove the attribute from the
 * attribute context so that it's value will be resolved from the parent node.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 16/11/2018
 */
class RestoreModelAttributeInternalChangeSetOperation extends RestoreModelNodeChangeSetOperation<ModelAttribute> {

	public static final String INTERNAL_RESTORE = "internalRestore";

	@Override
	public boolean isAccepted(Object target) {
		return target instanceof ModelAttribute;
	}

	@Override
	public Stream<ModelChangeSetInfo> applyChange(Models models, ModelAttribute targetNode, ModelChangeSet changeSet) {
		ModelNode targetNodeContext = targetNode.getContext();

		// to restore an attribute we first have to handle the specific attribute logic if needed, then remove the attribute
		// we do not remove attribute here as it's used in the optional region assign operation
		return Stream.concat(
				handleFieldTargetNode(targetNode, targetNodeContext),
				Stream.of(RemoveNodeChangeSetOperation.createChange(changeSet)));
	}

	@Override
	public String getName() {
		return INTERNAL_RESTORE;
	}

	public static ModelChangeSetInfo createChange(Path selector) {
		return ModelChangeSetInfo.createIntermediate(selector, INTERNAL_RESTORE, null, null);
	}

	public static ModelChangeSetInfo createChange(ModelChangeSet changeSet) {
		return ModelChangeSetInfo.createIntermediate(changeSet, INTERNAL_RESTORE);
	}

	private Stream<ModelChangeSetInfo> handleFieldTargetNode(ModelAttribute targetNode, ModelNode targetNodeContext) {
		if (!(targetNodeContext instanceof ModelField)) {
			return Stream.empty();
		}
		if (DefinitionModelAttributes.REGION_ID.equals(targetNode.getName())) {
			return AssignToRegionChangeSetOperation.createChange(targetNodeContext.getPath(), null);
		}
		return Stream.empty();
	}
}
