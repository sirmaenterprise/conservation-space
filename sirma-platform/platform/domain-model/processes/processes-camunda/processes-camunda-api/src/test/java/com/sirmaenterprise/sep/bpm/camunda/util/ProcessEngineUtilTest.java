package com.sirmaenterprise.sep.bpm.camunda.util;

import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.DEFAULT_ENGINE;
import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockActivity;
import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockDelegateExecution;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.ProcessEngineServices;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.junit.Test;

import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationRuntimeException;

/**
 * Tests the {@link ProcessEngineUtil}
 * 
 * @author bbanchev
 */
public class ProcessEngineUtilTest {

	@Test
	public void testGetProcessEngineName() throws Exception {
		String engineName = ProcessEngineUtil
				.getProcessEngineName(mockActivity(mockDelegateExecution(DEFAULT_ENGINE, ActivityExecution.class)));
		assertEquals(DEFAULT_ENGINE, engineName);
	}

	@Test(expected = CamundaIntegrationRuntimeException.class)
	public void testGetProcessEngineNameWithException() throws Exception {
		DelegateExecution mockDelegateExecution = mockDelegateExecution(DEFAULT_ENGINE, DelegateExecution.class);
		// downgrade engine
		ProcessEngineServices simpleEngine = mock(ProcessEngineServices.class);
		when(mockDelegateExecution.getProcessEngineServices()).thenReturn(simpleEngine);
		String engineName = ProcessEngineUtil.getProcessEngineName(mockDelegateExecution);
		assertEquals(DEFAULT_ENGINE, engineName);
	}
}
