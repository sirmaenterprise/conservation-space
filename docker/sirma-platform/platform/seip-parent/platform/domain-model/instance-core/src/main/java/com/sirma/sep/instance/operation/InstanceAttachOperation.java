package com.sirma.sep.instance.operation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.AbstractOperation;
import com.sirma.itt.seip.instance.actions.InstanceOperation;
import com.sirma.itt.seip.instance.actions.InstanceOperationProperties;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.event.InstanceAttachedEvent;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Default implementation for attach operation.
 *
 * @author BBonev
 */
@Extension(target = InstanceOperation.TARGET_NAME, order = 99980)
public class InstanceAttachOperation extends AbstractOperation {

	private static final Set<String> SUPPORTED_OPERATIONS = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(ActionTypeConstants.ATTACH, ActionTypeConstants.ATTACH_OBJECT,
					ActionTypeConstants.ATTACH_DOCUMENT, ActionTypeConstants.ADD_LIBRARY)));

	@Inject
	private InstanceContextService contextService;
	@Inject
	private DomainInstanceService domainInstanceService;

	@Override
	public Set<String> getSupportedOperations() {
		return SUPPORTED_OPERATIONS;
	}

	@Override
	public Object execute(Context<String, Object> executionContext) {
		Instance instance = getTargetInstance(executionContext);
		Operation operation = getExecutedOperation(executionContext);

		Instance[] instances = executionContext.getIfSameType(InstanceOperationProperties.INSTANCE_ARRAY,
				Instance[].class);
		attach(instance, operation, instances);
		return null;
	}

	private void attach(Instance context, Operation operation, Instance... children) {
		beforeOperation(context, operation);
		InstanceEventProvider<Instance> eventProvider = serviceRegistry.getEventProvider(context);
		for (Instance child : children) {
			InstanceAttachedEvent<Instance> event = eventProvider.createAttachEvent(context, child);
			event.setOperationId(Operation.getUserOperationId(operation));
			contextService.bindContext(child, context);
			eventService.fire(event);
			domainInstanceService.save(InstanceSaveContext.create(child, operation));
		}
	}
}
