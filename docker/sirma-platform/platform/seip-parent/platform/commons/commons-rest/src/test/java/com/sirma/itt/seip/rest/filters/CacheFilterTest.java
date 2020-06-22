package com.sirma.itt.seip.rest.filters;

import java.io.IOException;
import java.util.HashSet;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.core.Response.StatusType;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.rest.annotations.Cache;
import com.sirma.itt.seip.rest.cache.CacheHandler;
import com.sirma.itt.seip.util.CDI;

@RunWith(MockitoJUnitRunner.class)
public class CacheFilterTest {

	@Mock
	private BeanManager bm;

	@Mock
	private CacheHandler handler;

	@Mock
	private ResourceInfo resource;

	@Mock
	private ContainerRequestContext requestContext;

	@Mock
	private ContainerResponseContext responseContext;

	@Mock
	private StatusType statusType;

	@InjectMocks
	private CacheFilter filter;

	@Before
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void init() throws NoSuchMethodException, SecurityException {
		Mockito.when(bm.getBeans(Mockito.any(), Mockito.any())).thenReturn(new HashSet<>());

		Bean bean = Mockito.mock(Bean.class);
		CreationalContext context = Mockito.mock(CreationalContext.class);

		Mockito.when(bm.resolve(Mockito.any())).thenReturn(bean);
		Mockito.when(bm.createCreationalContext(bean)).thenReturn(context);
		Mockito.when(bean.create(context)).thenReturn(handler);
		Mockito.when(resource.getResourceMethod()).thenReturn(CacheFilterTest.class.getDeclaredMethod("dummyCachableReource"));

		CDI.setCachedBeanManager(bm);
	}

	@Test
	public void testResourceHasntChanged() throws IOException {
		ResponseBuilder builder = Mockito.mock(ResponseBuilder.class);
		Mockito.when(handler.handleRequest(Mockito.any(), Mockito.any())).thenReturn(builder);

		filter.filter(requestContext);

		Mockito.verify(handler).handleRequest(Mockito.any(), Mockito.any());
		Mockito.verify(requestContext).abortWith(Mockito.any());
		Mockito.verify(builder).build();
	}

	@Test
	public void testResourceHasChanged() throws IOException {
		Mockito.when(handler.handleRequest(Mockito.any(), Mockito.any())).thenReturn(null);

		filter.filter(requestContext);

		Mockito.verify(handler).handleRequest(Mockito.any(), Mockito.any());
		Mockito.verify(requestContext, Mockito.never()).abortWith(Mockito.any());
	}

	@Test
	public void testReponseStatusUnavailable() throws IOException {
		Mockito.when(responseContext.getStatusInfo()).thenReturn(null);

		filter.filter(requestContext, responseContext);
		Mockito.verify(handler, Mockito.never()).handleResponse(Mockito.any(), Mockito.any());
	}

	@Test
	public void testResponseStatusUnsuccessful() throws IOException {
		Mockito.when(statusType.getFamily()).thenReturn(Family.CLIENT_ERROR);
		Mockito.when(responseContext.getStatusInfo()).thenReturn(statusType);

		filter.filter(requestContext, responseContext);
		Mockito.verify(handler, Mockito.never()).handleResponse(Mockito.any(), Mockito.any());
	}

	@Test
	public void testResponseCallHandler() throws IOException {
		Mockito.when(statusType.getFamily()).thenReturn(Family.SUCCESSFUL);
		Mockito.when(responseContext.getStatusInfo()).thenReturn(statusType);

		filter.filter(requestContext, responseContext);
		Mockito.verify(handler).handleResponse(Mockito.any(), Mockito.any());
	}

	@Cache
	public void dummyCachableReource() {
		// dummy method used in mocks
	}
}
