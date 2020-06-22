package com.sirma.itt.emf.instance.actions;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.AbstractOperation;
import com.sirma.itt.seip.instance.actions.InstanceOperation;
import com.sirma.itt.seip.instance.properties.PropertiesChangeEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Instance operation that triggers added an operation to audit log.
 *
 * @author BBonev
 */
@Extension(target = InstanceOperation.TARGET_NAME, order = 90000)
public class AuditableInstanceOperation extends AbstractOperation {

	private static final Set<String> SUPPORTED_OPERATIONS = Collections.singleton("auditable");

	@Override
	public Set<String> getSupportedOperations() {
		return SUPPORTED_OPERATIONS;
	}

	@Override
	public Object execute(Context<String, Object> executionContext) {
		Instance instance = getTargetInstance(executionContext);
		Operation operation = getExecutedOperation(executionContext);
		// fire the event to notify for dummy instance change
		eventService.fire(new PropertiesChangeEvent(instance, Collections.<String, Serializable> emptyMap(),
				Collections.<String, Serializable> emptyMap(), operation));

		return null;
	}

}
