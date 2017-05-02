package com.sirmaenterprise.sep.bpm.camunda.transitions.action;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link BPMReleaseRequest}.
 *
 * @author hlungov
 */
public class BPMReleaseRequestTest {

	@Test
	public void getOperationTest() {
		BPMReleaseRequest releaseRequest = new BPMReleaseRequest();
		Assert.assertEquals(BPMReleaseRequest.RELEASE_OPERATION, releaseRequest.getOperation());
	}

}
