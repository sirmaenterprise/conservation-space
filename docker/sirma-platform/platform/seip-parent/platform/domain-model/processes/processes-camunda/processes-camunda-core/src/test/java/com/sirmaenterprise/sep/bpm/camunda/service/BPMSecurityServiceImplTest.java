package com.sirmaenterprise.sep.bpm.camunda.service;

import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.DEFAULT_ENGINE;
import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockProcessEngine;
import static com.sirmaenterprise.sep.bpm.camunda.util.ProcessEngineUtil.getTenantId;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.AtMost;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirmaenterprise.sep.bpm.camunda.exception.CamundaIntegrationException;

/**
 * Tests {@link BPMSecurityServiceImpl}
 *
 * @author bbanchev
 */
@RunWith(MockitoJUnitRunner.class)
public class BPMSecurityServiceImplTest {
	@Mock
	private ProcessEngine processEngine;
	@Mock
	private SecurityContext securityContext;
	@Mock
	private SecurityContextManager securityContextManager;
	@InjectMocks
	private BPMSecurityServiceImpl bpmValidationService;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		when(processEngine.getName()).thenReturn(DEFAULT_ENGINE);
		when(securityContextManager.getCurrentContext()).thenReturn(securityContext);
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		when(securityContext.getCurrentTenantId()).thenReturn(SecurityContext.SYSTEM_TENANT);
		when(securityContext.isSystemTenant()).thenReturn(Boolean.TRUE);
		ProcessEngines.init();
		ProcessEngines.registerProcessEngine(processEngine);
	}

	@After
	public void tearDown() throws Exception {
		ProcessEngines.destroy();
	}

	@Test
	public void testInactiveContext() throws Exception {
		when(securityContext.isActive()).thenReturn(Boolean.FALSE);
		when(securityContext.isSystemTenant()).thenReturn(Boolean.FALSE);
		assertEquals(ProcessEngineStatus.INVALID_REQUEST, bpmValidationService.getProcessEngineStatus());
	}

	@Test
	public void testSystemTenantContext() throws Exception {
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		when(securityContext.getCurrentTenantId()).thenReturn(SecurityContext.SYSTEM_TENANT);
		when(securityContext.isSystemTenant()).thenReturn(Boolean.TRUE);
		assertEquals(ProcessEngineStatus.INVALID_REQUEST, bpmValidationService.getProcessEngineStatus());
	}

	@Test
	public void testDiffTenantContext() throws Exception {
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		when(securityContext.isSystemTenant()).thenReturn(Boolean.FALSE);
		ProcessEngines.registerProcessEngine(mockProcessEngine("camunda_test2"));
		when(securityContext.getCurrentTenantId()).thenReturn("camunda.test2");
		assertEquals(ProcessEngineStatus.ERROR, bpmValidationService.getProcessEngineStatus());
	}

	@Test
	public void testUnregisteredEngineContext() throws Exception {
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		when(securityContext.isSystemTenant()).thenReturn(Boolean.FALSE);
		when(securityContext.getCurrentTenantId()).thenReturn("camunda.test2");
		assertEquals(ProcessEngineStatus.UNAVAILABLE, bpmValidationService.getProcessEngineStatus());
	}

	@Test
	public void testValidTenantContext() throws Exception {
		when(securityContext.isActive()).thenReturn(Boolean.TRUE);
		when(securityContext.isSystemTenant()).thenReturn(Boolean.FALSE);
		when(securityContext.getCurrentTenantId()).thenReturn(DEFAULT_ENGINE.replace("_", "."));
		assertEquals(ProcessEngineStatus.AVAILABLE, bpmValidationService.getProcessEngineStatus());
	}

	@Test(expected = CamundaIntegrationException.class)
	public void testExecuteEngineOperationWrongContext() throws Exception {
		when(securityContext.getCurrentTenantId()).thenReturn(getTenantId(DEFAULT_ENGINE));
		when(securityContext.isSystemTenant()).thenReturn(Boolean.FALSE);
		Callable callable = mock(Callable.class);
		bpmValidationService.executeEngineOperation("camunda_engine2", callable);
	}

	@Test
	public void testExecuteEngineOperationContextSwitch() throws Exception {
		Callable callable = mock(Callable.class);
		bpmValidationService.executeEngineOperation("camunda_engine2", callable);
		verify(securityContextManager).initializeTenantContext(Mockito.eq(getTenantId("camunda_engine2")));
		verify(securityContextManager).endContextExecution();
	}

	@Test
	public void testExecuteEngineOperationWithExactContext() throws Exception {
		when(securityContext.getCurrentTenantId()).thenReturn(getTenantId(DEFAULT_ENGINE));
		when(securityContext.isSystemTenant()).thenReturn(Boolean.FALSE);
		Callable callable = mock(Callable.class);
		bpmValidationService.executeEngineOperation(DEFAULT_ENGINE, callable);
		verify(securityContextManager, new AtMost(0)).initializeTenantContext(Mockito.anyString());
	}

}
