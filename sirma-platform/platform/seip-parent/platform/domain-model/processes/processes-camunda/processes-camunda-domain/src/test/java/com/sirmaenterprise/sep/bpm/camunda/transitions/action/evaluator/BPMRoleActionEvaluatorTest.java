package com.sirmaenterprise.sep.bpm.camunda.transitions.action.evaluator;

import static com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants.TRANSITIONS;
import static com.sirmaenterprise.sep.bpm.camunda.transitions.action.evaluator.BPMRoleActionEvaluator.CLAIM;
import static com.sirmaenterprise.sep.bpm.camunda.transitions.action.evaluator.BPMRoleActionEvaluator.RELEASE;
import static com.sirmaenterprise.sep.bpm.model.ProcessConstants.ACTIVITY_ID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.instance.ObjectInstance;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.resources.Resource;
import com.sirmaenterprise.sep.bpm.camunda.bpmn.CamundaBPMNService;
import com.sirmaenterprise.sep.bpm.camunda.model.DomainProcessConstants;
import com.sirmaenterprise.sep.bpm.camunda.transitions.model.TransitionModelService;
import com.sirmaenterprise.sep.bpm.camunda.transitions.states.SequenceFlowModelTest;

/**
 * Test for {@link BPMRoleActionEvaluator}.
 *
 * @author hlungov
 */
@RunWith(MockitoJUnitRunner.class)
public class BPMRoleActionEvaluatorTest {

	@Mock
	private TransitionModelService transitionModelService;
	@Mock
	private CamundaBPMNService camundaBPMNService;
	@InjectMocks
	private BPMRoleActionEvaluator bpmRoleActionEvaluator;

	private static final Action TEST_BPM_ACTION = new EmfAction("id2");
	private List<Action> actionsList = Arrays.asList(CLAIM, RELEASE);
	private Set<Action> actions = null;

	private static final String INSTANCE_ID = "emf:instanceId";
	private static final String EMF_USER_ID = "emf:userId";

	@Before
	public void beforeMothod() {
		actions = new HashSet<>(actionsList);
	}

	@Test
	public void getSupportedObjectsTest() {
		Assert.assertFalse(bpmRoleActionEvaluator.getSupportedObjects().isEmpty());
		Assert.assertEquals(1, bpmRoleActionEvaluator.getSupportedObjects().size());
		Assert.assertEquals(ObjectInstance.class, bpmRoleActionEvaluator.getSupportedObjects().get(0));
	}

	@Test
	public void filterInternalTest_is_process_and_activity() {
		ObjectInstance taskInstance = buildInstance(INSTANCE_ID, true);
		when(taskInstance.isValueNotNull(Mockito.anyString())).thenReturn(Boolean.TRUE);
		bpmRoleActionEvaluator.filterInternal(taskInstance, mockUser(), null, actions);
		Assert.assertEquals(2, actions.size());
	}

