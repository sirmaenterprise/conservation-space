package com.sirmaenterprise.sep.bpm.camunda.transitions.states;

import static com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants.TRANSITIONS;
import static com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants.TRANSITIONS_NONPERSISTED;
import static com.sirmaenterprise.sep.bpm.camunda.util.BPMActivityUtil.isActivityInProgress;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.definition.StateTransition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.state.DynamicStateTransitionProvider;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.plugin.Extension;

/**
 * The {@link BPMStateTransitionProvider} relays on the current BPM model for instances part of BPM execution. The list
 * of {@link StateTransition} is based on the possible transitions for current activity (the provided instance).
 * Generated {@link StateTransition}s has predefined fromState==currentState(instance) and toState=COMPLETED.
 * 
 * @author bbanchev
 */
@Extension(target = DynamicStateTransitionProvider.TARGET_NAME)
public class BPMStateTransitionProvider implements DynamicStateTransitionProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private StateService stateService;

	@Override
	public String getName() {
		return BPMStateTransitionProvider.class.getSimpleName();
	}

	@Override
	public List<StateTransition> provide(Instance activity) {
		SequenceFlowModel flowModel = getSequenceFlowModel(activity);
		if (flowModel != null) {
			LOGGER.trace("Returnining dynamic bpm transitions based on model: {}", flowModel);
			return flowModel.toStateTransitionModel(stateService.getPrimaryState(activity), null);
		}
		return Collections.emptyList();
	}

	/**
	 * Retrieves the current sequence flow model for activity based on its metadata
	 * 
	 * @param activity
	 *            the instance to retrieve transition model for
	 * @return the related {@link SequenceFlowModel} or null if instance is not an activity or not in progress
	 */
	public static SequenceFlowModel getSequenceFlowModel(Instance activity) {
		// if activity is completed should be skipped
		if (!isActivityInProgress(activity)) {
			return null;
		}
		// check if data is cached as the default value
		String transitionModel = activity.getAsString(TRANSITIONS);
		if (transitionModel == null) {
			// fallback to temporary models
			transitionModel = activity.getAsString(TRANSITIONS_NONPERSISTED);
		}
		return SequenceFlowModel.deserialize(transitionModel);
	}

	/**
	 * Finds a transition based on the current activity {@link SequenceFlowModel} and the desired transition id
	 * 
	 * @param activity
	 *            the instance to retrieve transition model for
	 * @param transitionId
	 *            the transition id
	 * @return the transition entity or null if not found
	 */
	public static SequenceFlowEntry getTransition(Instance activity, String transitionId) {
		SequenceFlowModel flowModel = getSequenceFlowModel(activity);
		if (flowModel != null && flowModel.containsTransition(transitionId)) {
			return flowModel.getTransition(transitionId);
		}
		return null;
	}

}
