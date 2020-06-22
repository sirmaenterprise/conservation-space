package com.sirma.itt.seip.security.listeners;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.util.ReflectionUtils;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;

/**
 * Test creation and destruction of security context on each request.
 * 
 * @author yasko
 */
@Test
public class SecurityContextWebListenerTest {
	SecurityContextWebListener listener;
	
	@Mock
	HttpServletRequest request;
	
	@Mock
	ServletRequestEvent event;
	
	@Mock
	SecurityContext context;
	
	@Mock
	SecurityContextManager securityContextManager;
	
	/**
	 * Initialize test.
	 */
	@BeforeMethod
	protected void init() {
		listener = new SecurityContextWebListener();
		
		MockitoAnnotations.initMocks(this);
		when(securityContextManager.getCurrentContext()).thenReturn(context);
		when(event.getServletRequest()).thenReturn(request);
		
		ReflectionUtils.setFieldValue(listener, "securityContextManager", securityContextManager);
	}
	
	/**
	 * Test initialization of security context upon request.
	 */
	public void testCreateContext() {
		listener.requestInitialized(event);
		verify(securityContextManager).initializeExecution(Mockito.any());
	}
	
	/**
	 * Test destruction of security context when there isn't one.
	 */
	public void testDestroyInactiveContext() {
		when(context.isActive()).thenReturn(Boolean.FALSE);
		
		listener.requestDestroyed(null);
		verify(securityContextManager, never()).endExecution();
	}
	
	/**
	 * Test destruction of active security context.
	 */
	public void testDestroyActiveContext() {
		when(context.isActive()).thenReturn(Boolean.TRUE);
		
		listener.requestDestroyed(null);
		verify(securityContextManager).endExecution();
	}
}
