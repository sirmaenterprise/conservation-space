package com.sirma.itt.seip.rest.metrics;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.seip.rest.metrics.PingResponse;
import com.sirma.itt.seip.rest.metrics.PingResponse.PingStatus;

public class PingResponseTest {

	@Test
	public void testFromStatus() {
		PingResponse response = PingResponse.fromStatus(PingStatus.OK);
		
		Assert.assertNotNull(response);
		Assert.assertEquals(PingStatus.OK, response.getStatus());
	}
	
	@Test(expected = NullPointerException.class)
	public void testFromStatusReuireStatus() {
		PingResponse.fromStatus(null);
	}
}
