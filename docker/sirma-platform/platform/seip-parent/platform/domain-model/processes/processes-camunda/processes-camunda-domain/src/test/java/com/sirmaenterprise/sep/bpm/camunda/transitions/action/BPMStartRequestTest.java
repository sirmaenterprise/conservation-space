package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link BPMStartRequest}.
 *
 * @author hlungov
 */
public class BPMStartRequestTest {

	@Test
	public void getOperationTest() {
		BPMStartRequest startRequest = new BPMStartRequest();
		Assert.assertEquals(BPMStartRequest.START_OPERATION, startRequest.getOperation());
	}
}
