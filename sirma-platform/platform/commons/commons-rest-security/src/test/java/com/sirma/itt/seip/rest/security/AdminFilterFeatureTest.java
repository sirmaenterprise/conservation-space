package com.sirma.itt.seip.rest.security;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.rest.annotations.security.AdminResource;
import com.sirma.itt.seip.rest.filters.AdminFilter;
import com.sirma.itt.seip.rest.secirity.AdminFilterFeature;

/**
 * Tests for {@link AdminFilterFeature}.
 *
 * @author smustafov
 */
public class AdminFilterFeatureTest {

	@Mock
	private AdminFilter filter;

	@Mock
	private ResourceInfo resourceInfo;

	@Mock
	private FeatureContext context;

	@InjectMocks
	private AdminFilterFeature feature;

	@Before
	public void before() {
		feature = new AdminFilterFeature();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testConfigure_withClassAnnotation() throws Exception {
		when(resourceInfo.getResourceClass()).thenAnswer((invocation) -> AdminResourceClass.class);
		when(resourceInfo.getResourceMethod())
				.thenReturn(AdminFilterFeatureTest.class.getDeclaredMethod("nonAdminResource"));

		feature.configure(resourceInfo, context);

		verify(context).register(filter);
	}

	@Test
	public void testConfigure_withMethodAnnotation() throws Exception {
		when(resourceInfo.getResourceClass()).thenAnswer((invocation) -> NonAdminResourceClass.class);
		when(resourceInfo.getResourceMethod())
				.thenReturn(AdminFilterFeatureTest.class.getDeclaredMethod("adminResource"));

		feature.configure(resourceInfo, context);

		verify(context).register(filter);
	}

	@Test
	public void testConfigure_withNoAnnotation() throws Exception {
		when(resourceInfo.getResourceClass()).thenAnswer((invocation) -> NonAdminResourceClass.class);
		when(resourceInfo.getResourceMethod())
				.thenReturn(AdminFilterFeatureTest.class.getDeclaredMethod("nonAdminResource"));

		feature.configure(resourceInfo, context);

		verify(context, times(0)).register(filter);
	}

	@AdminResource
	private void adminResource() {
		// used as test data
	}

	private void nonAdminResource() {
		// used as test data
	}

	@AdminResource
	private class AdminResourceClass {
		// used as test data
	}

	private class NonAdminResourceClass {
		// used as test data
	}

}
