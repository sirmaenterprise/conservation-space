package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link BPMClaimRequest}.
 *
 * @author hlungov
 */
public class BPMClaimRequestTest {

	@Test
	public void getOperationTest() {
		BPMClaimRequest claimRequest = new BPMClaimRequest();
		Assert.assertEquals(BPMClaimRequest.CLAIM_OPERATION, claimRequest.getOperation());
	}
}
