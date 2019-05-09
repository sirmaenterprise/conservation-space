package com.sirma.itt.seip.rest.filters.response;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests {@link NoCacheResponseFilter}.
 *
 * @author smustafov
 */
public class NoCacheResponseFilterTest {

	@Test
	public void testDisableCachingHeaders() throws IOException {
		NoCacheResponseFilter filter = Mockito.spy(NoCacheResponseFilter.class);

		MultivaluedMap<String, Object> map = new MultivaluedHashMap<>();
		ContainerRequestContext request = Mockito.mock(ContainerRequestContext.class);
		ContainerResponseContext response = Mockito.mock(ContainerResponseContext.class);
		Mockito.when(response.getHeaders()).thenReturn(map);

		filter.filter(request, response);

		Assert.assertEquals(3, map.size());
		Assert.assertNotNull(map.get("Cache-Control"));
		Assert.assertNotNull(map.get("Pragma"));
		Assert.assertNotNull(map.get("Expires"));
	}

}
