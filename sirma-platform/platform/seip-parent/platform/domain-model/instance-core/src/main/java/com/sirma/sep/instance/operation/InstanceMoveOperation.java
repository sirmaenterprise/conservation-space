package com.sirma.sep.instance.operation;

import static com.sirma.itt.seip.instance.actions.InstanceOperationProperties.OPERATION;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.AbstractOperation;
import com.sirma.itt.seip.instance.actions.InstanceOperation;
import com.sirma.itt.seip.instance.actions.InstanceOperationProperties;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.event.BeforeInstanceMoveEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Default implementation for move operation.
 *
 * @author S.Djulgerova
 */
@Extension(target = InstanceOperation.TARGET_NAME, order = 99990)
public class InstanceMoveOperation extends AbstractOperation {

	private static final Set<String> SUPPORTED_OPERATIONS = Collections.singleton(ActionTypeConstants.MOVE);

	@Inject
	private InstanceContextService contextService;

	@Inject
	private DomainInstanceService domainInstanceService;

	@Inject
	private InstancePropertyNameResolver fieldConverter ;

	@Override
	public Set<String> getSupportedOperations() {
		return SUPPORTED_OPERATIONS;
	}

	@Override
	public Object execute(Context<String, Object> executionContext) {
		Instance newParent = executionContext.getIfSameType(InstanceOperationProperties.INSTANCE, Instance.class);
		Instance toMove = executionContext.getIfSameType(InstanceOperationProperties.SOURCE_INSTANCE, Instance.class);
		move(newParent, toMove, getOperation(executionContext));
		return null;
	}

	private static Operation getOperation(Context<String, Object> executionContext) {
		Object operation = executionContext.get(OPERATION);
		return operation != null ? (Operation) operation : new Operation(ActionTypeConstants.MOVE);
	}

	private void move(Instance newParent, Instance instanceToMove, Operation operation) {
		Instance previousParent = getPreviousParent(instanceToMove);
		// notify that the file is going to be moved
		BeforeInstanceMoveEvent event = new BeforeInstanceMoveEvent(instanceToMove, previousParent, newParent);
		eventService.fire(event);
		instanceToMove.add(InstanceContextService.HAS_PARENT, newParent.getId(), fieldConverter);
		// just set new parent into instance.
		save(instanceToMove, operation);
		// notify that the file has been moved.
		// NOTE: that the new event contains the instance that is attached to the new instance
		eventService.fireNextPhase(event);
	}

	private Instance getPreviousParent(Instance instanceToMove) {
		return contextService.getContext(instanceToMove).map(InstanceReference::toInstance).orElse(null);
	}

	private void save(Instance instance, Operation operation) {
		// Save of instance will trigger creation of new version.
		// We disabled audit log because move operation is already triggered/logged it.
		try {
			Options.DISABLE_AUDIT_LOG.enable();
			// Save instance will trigger creation of it's version.
			domainInstanceService.save(InstanceSaveContext.create(instance, operation));
		} finally {
			Options.DISABLE_AUDIT_LOG.disable();
		}
	}
}
