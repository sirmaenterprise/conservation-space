package com.sirma.itt.seip.rest.filters;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.container.ContainerRequestContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Test for {@link CustomHeaderInterceptorFilter}
 *
 * @author BBonev
 */
public class CustomHeaderInterceptorFilterTest {

	@InjectMocks
	private CustomHeaderInterceptorFilter filter;

	@Mock
	private UserStore userStore;

	@Mock
	private SecurityContext securityContext;

	@Mock
	private User user;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(securityContext.getAuthenticated()).thenReturn(user);
	}

	@Test
	public void callUserStoreToSetRequestProperties() throws Exception {
		when(securityContext.isActive()).thenReturn(true);
		filter.filter(mock(ContainerRequestContext.class));
		verify(userStore).setRequestProperties(eq(user), any());
	}

	@Test
	public void callUserStoreToSetRequestPropertiesNoContext() throws Exception {
		when(securityContext.isActive()).thenReturn(false);
		filter.filter(mock(ContainerRequestContext.class));
		verify(userStore, never()).setRequestProperties(eq(user), any());
	}
}
