package com.sirma.itt.cmf.services.actions;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.instance.actions.AbstractOperation;
import com.sirma.itt.seip.instance.actions.InstanceOperation;
import com.sirma.itt.seip.instance.actions.InstanceOperationProperties;
import com.sirma.itt.seip.instance.dao.InstanceService;
import com.sirma.itt.seip.instance.event.BeforeInstanceMoveEvent;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Default implementation for move operation
 *
 * @author S.Djulgerova
 */
@Extension(target = InstanceOperation.TARGET_NAME, order = 99990)
public class InstanceMoveOperation extends AbstractOperation {

	private static final Set<String> SUPPORTED_OPERATIONS = Collections.singleton(ActionTypeConstants.MOVE);

	@Inject
	protected LinkService linkService;

	@Inject
	private InstanceService instanceService;

	@Override
	public Set<String> getSupportedOperations() {
		return SUPPORTED_OPERATIONS;
	}

	@Override
	public Object execute(Context<String, Object> executionContext) {
		Instance targetInstance = executionContext.getIfSameType(InstanceOperationProperties.INSTANCE, Instance.class);
		Instance sourceInstance = executionContext.getIfSameType(InstanceOperationProperties.SOURCE_INSTANCE,
				Instance.class);
		Operation operation = executionContext.getIfSameType(InstanceOperationProperties.OPERATION, Operation.class);
		move(targetInstance, operation, sourceInstance);
		return null;
	}

	/**
	 * Performs the move operation
	 *
	 * @param target
	 *            the target instance
	 * @param operation
	 *            the operation
	 * @param src
	 *            the document which will be moved
	 */
	public void move(Instance target, Operation operation, Instance src) {
		Instance parent = InstanceUtil.getDirectParent(src);

		// notify that the file is going to be moved
		BeforeInstanceMoveEvent event = new BeforeInstanceMoveEvent(src, parent, target);
		eventService.fire(event);

		// prevent logging of the attach/detach events, which cause duplicated
		// actions
		Options.DISABLE_AUDIT_LOG.enable();
		try {
			// remove from the old location (if owning instance is case section)
			if (parent != null) {
				instanceService.detach(parent, operation, src);
			}

			// attach to the new one
			instanceService.attach(target, operation, src);
		} finally {
			// enable the audit log in order to log the after document move
			// event
			Options.DISABLE_AUDIT_LOG.disable();
		}

		// notify that the file has been moved.
		// NOTE: that the new event is with the new document
		// instance and is attached the the new instance
		eventService.fireNextPhase(event);
		if (parent != null && !parent.getId().equals(target.getId())) {
			removeRelations(src, parent);
		}
	}

	/**
	 * Remove the partOf and hasParent relations between the child and the parent.
	 *
	 * @param src
	 *            the child instance
	 * @param parent
	 *            the parent instance
	 */
	private void removeRelations(Instance src, Instance parent) {
		if (parent != null) {
			// Remove partOf and hasParent links before the move continue - Simple relations
			linkService.unlinkSimple(parent.toReference(), src.toReference(), LinkConstants.HAS_CHILD_URI,
					LinkConstants.PART_OF_URI);
			linkService.unlinkSimple(parent.toReference(), src.toReference(), LinkConstants.TREE_PARENT_TO_CHILD,
					LinkConstants.TREE_CHILD_TO_PARENT);
			linkService.unlinkSimple(parent.toReference(), src.toReference(), LinkConstants.HAS_ATTACHMENT,
					LinkConstants.IS_ATTACHED_TO);

			// Remove PartOf - Complex relation
			linkService.unlink(parent.toReference(), src.toReference(), LinkConstants.HAS_CHILD_URI,
					LinkConstants.PART_OF_URI);
		}
	}

}