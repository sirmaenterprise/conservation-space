package com.sirma.itt.seip.rest.metrics;

import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.seip.rest.metrics.PingResponse;
import com.sirma.itt.seip.rest.metrics.PingRestService;
import com.sirma.itt.seip.rest.metrics.PingResponse.PingStatus;

public class PingRestServiceTest {

	@Test
	public void testOkResponse() {
		PingRestService service = new PingRestService();
		PingResponse pong = service.ping();
		
		Assert.assertNotNull(pong);
		Assert.assertEquals(PingStatus.OK, pong.getStatus());
	}
}
