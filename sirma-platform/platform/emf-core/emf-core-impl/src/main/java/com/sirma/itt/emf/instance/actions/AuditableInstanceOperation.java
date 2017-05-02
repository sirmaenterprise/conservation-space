package com.sirma.itt.emf.instance.actions;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.AbstractOperation;
import com.sirma.itt.seip.instance.actions.InstanceOperation;
import com.sirma.itt.seip.instance.notification.MessageLevel;
import com.sirma.itt.seip.instance.notification.NotificationMessage;
import com.sirma.itt.seip.instance.notification.NotificationSupport;
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

	private static final Logger LOGGER = LoggerFactory.getLogger(AuditableInstanceOperation.class);

	private static final String EMF_AUDITABLE_ACTION_MESSAGE_LABEL = "emf.auditable.action.message";

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private javax.enterprise.inject.Instance<NotificationSupport> notificationSupport;

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

		notifyForAuditableAction();
		return null;
	}

	/**
	 * Uses {@link com.sirma.itt.seip.instance.notification.NotificationSupport} to show message for executing auditable action.
	 */
	private void notifyForAuditableAction() {
		// in case of problem with the notification
		try {
			notificationSupport.get().addMessage(new NotificationMessage(
					labelProvider.getValue(EMF_AUDITABLE_ACTION_MESSAGE_LABEL), MessageLevel.INFO));
		} catch (Exception e) {
			LOGGER.debug("Error occurred while showing the message for auditable action.", e);
		}
	}

}
