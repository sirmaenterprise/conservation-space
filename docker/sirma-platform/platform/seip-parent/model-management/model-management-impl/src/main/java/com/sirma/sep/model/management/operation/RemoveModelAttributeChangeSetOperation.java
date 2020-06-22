package com.sirma.sep.model.management.operation;

import java.util.stream.Stream;

import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.ModelAttribute;
import com.sirma.sep.model.management.Models;

/**
 * Model change set operation for removing any non detached {@link ModelAttribute}. Non detached attributes are
 * considered nodes those which method {@link ModelNode#isDetached() isDetached()} returns {@code false}.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 28/11/2018
 */
public class RemoveModelAttributeChangeSetOperation extends RemoveNodeChangeSetOperation<ModelAttribute> {

	@Override
	public boolean isAccepted(Object target) {
		return target instanceof ModelAttribute;
	}

	@Override
	public Stream<ModelChangeSetInfo> applyChange(Models models, ModelAttribute targetNode, ModelChangeSet changeSet) {
		if (!targetNode.isDetached()) {
			targetNode.detach();
		}
		return Stream.empty();
	}
}
