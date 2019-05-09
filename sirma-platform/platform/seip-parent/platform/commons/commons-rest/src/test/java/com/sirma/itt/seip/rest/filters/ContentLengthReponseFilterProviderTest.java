package com.sirma.itt.seip.rest.filters;

import java.io.File;
import java.lang.reflect.Method;

import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContentLengthReponseFilterProviderTest {

	@Mock
	private ResourceInfo resourceInfo;

	@Mock
	private FeatureContext context;

	@Test
	public void testRegisterFilter() throws NoSuchMethodException, SecurityException {
		Method m = ContentLengthReponseFilterProviderTest.class.getDeclaredMethod("dummyFileReturningReource");
		Mockito.when(resourceInfo.getResourceMethod()).thenReturn(m);

		new ContentLengthReponseFilterProvider().configure(resourceInfo, context);
		Mockito.verify(context).register(ContentLengthReponseFilter.class);
	}

	@Test
	public void testShouldNotRegisterFilter() throws NoSuchMethodException, SecurityException {
		Method m = ContentLengthReponseFilterProviderTest.class.getDeclaredMethod("dummyStringReturningReource");
		Mockito.when(resourceInfo.getResourceMethod()).thenReturn(m);

		new ContentLengthReponseFilterProvider().configure(resourceInfo, context);
		Mockito.verify(context, Mockito.never()).register(Mockito.any());
	}

	public File dummyFileReturningReource() {
		// dummy method used in mocks
		return null;
	}

	public String dummyStringReturningReource() {
		// dummy method used in mocks
		return null;
	}
}