	@Test
	public void filterInternalTest_is_completed_activity() {
		ObjectInstance taskInstance = buildInstance(INSTANCE_ID, false);
		when(taskInstance.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		when(taskInstance.get(Mockito.eq(DomainProcessConstants.COMPLETED_ON))).thenReturn(new Date());
		bpmRoleActionEvaluator.filterInternal(taskInstance, mockUser(), null, actions);
		verify(camundaBPMNService, never()).isTaskPooled(taskInstance);
	}

	@Test
	public void filterInternalTest_is_process_only() {
		ObjectInstance taskInstance = buildInstance(INSTANCE_ID, true);
		when(taskInstance.isValueNotNull(Mockito.anyString())).thenReturn(Boolean.FALSE);
		bpmRoleActionEvaluator.filterInternal(taskInstance, mockUser(), null, actions);
		Assert.assertEquals(2, actions.size());
	}

	@Test
	public void filterInternalTest_not_pooled_task_have_assignee() {
		ObjectInstance taskInstance = buildInstance(INSTANCE_ID, false);
		when(taskInstance.isValueNotNull(Mockito.eq(DomainProcessConstants.ACTIVITY_ID))).thenReturn(Boolean.TRUE);
		when(taskInstance.get(Mockito.eq(DomainProcessConstants.COMPLETED_ON))).thenReturn(null);
		Mockito.when(camundaBPMNService.isTaskAssignee(taskInstance, EMF_USER_ID)).thenReturn(Boolean.TRUE);
		Mockito.when(camundaBPMNService.isTaskPooled(taskInstance)).thenReturn(Boolean.FALSE);
		bpmRoleActionEvaluator.filterInternal(taskInstance, mockUser(), null, actions);
		Assert.assertTrue(actions.isEmpty());
	}

	@Test
	public void filterInternalTest_not_pooled_task_no_assignee() {
		ObjectInstance taskInstance = buildInstance(INSTANCE_ID, false);
		when(taskInstance.isValueNotNull(Mockito.eq(DomainProcessConstants.ACTIVITY_ID))).thenReturn(Boolean.TRUE);
		when(taskInstance.get(Mockito.eq(DomainProcessConstants.COMPLETED_ON))).thenReturn(null);
		Mockito.when(camundaBPMNService.isTaskPooled(taskInstance)).thenReturn(Boolean.FALSE);
		Mockito.when(camundaBPMNService.isTaskAssignee(taskInstance, EMF_USER_ID)).thenReturn(Boolean.FALSE);
		bpmRoleActionEvaluator.filterInternal(taskInstance, mockUser(), null, actions);
		Assert.assertTrue(actions.isEmpty());
	}

	@Test
	public void filterInternalTest_is_pooled_task_can_be_released() {
		ObjectInstance taskInstance = buildInstance(INSTANCE_ID, false);
		when(taskInstance.isValueNotNull(Mockito.eq(DomainProcessConstants.ACTIVITY_ID))).thenReturn(Boolean.TRUE);
		when(taskInstance.get(Mockito.eq(DomainProcessConstants.COMPLETED_ON))).thenReturn(null);
		Mockito.when(camundaBPMNService.isTaskPooled(taskInstance)).thenReturn(Boolean.TRUE);
		Mockito.when(camundaBPMNService.isTaskClaimable(taskInstance, EMF_USER_ID)).thenReturn(Boolean.FALSE);
		Mockito.when(camundaBPMNService.isTaskReleasable(taskInstance, EMF_USER_ID)).thenReturn(Boolean.TRUE);
		bpmRoleActionEvaluator.filterInternal(taskInstance, mockUser(), null, actions);
		Assert.assertEquals(1, actions.size());
		Assert.assertTrue(actions.contains(RELEASE));
	}

	@Test
	public void filterInternalTest_is_pooled_task_can_be_claimed() {
		ObjectInstance taskInstance = buildInstance(INSTANCE_ID, false);
		when(taskInstance.isValueNotNull(Mockito.eq(DomainProcessConstants.ACTIVITY_ID))).thenReturn(Boolean.TRUE);
		when(taskInstance.get(Mockito.eq(DomainProcessConstants.COMPLETED_ON))).thenReturn(null);
		Mockito.when(camundaBPMNService.isTaskPooled(taskInstance)).thenReturn(Boolean.TRUE);
		Mockito.when(camundaBPMNService.isTaskClaimable(taskInstance, EMF_USER_ID)).thenReturn(Boolean.TRUE);
		bpmRoleActionEvaluator.filterInternal(taskInstance, mockUser(), null, actions);
		Assert.assertEquals(1, actions.size());
		Assert.assertTrue(actions.contains(CLAIM));
	}

	@Test
	public void filterInternalTest_is_pooled_task_but_user_is_only_watcher() {
		ObjectInstance taskInstance = buildInstance(INSTANCE_ID, false);
		when(taskInstance.isValueNotNull(Mockito.eq(DomainProcessConstants.ACTIVITY_ID))).thenReturn(Boolean.TRUE);
		when(taskInstance.get(Mockito.eq(DomainProcessConstants.COMPLETED_ON))).thenReturn(null);
		Mockito.when(camundaBPMNService.isTaskPooled(taskInstance)).thenReturn(Boolean.TRUE);
		Mockito.when(camundaBPMNService.isTaskClaimable(taskInstance, EMF_USER_ID)).thenReturn(Boolean.FALSE);
		Mockito.when(camundaBPMNService.isTaskReleasable(taskInstance, EMF_USER_ID)).thenReturn(Boolean.FALSE);
		bpmRoleActionEvaluator.filterInternal(taskInstance, mockUser(), null, actions);
		Assert.assertEquals(0, actions.size());
	}

	@Test
	public void verifyBPMTransitionActionsTest_none_bpm_actions() {
		ObjectInstance taskInstance = buildInstance(INSTANCE_ID, false);
		when(taskInstance.getAsString(TRANSITIONS))
				.thenReturn(SequenceFlowModelTest.SERIALIZED_MODEL_WITH_UEL_CONDITION);
		bpmRoleActionEvaluator.verifyBPMTransitionActions(taskInstance, actions, false);
		Assert.assertEquals(2, actions.size());
	}

	@Test
	public void verifyBPMTransitionActionsTest_true_condition_on_bpm_action() {
		actions.add(TEST_BPM_ACTION);
		ObjectInstance taskInstance = buildInstance(INSTANCE_ID, false);
		when(taskInstance.getAsString(TRANSITIONS))
				.thenReturn(SequenceFlowModelTest.SERIALIZED_MODEL_WITH_UEL_CONDITION);
		when(transitionModelService.createTransitionConditionFilter(taskInstance)).thenReturn(entry -> true);
		bpmRoleActionEvaluator.verifyBPMTransitionActions(taskInstance, actions, false);
		Assert.assertEquals(3, actions.size());
	}

	@Test
	public void verifyBPMTransitionActionsTest_false_condition_on_bpm_action() {
		actions.add(TEST_BPM_ACTION);
		ObjectInstance taskInstance = buildInstance(INSTANCE_ID, false);
		when(taskInstance.getAsString(TRANSITIONS))
				.thenReturn(SequenceFlowModelTest.SERIALIZED_MODEL_WITH_UEL_CONDITION);
		when(transitionModelService.createTransitionConditionFilter(taskInstance)).thenReturn(entry -> false);
		bpmRoleActionEvaluator.verifyBPMTransitionActions(taskInstance, actions, false);
		Assert.assertEquals(3, actions.size());
	}

	@Test
	public void verifyBPMTransitionActionsTest_filter_bpm_action() {
		actions.add(TEST_BPM_ACTION);
		ObjectInstance taskInstance = buildInstance(INSTANCE_ID, false);
		when(taskInstance.getAsString(TRANSITIONS))
				.thenReturn(SequenceFlowModelTest.SERIALIZED_MODEL_WITH_UEL_CONDITION);
		bpmRoleActionEvaluator.verifyBPMTransitionActions(taskInstance, actions, true);
		Assert.assertEquals(2, actions.size());
	}

	private static ObjectInstance buildInstance(String targetId, boolean isProcess) {
		ObjectInstance instance = mock(ObjectInstance.class);
		when(instance.getId()).thenReturn(targetId);
		when(instance.getOrCreateProperties()).thenReturn(Collections.emptyMap());
		when(instance.getProperties()).thenReturn(Collections.emptyMap());
		when(instance.get("status")).thenReturn("status");
		InstanceReference instanceRef = mock(InstanceReference.class);
		InstanceType instanceType = mock(InstanceType.class);
		when(instanceType.is(Mockito.anyString())).thenReturn(isProcess);
		when(instance.type()).thenReturn(instanceType);
		when(instance.toReference()).thenReturn(instanceRef);
		when(instanceRef.toInstance()).thenReturn(instance);
		when(instanceRef.getId()).thenReturn(targetId);
		return instance;
	}

	private static Resource mockUser() {
		Resource user = Mockito.mock(Resource.class);
		Mockito.when(user.getId()).thenReturn(EMF_USER_ID);
		return user;
	}

}
