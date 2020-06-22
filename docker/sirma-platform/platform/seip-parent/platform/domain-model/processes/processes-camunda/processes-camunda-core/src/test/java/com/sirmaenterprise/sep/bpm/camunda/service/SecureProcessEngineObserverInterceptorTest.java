package com.sirmaenterprise.sep.bpm.camunda.service;

import static org.mockito.Mockito.when;

import javax.interceptor.InvocationContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationException;

/**
 * Tests {@link SecureProcessEngineObserverInterceptor}
 * 
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class SecureProcessEngineObserverInterceptorTest {
	@Mock
	private BPMSecurityService bpmSecurityService;
	@InjectMocks
	private SecureProcessEngineObserverInterceptor secureProcessEngineObserverInterceptor;

	@Test(expected = CamundaIntegrationException.class)
	public void testInactiveContext() throws Exception {
		InvocationContext invocationContext = Mockito.mock(InvocationContext.class);
		when(invocationContext.getMethod()).thenReturn(this.getClass().getDeclaredMethod("securedMethod"));
		when(bpmSecurityService.getProcessEngineStatus()).thenReturn(ProcessEngineStatus.INVALID_REQUEST);
		secureProcessEngineObserverInterceptor.checkSecurityContext(invocationContext);
	}

	@Test
	public void testValidContext() throws Exception {
		InvocationContext invocationContext = Mockito.mock(InvocationContext.class);
		when(invocationContext.getMethod()).thenReturn(this.getClass().getDeclaredMethod("securedMethod"));
		when(bpmSecurityService.getProcessEngineStatus()).thenReturn(ProcessEngineStatus.AVAILABLE);
		secureProcessEngineObserverInterceptor.checkSecurityContext(invocationContext);
	}

	@Test(expected = CamundaIntegrationException.class)
	public void testUnavailableContextThrowingException() throws Exception {
		InvocationContext invocationContext = Mockito.mock(InvocationContext.class);
		when(invocationContext.getMethod())
				.thenReturn(this.getClass().getDeclaredMethod("securedMethodWithConfigurations"));
		when(bpmSecurityService.getProcessEngineStatus()).thenReturn(ProcessEngineStatus.UNAVAILABLE);
		secureProcessEngineObserverInterceptor.checkSecurityContext(invocationContext);
	}

	@Test
	public void testUnavailableContextSilently() throws Exception {
		InvocationContext invocationContext = Mockito.mock(InvocationContext.class);
		when(invocationContext.getMethod()).thenReturn(this.getClass().getDeclaredMethod("securedMethod"));
		when(bpmSecurityService.getProcessEngineStatus()).thenReturn(ProcessEngineStatus.UNAVAILABLE);
		secureProcessEngineObserverInterceptor.checkSecurityContext(invocationContext);
	}

	@SecureProcessEngine
	void securedMethod() throws Exception {
		// used to provide annotated method
	}

	@SecureProcessEngine(notInitializedAccepted = false)
	void securedMethodWithConfigurations() throws Exception {
		// used to provide annotated method
	}

}
