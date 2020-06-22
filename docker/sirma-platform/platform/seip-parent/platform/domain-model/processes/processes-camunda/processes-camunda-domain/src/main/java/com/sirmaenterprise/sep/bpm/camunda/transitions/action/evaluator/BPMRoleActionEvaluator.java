package com.sirmaenterprise.sep.bpm.camunda.transitions.action.evaluator;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.ObjectTypes;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.instance.security.DomainObjectsBaseRoleEvaluator;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleEvaluator;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorType;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.resources.Resource;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;
import com.sirmaenterprise.sep.bpm.camunda.service.CamundaBPMService;
import com.sirmaenterprise.sep.bpm.camunda.transitions.model.TransitionModelService;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.BPMStateTransitionProvider;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowEntry;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowModel;
import com.sirmaenterprise.sep.bpm.camunda.util.BPMActivityUtil;

/**
 * BPM Actions Evaluator which verify transition conditions and claim/release actions.
 *
 * @author Hristo Lungov
 */
@ApplicationScoped
@RoleEvaluatorType(ObjectTypes.OBJECT)
@Extension(target = RoleEvaluator.TARGET_NAME, order = 100)
public class BPMRoleActionEvaluator extends DomainObjectsBaseRoleEvaluator<ObjectInstance> {

	@Inject
	private TransitionModelService transitionModelService;

	@Inject
	private CamundaBPMNService camundaBPMNService;

	private static final List<Class> SUPPORTED = Collections.singletonList(ObjectInstance.class);
	/**
	 * Claim BPM Action
	 */
	static final Action CLAIM = new EmfAction(ActionTypeConstants.CLAIM);
	/**
	 * Release BPM Action
	 */
	static final Action RELEASE = new EmfAction(ActionTypeConstants.RELEASE);

	@Override
	public List<Class> getSupportedObjects() {
		return SUPPORTED;
	}

	@Override
	protected Boolean filterInternal(ObjectInstance taskInstance, Resource resource, Role role, Set<Action> actions) {
		if (!CamundaBPMService.isActivity(taskInstance) || CamundaBPMNService.isProcess(taskInstance)) {
			return Boolean.FALSE;
		}
		if (BPMActivityUtil.isActivityCompleted(taskInstance)) {
			verifyBPMTransitionActions(taskInstance, actions, true);
			return Boolean.FALSE;
		}
		String currentUserId = resource.getId().toString();
		if (camundaBPMNService.isTaskPooled(taskInstance)) {
			if (camundaBPMNService.isTaskClaimable(taskInstance, currentUserId)) {
				// can claim remove release and remove bpm transition actions
				actions.remove(RELEASE);
				verifyBPMTransitionActions(taskInstance, actions, true);
			} else if (camundaBPMNService.isTaskReleasable(taskInstance, currentUserId)) {
				// can not claim remove claim action and show bpm transition actions
				actions.remove(CLAIM);
				verifyBPMTransitionActions(taskInstance, actions, false);
			} else {
				actions.remove(CLAIM);
				actions.remove(RELEASE);
				verifyBPMTransitionActions(taskInstance, actions, true);
			}
		} else {
			// not pool task, so remove actions
			actions.remove(CLAIM);
			actions.remove(RELEASE);
			if (camundaBPMNService.isTaskAssignee(taskInstance, currentUserId)) {
				// is assignee show bpm transition action
				verifyBPMTransitionActions(taskInstance, actions, false);
			} else {
				// not an assignee remove bpm transition action
				verifyBPMTransitionActions(taskInstance, actions, true);
			}

		}
		return Boolean.FALSE;
	}

	/**
	 * Verify transition action conditions if they must be seen.
	 *
	 * @param instance
	 * 		task activity instance
	 * @param actions
	 * 		all actions
	 * @param removeBPMTransitions
	 * 		to remove or not bpm transition actions
	 */
	protected void verifyBPMTransitionActions(ObjectInstance instance, Set<Action> actions,
			boolean removeBPMTransitions) {
		SequenceFlowModel sequenceFlowModel;
		if ((sequenceFlowModel = BPMStateTransitionProvider.getSequenceFlowModel(instance)) != null) {
			Predicate<SequenceFlowEntry> transitionFilter = transitionModelService.createTransitionConditionFilter(
					instance);
			Set<Action> bpmActions = new LinkedHashSet<>();
			for (Action action : actions) {
				SequenceFlowEntry transition = sequenceFlowModel.getTransition(action.getActionId());
				if (transition == null) {
					bpmActions.add(action);
				} else if (!removeBPMTransitions) {
					bpmActions.add(testFilter(transitionFilter, transition, action));
				}
			}
			actions.clear();
			actions.addAll(bpmActions);
		}
	}

	private static Action testFilter(Predicate<SequenceFlowEntry> transitionFilter, SequenceFlowEntry transition,
			Action action) {
		if (transitionFilter.test(transition)) {
			return action;
		}
		return cloneAction(action);
	}

	private static Action cloneAction(Action action) {
		EmfAction bpmAction = new EmfAction(action.getActionId());
		bpmAction.setPurpose(action.getPurpose());
		bpmAction.setDisabledReason(action.getDisabledReason());
		bpmAction.setConfirmationMessage(action.getConfirmationMessage());
		bpmAction.setIconImagePath(action.getIconImagePath());
		bpmAction.setLabel(action.getLabel());
		bpmAction.setTooltip(action.getTooltip());
		bpmAction.setDisabled(true);
		return bpmAction;
	}
}
