package com.sirmaenterprise.sep.bpm.camunda.observers;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.event.ObjectPropertyAddEvent;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.event.EmfEvent;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.actions.save.SaveRequest;
import com.sirma.itt.seip.instance.context.InstanceContextService;
import com.sirma.itt.seip.instance.relation.LinkAddedEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.state.StateChangedEvent;
import com.sirma.itt.seip.security.annotation.SecureObserver;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.tasks.SchedulerService;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;
import com.sirmaenterprise.sep.bpm.camunda.properties.BPMTaskProperties;
import com.sirmaenterprise.sep.bpm.camunda.schedules.BPMEventScheduler;
import com.sirmaenterprise.sep.bpm.camunda.service.BPMSecurityService;
import com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService;
import com.sirmaenterprise.sep.bpm.camunda.util.BPMActivityUtil;

/**
 * Observer class that listen for {@link StateChangedEvent} and executing basic domain logic based on the event.
 * Contains logic for stopping of instance in the process engine.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class OperationsExecutionListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private CamundaBPMNService camundaBPMNService;
	@Inject
	private BPMSecurityService bpmSecurityService;
	@Inject
	private InstanceTypeResolver instanceResolver;
	@Inject
	private SchedulerService schedulerService;
	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private SecurityContextManager securityContextManager;

	/**
	 * Observers {@link LinkAddedEvent} because of the corner case with "create" operation, which is fired with context
	 * of current created document not with parent.
	 *
	 * @param event
	 *            the current {@link LinkAddedEvent} event
	 */
	@SecureObserver
	public void handleSEPObjectEvents(@Observes ObjectPropertyAddEvent event) {
		if (!bpmSecurityService.isEngineAvailable()) {
			return;
		}

		catchForCreateOperation(event);
		catchAssigneeChange(event);
	}

	private void catchForCreateOperation(ObjectPropertyAddEvent event) {
		if (!InstanceContextService.HAS_PARENT.equalsIgnoreCase(event.getObjectPropertyName())) {
			return;
		}

		Serializable currentOperation = Options.CURRENT_OPERATION.get();
		if (currentOperation instanceof Operation && ((Operation) currentOperation).isUserOperation()) {
			String serverAction = ((Operation) currentOperation).getOperation();
			String userAction = ((Operation) currentOperation).getUserOperationId();
			if (ActionTypeConstants.CREATE.equalsIgnoreCase(serverAction)
					&& ActionTypeConstants.CREATE.equalsIgnoreCase(userAction)) {
				notifyScheduleOnEvent(event.getTargetId(), userAction);
			}

		}
	}

	private void notifyScheduleOnEvent(Serializable targetId, String userAction) {
		Optional<InstanceReference> instanceReference = instanceResolver.resolveReference(targetId);
		if (!instanceReference.isPresent()) {
			LOGGER.warn("No instance: {} found, cannot notify schedule for userAction: {}", targetId, userAction);
			return;
		}

		SchedulerContext context = new SchedulerContext(2);
		context.put(BPMEventScheduler.USER_ACTION, userAction);
		context.put(BPMEventScheduler.SERVER_ACTION, SaveRequest.OPERATION_NAME);
		EmfEvent bpmEvent = BPMEventScheduler.createConfigurationEvent(context, instanceReference.get().toInstance());
		Executable executable = securityContextManager
				.executeAsAdmin()
					.toWrapper()
					.executable(() -> schedulerService.onEvent(bpmEvent));
		transactionSupport.invokeOnSuccessfulTransactionInTx(executable);
	}

	private void catchAssigneeChange(ObjectPropertyAddEvent event) {
		String objectPropertyName = event.getObjectPropertyName();
		if (BPMTaskProperties.HAS_ASSIGNEE.equalsIgnoreCase(objectPropertyName)
				|| BPMTaskProperties.HAS_POOL_ASSIGNEE.equalsIgnoreCase(objectPropertyName)) {
			Serializable currentOperation = Options.CURRENT_OPERATION.get();
			if (currentOperation instanceof Operation && ((Operation) currentOperation).isUserOperation()) {
				reassignTask((String) event.getTargetId(), event.getSourceId());
			}
		}
	}

	private void reassignTask(String eventTargetId, Serializable taskId) {
		Optional<InstanceReference> instanceReference = instanceResolver.resolveReference(taskId);
		if (!instanceReference.isPresent()) {
			return;
		}

		Instance taskInstance = instanceReference.get().toInstance();
		if (CamundaBPMService.isActivity(taskInstance) && !BPMActivityUtil.isActivityCompleted(taskInstance)
				&& !CamundaBPMNService.isProcess(taskInstance)) {
			camundaBPMNService.reassignTask(taskInstance, eventTargetId);
		}
	}
}
