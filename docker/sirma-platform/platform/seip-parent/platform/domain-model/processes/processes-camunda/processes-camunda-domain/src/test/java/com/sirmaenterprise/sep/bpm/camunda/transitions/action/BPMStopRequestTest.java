package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link BPMStopRequest}
 *
 * @author hlungov
 */
public class BPMStopRequestTest {

	@Test
	public void getOperationTest() {
		BPMStopRequest bpmStopRequest = new BPMStopRequest();
		Assert.assertEquals(BPMStopRequest.STOP_OPERATION, bpmStopRequest.getOperation());
	}

}
