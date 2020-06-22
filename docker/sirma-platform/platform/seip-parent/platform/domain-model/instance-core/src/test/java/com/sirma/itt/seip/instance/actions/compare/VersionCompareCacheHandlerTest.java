package com.sirma.itt.seip.instance.actions.compare;

import java.util.Arrays;
import java.util.Date;

import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.Assert;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;

@RunWith(MockitoJUnitRunner.class)
public class VersionCompareCacheHandlerTest {

	@InjectMocks
	private VersionCompareCacheHandler handler;

	@Mock
	private InstanceTypeResolver resolver;

	@Mock
	private ContainerResponseContext responseContext;

	@Mock
	private MultivaluedMap<String, Object> responseHeaders;

	@Mock
	private UriInfo info;

	@Mock
	private MultivaluedMap<String, String> params;

	@Mock
	private Instance first;

	@Mock
	private Instance second;

	@Mock
	private Date firstModDate;

	@Mock
	private Date secondModDate;

	@Mock
	private Request request;

	@Before
	public void init() {
		Mockito.when(firstModDate.getTime()).thenReturn(10L);
		Mockito.when(secondModDate.getTime()).thenReturn(20L);

		Mockito.when(first.get("modifiedOn", Date.class)).thenReturn(firstModDate);
		Mockito.when(second.get("modifiedOn", Date.class)).thenReturn(secondModDate);

		Mockito.when(params.getFirst("id")).thenReturn("1");
		Mockito.when(params.getFirst("first")).thenReturn("1-v1");
		Mockito.when(params.getFirst("second")).thenReturn("1-v2");

		Mockito.when(info.getPathParameters()).thenReturn(params);
		Mockito.when(info.getQueryParameters()).thenReturn(params);

		Mockito.when(responseContext.getHeaders()).thenReturn(responseHeaders);

		Mockito.when(resolver.resolveInstances(Arrays.asList("1-v1", "1-v2"))).thenReturn(Arrays.asList(first, second));
	}

	@Test
	public void testHandleResponse() {
		handler.handleResponse(info, responseContext);
		Mockito.verify(responseHeaders).add(HttpHeaders.ETAG, "\"1-1020\"");
		Mockito.verify(responseHeaders).add(HttpHeaders.CACHE_CONTROL, "no-cache");
	}

	@Test
	public void testHandleRequest() {
		ResponseBuilder builder = Mockito.mock(ResponseBuilder.class);
		Mockito.when(request.evaluatePreconditions(Mockito.any(EntityTag.class))).thenReturn(builder);

		ResponseBuilder result = handler.handleRequest(info, request);
		Assert.assertNotNull(result);
	}

	@Test
	public void testMissingParameters() {
		Mockito.when(params.getFirst("id")).thenReturn("");

		Assert.assertNull(handler.handleRequest(info, request));
		Mockito.verify(request, Mockito.never()).evaluatePreconditions(Mockito.any(EntityTag.class));

		Mockito.when(params.getFirst("id")).thenReturn("1");
		Mockito.when(params.getFirst("first")).thenReturn(null);
		Mockito.when(params.getFirst("second")).thenReturn("1-v2");

		Assert.assertNull(handler.handleRequest(info, request));
		Mockito.verify(request, Mockito.never()).evaluatePreconditions(Mockito.any(EntityTag.class));

		Mockito.when(params.getFirst("id")).thenReturn("1");
		Mockito.when(params.getFirst("first")).thenReturn("1-v1");
		Mockito.when(params.getFirst("second")).thenReturn(" ");

		Assert.assertNull(handler.handleRequest(info, request));
		Mockito.verify(request, Mockito.never()).evaluatePreconditions(Mockito.any(EntityTag.class));
	}
}
