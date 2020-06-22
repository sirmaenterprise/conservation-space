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
import com.sirma.itt.seip.instance.event.InstanceDetachedEvent;
import com.sirma.itt.seip.instance.event.InstanceEventProvider;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Default implementation for detach operation.
 *
 * @author BBonev
 */
@Extension(target = InstanceOperation.TARGET_NAME, order = 99970)
public class InstanceDetachOperation extends AbstractOperation {

	private static final Set<String> SUPPORTED_OPERATIONS = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList(ActionTypeConstants.DETACH,
					ActionTypeConstants.DETACH_DOCUMENT, ActionTypeConstants.DETACH_OBJECT)));

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
		Instance instance = executionContext.getIfSameType(InstanceOperationProperties.INSTANCE, Instance.class);
		Operation operation = executionContext.getIfSameType(InstanceOperationProperties.OPERATION, Operation.class);
		Instance[] instances = executionContext.getIfSameType(InstanceOperationProperties.INSTANCE_ARRAY,
				Instance[].class);
		detach(instance, operation, instances);
		return null;
	}

	private void detach(Instance context, Operation operation, Instance... children) {
		InstanceEventProvider<Instance> eventProvider = serviceRegistry.getEventProvider(context);
		for (Instance child : children) {
			InstanceDetachedEvent<Instance> event = eventProvider.createDetachEvent(context, child);
			event.setOperationId(Operation.getUserOperationId(operation));
			contextService.bindContext(child, null);
			eventService.fire(event);
			domainInstanceService.save(InstanceSaveContext.create(child, operation));
		}
	}
}
