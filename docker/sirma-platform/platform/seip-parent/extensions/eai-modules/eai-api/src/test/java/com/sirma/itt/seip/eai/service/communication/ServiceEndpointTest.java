package com.sirma.itt.seip.eai.service.communication;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class ServiceEndpointTest {

	@Test
	public void testGetMethodEndpoint() throws Exception {
		ServiceEndpoint serviceEndpoint = createDefaultEndpoint();
		Assert.assertNotNull(serviceEndpoint.getMethodEndpoint("GET"));
		Assert.assertNull(serviceEndpoint.getMethodEndpoint("POST"));
	}

	@Test
	public void testGetServiceId() {
		ServiceEndpoint serviceEndpoint = createDefaultEndpoint();
		Assert.assertEquals(BaseEAIServices.SEARCH, serviceEndpoint.getServiceId());
	}

	@Test
	public void testGetMethodTimeout() {
		ServiceEndpoint serviceEndpoint = createDefaultEndpoint();
		Assert.assertEquals(Integer.valueOf(1000), serviceEndpoint.getMethodTimeout("GET"));
		Assert.assertNull(serviceEndpoint.getMethodTimeout("POST"));
	}

	@Test
	public void testSeal() {
		ServiceEndpoint serviceEndpoint = createDefaultEndpoint();
		Assert.assertFalse(serviceEndpoint.isSealed());
		serviceEndpoint.seal();
		Assert.assertTrue(serviceEndpoint.isSealed());
	}

	private ServiceEndpoint createDefaultEndpoint() {
		Map<String, Map<String, Serializable>> data = new HashMap<>();
		Map<String, Serializable> config = new HashMap<>();
		config.put("uri", "/myuri");
		config.put("timeout", Integer.valueOf(1000));
		data.put("GET", config);
		ServiceEndpoint serviceEndpoint = new ServiceEndpoint(BaseEAIServices.SEARCH, data);
		return serviceEndpoint;
	}

}
