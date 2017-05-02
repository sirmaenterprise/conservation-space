package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

import static com.sirmaenterprise.sep.bpm.model.ProcessConstants.ACTIVITY_ID;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirmaenterprise.sep.bpm.bpmn.ProcessService;
import com.sirmaenterprise.sep.bpm.exception.BPMException;

/**
 * Test for {@link BPMStopAction}
 *
 * @author hlungov
 */
@RunWith(MockitoJUnitRunner.class)
public class BPMStopActionTest {

	@Mock
	private ProcessService processService;

	@InjectMocks
	private BPMStopAction bpmStopAction;

	@Test
	public void getNameTest() {
		Assert.assertEquals(BPMStopRequest.STOP_OPERATION, bpmStopAction.getName());
	}

	@Test
	public void executeBPMActionTest() throws BPMException {
		BPMStopRequest request = new BPMStopRequest();
		InstanceReference workflowReference = mock(InstanceReference.class);
		Instance process = mock(Instance.class);
		when(workflowReference.toInstance()).thenReturn(process);
		InstanceType instanceType = mock(InstanceType.class);
		when(process.type()).thenReturn(instanceType);
		when(instanceType.is("workflowinstancecontext")).thenReturn(Boolean.TRUE);
		when(process.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		request.setTargetReference(workflowReference);
		bpmStopAction.executeBPMAction(request);
		verify(processService).cancelProcess(eq(process));
	}

	@Test
	public void executeBPMActionTest_not_a_process() throws BPMException {
		BPMStopRequest request = new BPMStopRequest();
		InstanceReference workflowReference = mock(InstanceReference.class);
		Instance process = mock(Instance.class);
		when(workflowReference.toInstance()).thenReturn(process);
		InstanceType instanceType = mock(InstanceType.class);
		when(process.type()).thenReturn(instanceType);
		when(instanceType.is("workflowinstancecontext")).thenReturn(Boolean.FALSE);
		when(process.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.TRUE);
		request.setTargetReference(workflowReference);
		bpmStopAction.executeBPMAction(request);
		verify(processService, never()).cancelProcess(eq(process));
	}

	@Test
	public void executeBPMActionTest_not_a_activiti() throws BPMException {
		BPMStopRequest request = new BPMStopRequest();
		InstanceReference workflowReference = mock(InstanceReference.class);
		Instance process = mock(Instance.class);
		when(workflowReference.toInstance()).thenReturn(process);
		InstanceType instanceType = mock(InstanceType.class);
		when(process.type()).thenReturn(instanceType);
		when(instanceType.is("workflowinstancecontext")).thenReturn(Boolean.TRUE);
		when(process.isValueNotNull(ACTIVITY_ID)).thenReturn(Boolean.FALSE);
		request.setTargetReference(workflowReference);
		bpmStopAction.executeBPMAction(request);
		verify(processService, never()).cancelProcess(eq(process));
	}
}