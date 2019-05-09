package com.sirma.itt.seip.rest.filters;

import java.lang.reflect.Method;

import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.rest.annotations.Cache;
import com.sirma.itt.seip.rest.filters.response.NoCacheFilterProvider;
import com.sirma.itt.seip.rest.filters.response.NoCacheResponseFilter;

@RunWith(MockitoJUnitRunner.class)
public class NoCacheFilterProviderTest {

	@Mock
	private ResourceInfo resource;

	@Mock
	private FeatureContext context;

	@Test
	public void testShouldAddFilter() throws NoSuchMethodException, SecurityException {
		Method method = NoCacheFilterProviderTest.class.getDeclaredMethod("dummyNonCachableResourceMethod");
		Mockito.when(resource.getResourceMethod()).thenReturn(method);

		new NoCacheFilterProvider().configure(resource, context);

		Mockito.verify(context).register(NoCacheResponseFilter.class);
	}

	@Test
	public void testShoulNotdAddFilter() throws NoSuchMethodException, SecurityException {
		Method method = NoCacheFilterProviderTest.class.getDeclaredMethod("dummyCachableResourceMethod");
		Mockito.when(resource.getResourceMethod()).thenReturn(method);

		new NoCacheFilterProvider().configure(resource, context);

		Mockito.verify(context, Mockito.never()).register(Mockito.any());
	}

	public void dummyNonCachableResourceMethod() {
		// used in mocks
	}

	@Cache
	public void dummyCachableResourceMethod() {
		// used in mocks
	}
}
