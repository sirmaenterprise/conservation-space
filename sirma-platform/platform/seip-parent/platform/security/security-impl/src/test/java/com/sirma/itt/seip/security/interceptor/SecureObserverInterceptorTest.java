/**
 *
 */
package com.sirma.itt.seip.security.interceptor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.interceptor.InvocationContext;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.security.mocks.SecureEventMock;
import com.sirma.itt.seip.security.util.SecureEvent;

/**
 * The Class SecureObserverInterceptorTest.
 *
 * @author BBonev
 */
public class SecureObserverInterceptorTest {

	@Mock
	InvocationContext context;

	@InjectMocks
	SecureObserverInterceptor interceptor;

	/**
	 * Before method.
	 */
	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test no secure event.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testNoSecureEvent() throws Exception {
		interceptor.manageSecurityContext(context);
		verify(context).proceed();
	}

	/**
	 * Test with secure event.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testWithSecureEvent() throws Exception {
		SecureEvent event = new SecureEventMock();
		SecureEvent secureEvent = spy(event);
		when(context.getParameters()).thenReturn(new Object[] { secureEvent });
		interceptor.manageSecurityContext(context);
		verify(context).proceed();
		verify(secureEvent).call(any());
	}
}
